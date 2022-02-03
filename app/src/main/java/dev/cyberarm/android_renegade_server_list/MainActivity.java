package dev.cyberarm.android_renegade_server_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import dev.cyberarm.android_renegade_server_list.library.AppOnBootReceiver;
import dev.cyberarm.android_renegade_server_list.library.AppSync;
import dev.cyberarm.android_renegade_server_list.library.Callback;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServer;

public class MainActivity extends AppCompatActivity {
    LinearLayout container;
    SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        refresh = findViewById(R.id.refresh);
        refresh.setRefreshing(true);

        if (!AppSync.appInitialized) {
            AppSync.initialize(getFilesDir());

            if (AppSync.settings.serviceAutoRefreshInterval > 0) {
                AppSync.startService(this);
            }
        }

        registerReceiver(new AppOnBootReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));

        AppSync.fetchList(getApplicationContext(), new Callback() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AppSync.interfaceServerList = AppSync.serverList;
                        populateServerList(AppSync.interfaceServerList);
                    }
                });
            }
        }, false);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AppSync.fetchList(getApplicationContext(), new Callback() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AppSync.interfaceServerList = AppSync.serverList;
                                populateServerList(AppSync.interfaceServerList);
                            }
                        });
                    }
                }, true);
            }
        });
    }

    private void populateServerList(ArrayList<RenegadeServer> serverList) {
        container.removeAllViews();

        int i = 0;
        for (final RenegadeServer server : serverList) {
            final int index = i;

            View layout = View.inflate(this, R.layout.server_list_item, null);
            if (i % 2 == 1) {
                if (AppSync.isDarkMode(this)) {
                    layout.setBackgroundColor(getResources().getColor(R.color.odd_dark));
                } else {
                    layout.setBackgroundColor(getResources().getColor(R.color.odd));
                }
            }
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ServerViewActivity.class);
                    intent.putExtra("server_index", index);

                    startActivity(intent);
                }
            });

            ImageView gameIcon = layout.findViewById(R.id.server_game_icon);
            TextView hostname = layout.findViewById(R.id.server_hostname);
            hostname.setText("" + (server.status.password ? "\uD83D\uDD12 " : "") + server.status.name);

            gameIcon.setImageResource(AppSync.game_icon(server.game));

            TextView mapname = layout.findViewById(R.id.server_mapname);
            mapname.setText(server.status.map);

            TextView players = layout.findViewById(R.id.server_players);
            players.setText("" + server.status.numPlayers + "/" + server.status.maxPlayers);

            container.addView(layout);
            i++;
        }

        refresh.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, AppSettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_refresh:
                refresh.setRefreshing(true);

                AppSync.fetchList(getApplicationContext(), new Callback() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AppSync.interfaceServerList = AppSync.serverList;
                                populateServerList(AppSync.interfaceServerList);
                            }
                        });
                    }
                }, true);
                break;
        }

        return true;
    }
}