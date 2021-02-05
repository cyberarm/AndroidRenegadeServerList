package dev.cyberarm.cncnet_renegade_servers.library;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.cyberarm.cncnet_renegade_servers.serializers.RenegadePlayerDeserializer;
import dev.cyberarm.cncnet_renegade_servers.serializers.RenegadeServerDeserializer;
import dev.cyberarm.cncnet_renegade_servers.serializers.ServerSettingsDeserializer;
import dev.cyberarm.cncnet_renegade_servers.serializers.ServerSettingsSerializer;
import dev.cyberarm.cncnet_renegade_servers.serializers.SettingsDeserializer;
import dev.cyberarm.cncnet_renegade_servers.serializers.SettingsSerializer;

public class AppSync {
    static final public String ENDPOINT = "https://api.cncnet.org/renegade?timeleft=&_players=1&website=";
    private static final String TAG = "AppSync";
    private static final String VERSION = "0.1.0";
    private static final String USER_AGENT = String.format("CyberarmRenegadeServerList/%s (cyberarm.dev)", VERSION);
    private static boolean lockNetwork = false;
    private static long lastSuccessfulFetch = 0;
    public  static final long softFetchLimit = 30_000; // milliseconds

    public static ArrayList<RenegadeServer> serverList;
    public static ArrayList<RenegadeServer> lastServerList;
    public static Settings settings;
    public static boolean appInitialized = false;
    private static String storageLocation;
    private static String configFilePath;

    public static void initialize(File storageLocation) {
        if (appInitialized) {
            throw(new RuntimeException("AppSync all ready initialized!"));
        }

        AppSync.storageLocation = storageLocation.getPath();
        AppSync.configFilePath = storageLocation + File.pathSeparator + "settings.json";
        appInitialized = true;

        loadSettings();
    }

    public static String getConfigFilePath() {
        return configFilePath;
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, RenegadeServerListService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, RenegadeServerListService.class);
        context.stopService(intent);
    }

    private static void loadSettings() {
        // TODO: Load settings file if exists
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            settings = gson().fromJson(readFromFile(configFile.getPath()), Settings.class);
        } else {
            settings = new Settings("", 0, false,
                                    new ServerSettings("", 0, new ArrayList<>(), new ArrayList<>()),
                                    new ArrayList<>());
            saveSettings();
        }
    }

    public static boolean saveSettings() {
        return writeToFile(configFilePath, gson().toJson(settings, Settings.class));
    }

    public static void setServerList(RenegadeServer[] serverList) {
        List<RenegadeServer> list = Arrays.asList(serverList);
        if (AppSync.serverList != null) {
            AppSync.lastServerList = AppSync.serverList;
        }
        AppSync.serverList = new ArrayList<>(list);

        Collections.sort(AppSync.serverList, new Comparator<RenegadeServer>() {
            @Override
            public int compare(RenegadeServer a, RenegadeServer b) {
                return Integer.compare(b.numplayers, a.numplayers);
            }
        });
    }

    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(RenegadeServer.class, new RenegadeServerDeserializer())
                .registerTypeAdapter(RenegadePlayer.class, new RenegadePlayerDeserializer())
                .registerTypeAdapter(ServerSettings.class, new ServerSettingsSerializer())
                .registerTypeAdapter(ServerSettings.class, new ServerSettingsDeserializer())
                .registerTypeAdapter(Settings.class, new SettingsSerializer())
                .registerTypeAdapter(Settings.class, new SettingsDeserializer())
                .serializeNulls()
                .create();
    }

    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void fetchList(Callback callback, boolean forceFetch) {
        if (System.currentTimeMillis() - lastSuccessfulFetch >= softFetchLimit || forceFetch) {
        } else {
            Log.i(TAG, "Skipping fetch.");
            if (serverList.size() > 0) {
                callback.run();
            }
            return;
        }

        if (lockNetwork) {
            return;
        }

        lockNetwork = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;

                try {
                    URL url = new URL(ENDPOINT);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", USER_AGENT);

                    InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStream);
                    StringBuilder stringBuffer = new StringBuilder();

                    String output;
                    while ((output = reader.readLine()) != null) {
                        stringBuffer.append(output);
                    }

                    System.out.println(stringBuffer.toString());

                    RenegadeServer[] serverList = gson().fromJson(stringBuffer.toString(), RenegadeServer[].class);

                    AppSync.setServerList(serverList);
                    lastSuccessfulFetch = System.currentTimeMillis();
                    callback.run();
                } catch (IOException e) {
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    lockNetwork = false;
                }
            }
        }).start();
    }

    public static String readFromFile(String path) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader( new FileReader(path) );
            String line;

            while((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }

            br.close();
        } catch (IOException e) {
            return null;
        }

        return text.toString();
    }

    public static boolean writeToFile(String filePath, String content) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(content);
            writer.close();

            return true;

        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return false;
        }
    }

    public static ServerSettings serverSettings(String serverID) {
        for (ServerSettings serverSettings : settings.serverSettings) {
            if (serverSettings.ID.equals(serverID)) {
                return serverSettings;
            }
        }

        ServerSettings serverSettings = new ServerSettings(serverID, 0, new ArrayList<>(), new ArrayList<>());
        settings.serverSettings.add(serverSettings);

        return serverSettings;
    }
}
