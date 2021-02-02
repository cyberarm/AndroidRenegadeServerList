package dev.cyberarm.cncnet_renegade_servers.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class AppSync {
    static final public String ENDPOINT = "https://api.cncnet.org/renegade?timeleft=&_players=1&website=";
    private static boolean lockNetwork = false;

    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(RenegadeServer.class, new RenegadeServerDeserializer())
                .registerTypeAdapter(RenegadePlayer.class, new RenegadePlayerDeserializer())
                .serializeNulls()
                .create();
    }

    public static void fetchList(Callback callback) {
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
                    StringBuffer stringBuffer = new StringBuffer();

                    String output;
                    while ((output = reader.readLine()) != null) {
                        stringBuffer.append(output);
                    }

                    System.out.println(stringBuffer.toString());

                    RenegadeServer[] serverList = gson().fromJson(stringBuffer.toString(), RenegadeServer[].class);

                    callback.setServerList(serverList);
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
