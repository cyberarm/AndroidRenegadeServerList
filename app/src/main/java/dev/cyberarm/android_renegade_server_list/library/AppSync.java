package dev.cyberarm.android_renegade_server_list.library;

import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

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

import dev.cyberarm.android_renegade_server_list.R;
import dev.cyberarm.android_renegade_server_list.serializers.RenegadeServerStatusDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.RenegadeServerStatusPlayerDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.RenegadeServerDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.RenegadeServerStatusTeamDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.ServerSettingsDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.ServerSettingsSerializer;
import dev.cyberarm.android_renegade_server_list.serializers.SettingsDeserializer;
import dev.cyberarm.android_renegade_server_list.serializers.SettingsSerializer;

public class AppSync {
    static final public String ENDPOINT =  "https://gsh.w3dhub.com/listings/getAll/v2?statusLevel=2"; // "https://api.cncnet.org/renegade?timeleft=&_players=1&website=";
    private static final String TAG = "AppSync";
    private static final String VERSION = "0.1.0";
    private static final String USER_AGENT = String.format("Cyberarm's Renegade Server List/%s (cyberarm.dev)", VERSION);
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
            settings = new Settings("", 0, false, false,
                                    new ServerSettings("", 0, new ArrayList<>(), new ArrayList<>(), false),
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
                return Integer.compare(b.status.numPlayers, a.status.numPlayers);
            }
        });
    }

    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(RenegadeServer.class, new RenegadeServerDeserializer())
                .registerTypeAdapter(RenegadeServerStatus.class, new RenegadeServerStatusDeserializer())
                .registerTypeAdapter(RenegadeServerStatusTeam.class, new RenegadeServerStatusTeamDeserializer())
                .registerTypeAdapter(RenegadeServerStatusPlayer.class, new RenegadeServerStatusPlayerDeserializer())
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

    public static void fetchList(Context context, Callback callback, boolean forceFetch) {
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

        if (!AppSync.settings.refreshOnMeteredConnections) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.isActiveNetworkMetered()) {
                return;
            }
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

        ServerSettings serverSettings = new ServerSettings(serverID, 0, new ArrayList<>(), new ArrayList<>(), false);
        settings.serverSettings.add(serverSettings);

        return serverSettings;
    }

    public static int game_icon(String game) {
        switch (game) {
            case "ia":
                return R.drawable.ia_icon;
            case "tsr":
                return R.drawable.tsr_icon;
            case "apb":
                return R.drawable.apb_icon;
            case "ecw":
                return R.drawable.ecw_icon;
            case "ar":
                return R.drawable.ar_icon;
            case "bfd":
                return R.drawable.bfd_icon;
            case "gz":
                return R.drawable.gz_icon;
            default:
                return R.drawable.ren_icon;
        }
    }
}
