package dev.cyberarm.renegade_server_list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import dev.cyberarm.renegade_server_list.library.AppSync;
import dev.cyberarm.renegade_server_list.library.Settings;

public class AppSettingsActivity extends AppCompatActivity {
    private static final int EXPORT_FILE = 1;
    private static final int IMPORT_FILE = 2;
    private static final String TAG = "AppSettingsActivity";
    private static final String DEFAULT_SETTINGS_NAME = "rensrvlist_settings.json";
    private static final String MIME_TYPE = "application/json";
    Button cncNetWebsite;
    EditText renegadeUsername;
    TextView autoRefreshInterval;
    ToggleButton serviceAutoStart;

    TextView notifyPlayerCount;
    TextView notifyMaps;
    TextView notifyUsernames;

    Button settingsExport, settingsImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings_activity);

        getSupportActionBar().setTitle("App Settings");

        cncNetWebsite = findViewById(R.id.w3dhub_website);
        cncNetWebsite.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://w3dhub.com"));
            startActivity(browserIntent);
        });

        renegadeUsername = findViewById(R.id.username);
        autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        serviceAutoStart = findViewById(R.id.run_at_startup);

        notifyPlayerCount = findViewById(R.id.server_player_count);
        notifyMaps = findViewById(R.id.server_mapnames);
        notifyUsernames = findViewById(R.id.server_usernames);

        settingsExport = findViewById(R.id.settings_export);
        settingsImport = findViewById(R.id.settings_import);

        settingsExport.setOnClickListener(view -> {
            exportSettings();
        });
        settingsImport.setOnClickListener(view -> {
            importSettings();
        });

        loadSettings();
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
        autoRefreshInterval.setText("" + AppSync.settings.serviceAutoRefreshInterval);
        serviceAutoStart.setChecked(AppSync.settings.serviceAutoStartAtBoot);

        notifyPlayerCount.setText("" + AppSync.settings.globalServerSettings.notifyPlayerCount);

        // TODO: Fix String.join as is Oreo method... 😤😤
        notifyMaps.setText(String.join(", ", AppSync.settings.globalServerSettings.notifyMapNames));
        notifyUsernames.setText(String.join(", ", AppSync.settings.globalServerSettings.notifyUsernames));
    }

    private void saveSettings() {
        AppSync.settings.renegadeUsername = renegadeUsername.getText().toString();
        AppSync.settings.serviceAutoRefreshInterval = Integer.parseInt(autoRefreshInterval.getText().toString());
        AppSync.settings.serviceAutoStartAtBoot = serviceAutoStart.isChecked();

        AppSync.settings.globalServerSettings.notifyPlayerCount = Integer.parseInt(notifyPlayerCount.getText().toString());

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
            AppSync.startService(this);
        } else {
            AppSync.stopService(this);
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
}