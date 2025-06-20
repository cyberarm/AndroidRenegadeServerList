package dev.cyberarm.android_renegade_server_list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import dev.cyberarm.android_renegade_server_list.library.AppSync;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServer;
import dev.cyberarm.android_renegade_server_list.library.ServerSettings;
import dev.cyberarm.android_renegade_server_list.library.Settings;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class AppSettingsActivity extends AppCompatActivity {
    private static final int EXPORT_FILE = 1;
    private static final int IMPORT_FILE = 2;
    private static final String TAG = "AppSettingsActivity";
    private static final String DEFAULT_SETTINGS_NAME = "renegade_server_list_settings.json";
    private static final String MIME_TYPE = "application/json";
    Button w3dHubWebsite;
    EditText renegadeUsername;
    TextView autoRefreshInterval;
    ToggleButton serviceAutoStart, refreshOnMeteredConnections, notifyRequireMultipleConditions;

    TextView notifyPlayerCount;
    TextView notifyMaps;
    TextView notifyUsernames;

    Button settingsExport, settingsImport, serverSettingsPurgeOrphaned;

    private ArrayList<ServerSettings> serverSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings_activity);

        getSupportActionBar().setTitle("App Settings");

        w3dHubWebsite = findViewById(R.id.w3dhub_website);
        w3dHubWebsite.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://w3dhub.com"));
            startActivity(browserIntent);
        });

        renegadeUsername = findViewById(R.id.username);
        autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        serviceAutoStart = findViewById(R.id.run_at_startup);
        refreshOnMeteredConnections = findViewById(R.id.refresh_on_metered_connections);

        notifyPlayerCount = findViewById(R.id.server_player_count);
        notifyMaps = findViewById(R.id.server_mapnames);
        notifyUsernames = findViewById(R.id.server_usernames);

        notifyRequireMultipleConditions = findViewById(R.id.server_require_multiple_conditions);

        settingsExport = findViewById(R.id.settings_export);
        settingsImport = findViewById(R.id.settings_import);
        serverSettingsPurgeOrphaned = findViewById(R.id.server_settings_purge_orphaned);

        settingsExport.setOnClickListener(view -> {
            exportSettings();
        });
        settingsImport.setOnClickListener(view -> {
            importSettings();
        });

        loadSettings();

        if (AppSync.interfaceServerList != null) {
            // Button
            serverSettingsPurgeOrphaned.setOnClickListener(view -> {
                AppSync.showConfirmationDialog(view.getContext(), "Confirm Purge","Are you sure?", true,
                    () -> {
                        for (ServerSettings serverSettings : serverSettings) {
                            if (!serverSettings.orphaned)
                                continue;

                            AppSync.settings.serverSettings.remove(AppSync.serverSettings(serverSettings.ID));
                        }

                        AppSync.saveSettings();

                        populateServerSettings();
                    },
                    null
                );
            });

            // Function
            populateServerSettings();
        } else {
            serverSettingsPurgeOrphaned.setVisibility(View.GONE);
        }
    }

    private void exportSettings() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(MIME_TYPE)
                            .putExtra(Intent.EXTRA_TITLE, DEFAULT_SETTINGS_NAME);
        startActivityForResult(intent, EXPORT_FILE);
    }


    private void importSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(MIME_TYPE)
                .putExtra(Intent.EXTRA_TITLE, DEFAULT_SETTINGS_NAME);
        startActivityForResult(intent, IMPORT_FILE);
    }

    private void loadSettings() {
        renegadeUsername.setText(AppSync.settings.renegadeUsername);
        autoRefreshInterval.setText(String.format("%d", AppSync.settings.serviceAutoRefreshInterval));
        serviceAutoStart.setChecked(AppSync.settings.serviceAutoStartAtBoot);
        refreshOnMeteredConnections.setChecked(AppSync.settings.refreshOnMeteredConnections);

        notifyPlayerCount.setText(String.format("%d", AppSync.settings.globalServerSettings.notifyPlayerCount));

        String notifyMapsString = StreamSupport.stream(AppSync.settings.globalServerSettings.notifyMapNames)
                .collect(Collectors.joining(", "));
        String notifyUsernamesString = StreamSupport.stream(AppSync.settings.globalServerSettings.notifyUsernames)
                .collect(Collectors.joining(", "));
        notifyMaps.setText(notifyMapsString);
        notifyUsernames.setText(notifyUsernamesString);
        notifyRequireMultipleConditions.setChecked(AppSync.settings.globalServerSettings.notifyRequireMultipleConditions);
    }

    private void saveSettings() {
        AppSync.settings.renegadeUsername = renegadeUsername.getText().toString();
        String autoRefreshIntervalValue = autoRefreshInterval.getText().toString().length() == 0 ? "0" : autoRefreshInterval.getText().toString();
        AppSync.settings.serviceAutoRefreshInterval = Integer.parseInt(autoRefreshIntervalValue);
        AppSync.settings.serviceAutoStartAtBoot = serviceAutoStart.isChecked();
        AppSync.settings.refreshOnMeteredConnections = refreshOnMeteredConnections.isChecked();

        String notifyPlayerCountValue = notifyPlayerCount.getText().toString().length() == 0 ? "0" : notifyPlayerCount.getText().toString();
        AppSync.settings.globalServerSettings.notifyPlayerCount = Integer.parseInt(notifyPlayerCountValue);
        AppSync.settings.globalServerSettings.notifyRequireMultipleConditions = notifyRequireMultipleConditions.isChecked();

        String[] maps = notifyMaps.getText().toString().split(",");
        String[] usernames = notifyUsernames.getText().toString().split(",");
        ArrayList<String> mapsList = new ArrayList<>();
        ArrayList<String> usernamesList = new ArrayList<>();

        for (String map : maps) {
            mapsList.add(map.trim());
        }
        for (String username : usernames) {
            usernamesList.add(username.trim());
        }

        AppSync.settings.globalServerSettings.notifyMapNames = mapsList;
        AppSync.settings.globalServerSettings.notifyUsernames = usernamesList;

        AppSync.saveSettings();

        if (AppSync.settings.serviceAutoRefreshInterval > 0) {
            AppSync.startWorker(this);
        } else {
            AppSync.stopWorker(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case EXPORT_FILE: {
                    Log.i(TAG, "EXPORT " + data);
                    try {
                        OutputStream io = this.getContentResolver().openOutputStream(data.getData(), "w");
                        io.write(AppSync.gson().toJson(AppSync.settings).getBytes());
                        io.close();

                        Toast.makeText(this, "Exported preferences", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case IMPORT_FILE: {
                    // TODO: Validate file
                    Log.i(TAG, "IMPORT " + data);
                    try {
                        InputStream io = this.getContentResolver().openInputStream(data.getData());
                        InputStreamReader inputStreamReader = new InputStreamReader(io);
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        StringBuilder stringBuffer = new StringBuilder();

                        String output;
                        while ((output = reader.readLine()) != null) {
                            stringBuffer.append(output);
                        }

                        reader.read();
                        io.close();
                        reader.close();

                        AppSync.settings = AppSync.gson().fromJson(stringBuffer.toString(), Settings.class);
                        AppSync.writeToFile(AppSync.getConfigFilePath(), stringBuffer.toString());
                        loadSettings();

                        Toast.makeText(this, "Imported preferences", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        saveSettings();

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_save, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveSettings();
            finish();
        }

        return true;
    }

    private void populateServerSettings() {
        serverSettings = new ArrayList<>();

        Log.i(TAG, "Checking " + AppSync.settings.serverSettings.size() + " server settings for orphans...");
        for (ServerSettings settings : AppSync.settings.serverSettings) {
            boolean orphaned = true;

            // FIXME: crashes if server list hasn't loaded yet
            for(RenegadeServer listedServer : AppSync.interfaceServerList) {
                if (settings.ID.equals(AppSync.serverUID(listedServer))) {
                    orphaned = false;
                    break;
                }
            }

            settings.orphaned = orphaned;

            if (settings.orphaned) {
                Log.w(TAG, "Found orphaned server settings for: " + settings.ID);
            }

            serverSettings.add(settings);
        }

        LinearLayout serverSettingsList = findViewById(R.id.server_settings_list);
        serverSettingsList.removeAllViews();

        int i = -1;
        for (ServerSettings settings : serverSettings) {
            ++i;

            View layout = View.inflate(this, R.layout.server_settings_item, null);
            TextView nameView = layout.findViewById(R.id.server_settings_name);
            TextView idView = layout.findViewById(R.id.server_settings_id);
            ImageView imageView = layout.findViewById(R.id.server_game_icon);
            Button deleteView = layout.findViewById(R.id.server_settings_delete);

            nameView.setText(settings.name);
            idView.setText(settings.ID);

            deleteView.setOnClickListener(view -> {
                AppSync.showConfirmationDialog(view.getContext(), "Confirm Deletion", "Are you sure?", true, () -> {
                    AppSync.settings.serverSettings.remove(AppSync.serverSettings(settings.ID));
                    AppSync.saveSettings();

                    serverSettingsList.removeView(layout);
                }, null);
            });

            if (settings.orphaned) {
                if (i % 2 == 0) {
                    layout.setBackgroundColor(getResources().getColor(R.color.dark_red));
                } else {
                    layout.setBackgroundColor(getResources().getColor(R.color.red));
                }
            } else {
                for (RenegadeServer renegadeServer : AppSync.interfaceServerList) {
                    if (settings.ID.equals(AppSync.serverUID(renegadeServer))) {
                        imageView.setImageResource(AppSync.game_icon(renegadeServer.game));
                        break;
                    }
                }

                if (i % 2 == 1) {
                    layout.setBackgroundColor(getResources().getColor(R.color.odd));
                } else {
                    layout.setBackgroundColor(getResources().getColor(R.color.charcoal));
                }
            }

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ServerSettingsActivity.class);
                    intent.putExtra("server_id", settings.ID);

                    startActivity(intent);
                }
            });

            serverSettingsList.addView(layout);
        }
    }
}