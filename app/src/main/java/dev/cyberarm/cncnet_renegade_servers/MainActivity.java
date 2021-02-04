package dev.cyberarm.cncnet_renegade_servers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import dev.cyberarm.cncnet_renegade_servers.library.AppOnBootReceiver;
import dev.cyberarm.cncnet_renegade_servers.library.AppSync;
import dev.cyberarm.cncnet_renegade_servers.library.Callback;
import dev.cyberarm.cncnet_renegade_servers.library.RenegadeServer;
import dev.cyberarm.cncnet_renegade_servers.library.RenegadeServerListService;

public class MainActivity extends AppCompatActivity {
    LinearLayout container;
    SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        refresh = findViewById(R.id.refresh);

        if (!AppSync.appInitialized) {
            AppSync.initialize(getFilesDir());

            if (AppSync.settings.serviceAutoRefreshInterval > 0) {
                AppSync.startService(this);
            }
        }

        registerReceiver(new AppOnBootReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));

        AppSync.fetchList(new Callback() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateServerList(AppSync.serverList);
                    }
                });
            }
        }, false);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AppSync.fetchList(new Callback() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateServerList(AppSync.serverList);
                                refresh.setRefreshing(false);
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
                layout.setBackgroundColor(getResources().getColor(R.color.odd));
            }
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ServerViewActivity.class);
                    intent.putExtra("server_index", index);

                    startActivity(intent);
                }
            });

            TextView hostname = layout.findViewById(R.id.server_hostname);
            hostname.setText("" + (server.password ? "\uD83D\uDD12 " : "") + server.hostname);

            TextView mapname = layout.findViewById(R.id.server_mapname);
            mapname.setText(server.mapname);

            TextView players = layout.findViewById(R.id.server_players);
            players.setText("" + server.numplayers + "/" + server.maxplayers);

            container.addView(layout);
            i++;
        }
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

                AppSync.fetchList(new Callback() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateServerList(AppSync.serverList);
                                refresh.setRefreshing(false);
                            }
                        });
                    }
                }, true);
                break;
        }

        return true;
    }
}