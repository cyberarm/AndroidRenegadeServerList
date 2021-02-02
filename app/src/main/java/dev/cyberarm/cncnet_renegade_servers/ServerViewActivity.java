package dev.cyberarm.cncnet_renegade_servers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dev.cyberarm.cncnet_renegade_servers.library.AppSync;
import dev.cyberarm.cncnet_renegade_servers.library.RenegadePlayer;
import dev.cyberarm.cncnet_renegade_servers.library.RenegadeServer;

public class ServerViewActivity extends AppCompatActivity {

    private static final String TAG = "ServerViewActivity";
    RenegadeServer renegadeServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_view);

        Log.i(TAG, getIntent().getStringExtra("server"));

        renegadeServer = AppSync.gson().fromJson(getIntent().getStringExtra("server"), RenegadeServer.class);
        populateServerInfo();
    }

    private void populateServerInfo() {
        getSupportActionBar().setTitle(renegadeServer.hostname);
        LinearLayout playerInfo = findViewById(R.id.player_info);
        TextView map = findViewById(R.id.server_map);
        TextView country = findViewById(R.id.server_country);
        TextView players = findViewById(R.id.server_players);
        Button website = findViewById(R.id.server_website);

        map.setText(renegadeServer.mapname);
        country.setText(renegadeServer.country);
        players.setText("" + renegadeServer.numplayers + "/" + renegadeServer.maxplayers);

        if (renegadeServer.website != null && renegadeServer.website.length() > 0) {
            website.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + renegadeServer.website));
                    startActivity(browserIntent);
                }
            });
        } else {
            website.setVisibility(View.GONE);
        }

        playerInfo.removeAllViews();

        int i = 0;
        for (final RenegadePlayer player : renegadeServer.players) {
            View layout = View.inflate(this, R.layout.player_info_item, null);
            if (i % 2 == 1) {
                layout.setBackgroundColor(getResources().getColor(R.color.odd));
            }

            TextView team = layout.findViewById(R.id.team);
            TextView name = layout.findViewById(R.id.name);
            TextView score = layout.findViewById(R.id.score);
            TextView kills = layout.findViewById(R.id.kills);
            TextView deaths = layout.findViewById(R.id.deaths);
            team.setText(player.team);
            name.setText(player.name);
            score.setText("" + player.score);
            kills.setText("" + player.kills);
            deaths.setText("" + player.deaths);

            playerInfo.addView(layout);
            i++;
        }
    }
}