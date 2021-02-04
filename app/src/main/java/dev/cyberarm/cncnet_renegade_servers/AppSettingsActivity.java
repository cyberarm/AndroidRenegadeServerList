package dev.cyberarm.cncnet_renegade_servers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.cyberarm.cncnet_renegade_servers.library.AppSync;

public class AppSettingsActivity extends AppCompatActivity {
    Button cncNetWebsite;
    EditText renegadeUsername;
    TextView autoRefreshInterval;
    ToggleButton serviceAutoStart;

    TextView notifyPlayerCount;
    TextView notifyMaps;
    TextView notifyUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings_activity);

        getSupportActionBar().setTitle("App Settings");

        cncNetWebsite = findViewById(R.id.cncnet_website);
        cncNetWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cncnet.org/renegade"));
                startActivity(browserIntent);
            }
        });

        renegadeUsername = findViewById(R.id.username);
        autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        serviceAutoStart = findViewById(R.id.run_at_startup);

        notifyPlayerCount = findViewById(R.id.server_player_count);
        notifyMaps = findViewById(R.id.server_mapnames);
        notifyUsernames = findViewById(R.id.server_usernames);

        loadSettings();
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