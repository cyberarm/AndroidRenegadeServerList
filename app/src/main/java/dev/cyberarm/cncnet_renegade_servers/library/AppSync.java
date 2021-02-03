package dev.cyberarm.cncnet_renegade_servers.library;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppSync {
    static final public String ENDPOINT = "https://api.cncnet.org/renegade?timeleft=&_players=1&website=";
    private static final String TAG = "AppSync";
    private static boolean lockNetwork = false;
    public static ArrayList<RenegadeServer> serverList;
    private static long lastSuccessfulFetch = 0;
    private static long softFetchLimit = 30_000; // milliseconds

    public static void setServerList(RenegadeServer[] serverList) {
        List<RenegadeServer> list = Arrays.asList(serverList);
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
                .serializeNulls()
                .create();
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
}
