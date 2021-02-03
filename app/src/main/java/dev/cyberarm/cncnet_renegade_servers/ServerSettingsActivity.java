package dev.cyberarm.cncnet_renegade_servers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import dev.cyberarm.cncnet_renegade_servers.library.AppSync;
import dev.cyberarm.cncnet_renegade_servers.library.ServerSettings;

public class ServerSettingsActivity extends AppCompatActivity {

    private String serverID;
    ServerSettings serverSettings;

    TextView notifyPlayerCount;
    TextView notifyMaps;
    TextView notifyUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        serverID = getIntent().getStringExtra("server_id");
        serverSettings = AppSync.serverSettings(serverID);

        notifyPlayerCount = findViewById(R.id.server_player_count);
        notifyMaps = findViewById(R.id.server_mapnames);
        notifyUsernames = findViewById(R.id.server_usernames);

        loadSettings();
    }

    private void loadSettings() {
        notifyPlayerCount.setText("" + serverSettings.notifyPlayerCount);

        notifyMaps.setText(String.join(", ", serverSettings.notifyMapNames));
        notifyUsernames.setText(String.join(", ", serverSettings.notifyUsernames));
    }

    private void saveSettings() {
        serverSettings.notifyPlayerCount = Integer.parseInt(notifyPlayerCount.getText().toString());

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