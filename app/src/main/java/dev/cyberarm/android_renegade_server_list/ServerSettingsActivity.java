package dev.cyberarm.android_renegade_server_list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import dev.cyberarm.android_renegade_server_list.library.AppSync;
import dev.cyberarm.android_renegade_server_list.library.ServerSettings;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class ServerSettingsActivity extends AppCompatActivity {

    private String serverID;
    ServerSettings serverSettings;

    TextView notifyPlayerCount;
    TextView notifyMaps;
    TextView notifyUsernames;

    ToggleButton notifyRequireMultipleConditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        serverID = getIntent().getStringExtra("server_id");
        serverSettings = AppSync.serverSettings(serverID);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Server Settings");
        }

        notifyPlayerCount = findViewById(R.id.server_player_count);
        notifyMaps = findViewById(R.id.server_mapnames);
        notifyUsernames = findViewById(R.id.server_usernames);

        notifyRequireMultipleConditions = findViewById(R.id.server_require_multiple_conditions);

        loadSettings();
    }

    private void loadSettings() {
        notifyPlayerCount.setText(String.format("%d", serverSettings.notifyPlayerCount));

        String notifyMapsString = StreamSupport.stream(serverSettings.notifyMapNames)
                .collect(Collectors.joining(", "));
        String notifyUsernamesString = StreamSupport.stream(serverSettings.notifyUsernames)
                .collect(Collectors.joining(", "));
        notifyMaps.setText(notifyMapsString);
        notifyUsernames.setText(notifyUsernamesString);
        notifyRequireMultipleConditions.setChecked(serverSettings.notifyRequireMultipleConditions);
    }

    private void saveSettings() {
        String notifyPlayerCountValue = notifyPlayerCount.getText().toString().length() == 0 ? "0" : notifyPlayerCount.getText().toString();
        serverSettings.notifyPlayerCount = Integer.parseInt(notifyPlayerCountValue);
        serverSettings.notifyRequireMultipleConditions = notifyRequireMultipleConditions.isChecked();

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

        serverSettings.notifyMapNames = mapsList;
        serverSettings.notifyUsernames = usernamesList;

        AppSync.saveSettings();
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