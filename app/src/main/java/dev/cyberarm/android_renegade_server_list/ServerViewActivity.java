package dev.cyberarm.android_renegade_server_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import dev.cyberarm.android_renegade_server_list.library.AppSync;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServerStatusPlayer;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServer;
import java8.util.Comparators;
import java8.util.stream.Collector;
import java8.util.stream.StreamSupport;

public class ServerViewActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "ServerViewActivity";
    private GestureDetectorCompat gestureDetector;
    private RenegadeServer renegadeServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_view);

        gestureDetector = new GestureDetectorCompat(this,this);

        populateServerInfo();
    }

    private void populateServerInfo() {
        renegadeServer = AppSync.interfaceServerList.get(getIntent().getIntExtra("server_index", 0));

        getSupportActionBar().setTitle(renegadeServer.status.name);
        LinearLayout playerInfo = findViewById(R.id.player_info);
        TextView map = findViewById(R.id.server_map);
        TextView region = findViewById(R.id.server_region);
        TextView players = findViewById(R.id.server_players);
        TextView time_elapsed = findViewById(R.id.server_time);
        TextView time_left = findViewById(R.id.server_time_left);
        ImageView gameIcon = findViewById(R.id.game_icon);
        ImageView gameBalanceIcon = findViewById(R.id.game_balance_icon);
        TextView team_0_name = findViewById(R.id.team_0_name);
        TextView team_0_score = findViewById(R.id.team_0_score);
        TextView team_1_name = findViewById(R.id.team_1_name);
        TextView team_1_score = findViewById(R.id.team_1_score);
        TextView scoreRatio = findViewById(R.id.score_ratio);

        // Some servers don't provide a non zero value for team score...
        double team0TotalScore = renegadeServer.status.teams.get(0).score > 0 ? renegadeServer.status.teams.get(0).score : StreamSupport.stream(renegadeServer.status.players).filter(p -> p.team == 0).mapToDouble(p -> p.score).sum();
        double team1TotalScore = renegadeServer.status.teams.get(1).score > 0 ? renegadeServer.status.teams.get(1).score : StreamSupport.stream(renegadeServer.status.players).filter(p -> p.team == 1).mapToDouble(p -> p.score).sum();
        double ratio = 1.0 / (team0TotalScore / team1TotalScore);
        // floating point divide by zero
        if (Double.isNaN(ratio)) {
            ratio = 1.0;
        }

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSz");
        Date date = null;
        long diff = 0, hours = 0, minutes = 0, seconds = 0;
        try {
            Log.i(TAG, renegadeServer.status.started);
            date = inFormat.parse(renegadeServer.status.started);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            Date now = new Date();
            diff = now.getTime() - date.getTime();
            hours = diff / (1000 * 60 * 60 * 24) % 365;
            minutes = diff / (1000 * 60) % 60;
            seconds = diff / (1000) % 60;
        }

        map.setText(renegadeServer.status.map);
        region.setText(renegadeServer.region);
        players.setText(String.format(Locale.US, "%d/%d", renegadeServer.status.numPlayers, renegadeServer.status.maxPlayers));
        time_elapsed.setText(date != null ? String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds) : "---");
        time_left.setText(renegadeServer.status.remaining);

        team_0_name.setText(
                String.format(Locale.US, "%s (%d)", renegadeServer.status.teams.get(0).name,
                        StreamSupport.stream(renegadeServer.status.players).filter(player -> player.team == 0).count()));
        team_0_score.setText(String.format(Locale.US,"%,.0f", team0TotalScore));

        team_1_name.setText(
                String.format(Locale.US, "%s (%d)", renegadeServer.status.teams.get(1).name,
                        StreamSupport.stream(renegadeServer.status.players).filter(player -> player.team == 1).count()));
        team_1_score.setText(String.format(Locale.US,"%,.0f", team1TotalScore));

        scoreRatio.setText(String.format(Locale.US,"%.2f", ratio));


        gameIcon.setImageResource(AppSync.game_icon(renegadeServer.game));
        estimateGameBalance(gameBalanceIcon, team0TotalScore, team1TotalScore, ratio);

        playerInfo.removeAllViews();

        int i = 0;
        for (final RenegadeServerStatusPlayer player : renegadeServer.status.players) {
            View layout = View.inflate(this, R.layout.player_info_item, null);
            if (i % 2 == 1) {
                if (AppSync.isDarkMode(this)) {
                    layout.setBackgroundColor(getResources().getColor(R.color.odd_dark));
                } else {
                    layout.setBackgroundColor(getResources().getColor(R.color.odd));
                }            }

            TextView team = layout.findViewById(R.id.team);
            TextView name = layout.findViewById(R.id.name);
            TextView score = layout.findViewById(R.id.score);
            TextView kills = layout.findViewById(R.id.kills);
            TextView deaths = layout.findViewById(R.id.deaths);
            team.setText(renegadeServer.status.teams.get(player.team).name);
            name.setText(player.nick);
            score.setText(String.format(Locale.US, "%,d", player.score));
            kills.setText(String.format(Locale.US, "%,d", player.kills));
            deaths.setText(String.format(Locale.US, "%,d", player.deaths));

            playerInfo.addView(layout);
            i++;
        }
    }

    private void estimateGameBalance(ImageView imageView, double team_0_score, double team_1_score, double ratio) {
        // Do stuff
        imageView.setImageResource(R.drawable.question);

        if (renegadeServer.status.players.size() < 20 && !renegadeServer.game.equals("ren")) {
            imageView.setImageResource(R.drawable.cross);
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_red), android.graphics.PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageView.setTooltipText("Probably too few players for a balanced game");
            }
        } else if (team_0_score + team_1_score < 2_500) {
            imageView.setImageResource(R.drawable.question);
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.gray), android.graphics.PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageView.setTooltipText("Score to low to estimate game balance");
            }
        } else if (ratio >= 0.75 && ratio <= 1.25) {
            imageView.setImageResource(R.drawable.checkmark);
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageView.setTooltipText("Game seems balanced based on score");
            }
        } else if (ratio < 0.75) {
            imageView.setImageResource(R.drawable.arrow_right);
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.unbalanced_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageView.setTooltipText(renegadeServer.status.teams.get(0).name + " are winning significantly");
            }
        } else {
            imageView.setImageResource(R.drawable.arrow_left);
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.unbalanced_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageView.setTooltipText(renegadeServer.status.teams.get(1).name + " are winning significantly");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.server_view_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, ServerSettingsActivity.class);
                intent.putExtra("server_id", renegadeServer.id);
                startActivity(intent);
                break;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent eventDown, MotionEvent eventUp, float velocityX, float velocityY) {
        float triggerVelocityX = 5_000;
        float maxVelocityY = 5_000;
        int minIndex = 0;
        int maxIndex = AppSync.serverList.size() - 1;
        int currentIndex = getIntent().getIntExtra("server_index", 0);

        // Swipe Right
        if (velocityX < -triggerVelocityX && Math.abs(velocityY) < maxVelocityY) {
            if (currentIndex + 1 <= maxIndex) {

                getIntent().putExtra("server_index", currentIndex + 1);
                populateServerInfo();

                return true;
            }

        // Swipe Left
        } else if (velocityX > triggerVelocityX && Math.abs(velocityY) < maxVelocityY) {
            if (currentIndex - 1 >= minIndex) {

                getIntent().putExtra("server_index", currentIndex - 1);
                populateServerInfo();

                return true;
            }
        }

        return false;
    }
}