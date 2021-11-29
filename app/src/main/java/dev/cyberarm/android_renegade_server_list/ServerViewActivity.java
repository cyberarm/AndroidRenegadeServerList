package dev.cyberarm.android_renegade_server_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import dev.cyberarm.android_renegade_server_list.library.AppSync;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServerStatusPlayer;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServer;

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
        renegadeServer = AppSync.serverList.get(getIntent().getIntExtra("server_index", 0));

        getSupportActionBar().setTitle(renegadeServer.status.name);
        LinearLayout playerInfo = findViewById(R.id.player_info);
        TextView map = findViewById(R.id.server_map);
        TextView region = findViewById(R.id.server_region);
        TextView players = findViewById(R.id.server_players);
        Button website = findViewById(R.id.server_website);

        map.setText(renegadeServer.status.map);
        region.setText(renegadeServer.region);
        players.setText("" + renegadeServer.status.numPlayers + "/" + renegadeServer.status.maxPlayers);

        website.setVisibility(View.GONE);

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
            score.setText("" + player.score);
            kills.setText("" + player.kills);
            deaths.setText("" + player.deaths);

            playerInfo.addView(layout);
            i++;
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