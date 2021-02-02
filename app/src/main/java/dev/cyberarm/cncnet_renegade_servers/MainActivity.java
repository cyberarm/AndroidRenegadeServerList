package dev.cyberarm.cncnet_renegade_servers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import dev.cyberarm.cncnet_renegade_servers.library.AppSync;
import dev.cyberarm.cncnet_renegade_servers.library.Callback;
import dev.cyberarm.cncnet_renegade_servers.library.RenegadeServer;

public class MainActivity extends AppCompatActivity {
    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);

        AppSync.fetchList(new Callback() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateServerList(serverList);
                    }
                });
            }
        });
    }

    private void populateServerList(ArrayList<RenegadeServer> serverList) {
        container.removeAllViews();

        for (final RenegadeServer server : serverList) {
            View layout = View.inflate(this, R.layout.server_list_item, null);

            TextView hostname = layout.findViewById(R.id.server_hostname);
            hostname.setText("" + (server.password ? "\uD83D\uDD12 " : "") + server.hostname);

            TextView mapname = layout.findViewById(R.id.server_mapname);
            mapname.setText(server.mapname);

            TextView players = layout.findViewById(R.id.server_players);
            players.setText("" + server.players + "/" + server.maxPlayers);

            container.addView(layout);
        }
    }
}