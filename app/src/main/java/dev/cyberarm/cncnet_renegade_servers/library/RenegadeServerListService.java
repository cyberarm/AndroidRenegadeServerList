package dev.cyberarm.cncnet_renegade_servers.library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import dev.cyberarm.cncnet_renegade_servers.MainActivity;
import dev.cyberarm.cncnet_renegade_servers.R;

public class RenegadeServerListService extends Service {
    private static final String TAG = "ServerListService";
    private static final String CHANNEL_ID = "Renegade Server List Quite";
    private static final String CHANNEL_ID_NOISY = "Renegade Server List Noisy";
    private static final int ID = 2002_02_27;
    private static final int ID_B = 2021_02_27;
    private long lastTrigger;
    private boolean runService;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lastTrigger = 0;

        if (!AppSync.appInitialized) {
            AppSync.initialize(getFilesDir());
        }

        createNotificationChannels();

        foregroundify();

        runService = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                runUpdater();
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        runService = false;
    }

    private void foregroundify() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Renegade Server List")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();

        startForeground(ID, notification);
    }

    private void createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription("");

            int importance_b = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel_b = new NotificationChannel(CHANNEL_ID_NOISY, CHANNEL_ID_NOISY, importance_b);
            channel_b.setDescription("");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channel_b);
        }
    }

    private void runUpdater() {
        while(runService) {
            if (AppSync.settings.serviceAutoRefreshInterval > 0) {
                if (System.currentTimeMillis() - lastTrigger > Math.max(AppSync.softFetchLimit, AppSync.settings.serviceAutoRefreshInterval * 1000.0)) {
                    lastTrigger = System.currentTimeMillis();

                    AppSync.fetchList(new Callback() {
                        @Override
                        public void run() {
                            evaluate();
                        }
                    }, true);
                }
            }

            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void evaluate() {
        StringBuilder message = new StringBuilder();
        RenegadeServer oldDataServer = new RenegadeServer("Earth", "EA", "0.0.0", "",
                0, "", "", "", 0, 0, false, new ArrayList<RenegadePlayer>());

        for (RenegadeServer server : AppSync.serverList) {
            if (AppSync.lastServerList != null) {
                for (RenegadeServer oldServer : AppSync.lastServerList) {
                    if ((server.hostname + "" + server.hostport).equals(oldServer.hostname + "" + oldServer.hostport)) {
                        oldDataServer = oldServer;
                        break;
                    }
                }
            }

            ServerSettings serverSettings = AppSync.serverSettings(server.ip + "" + server.hostport);
            int notifyPlayerCount = AppSync.settings.globalServerSettings.notifyPlayerCount;
            ArrayList<String> mapnames = AppSync.settings.globalServerSettings.notifyMapNames;
            ArrayList<String> usernames = AppSync.settings.globalServerSettings.notifyUsernames;

            // Skip if user is in-game
            if (usernames.stream().anyMatch(obj -> obj.equals(AppSync.settings.renegadeUsername))) {
                continue;
            }

            if (serverSettings.notifyPlayerCount > 0) {
                notifyPlayerCount = serverSettings.notifyPlayerCount;
            }

            if (serverSettings.notifyMapNames.size() > 0) {
                Set<String> set = new LinkedHashSet<>(mapnames);
                set.addAll(serverSettings.notifyMapNames);
                mapnames = new ArrayList<>(set);
            }

            if (serverSettings.notifyUsernames.size() > 0) {
                Set<String> set = new LinkedHashSet<>(usernames);
                set.addAll(serverSettings.notifyUsernames);
                usernames = new ArrayList<>(set);
            }

            if (server.numplayers > 0 && server.numplayers >= notifyPlayerCount && server.numplayers > oldDataServer.numplayers) {
                message.append(server.hostname + " has " + server.numplayers + " players\n");
            }

            if (!server.mapname.equals(oldDataServer.mapname)) {
                for(String mapname : mapnames) {

                    if (server.mapname.contains(mapname)) {
                        message.append(server.hostname + " current map: " + server.mapname + "\n");
                        break;
                    }
                }
            }

            ArrayList<String> joinedPlayers = server.players.stream().map(obj -> obj.name).collect(Collectors.toCollection(ArrayList::new));

            for (RenegadePlayer player : oldDataServer.players) {
               joinedPlayers.removeIf(obj -> obj.equals(player.name));
            }

            joinedPlayers.removeIf(obj -> obj.toLowerCase().equals("gdi") || obj.toLowerCase().equals("nod"));

            for (String player : joinedPlayers) {
                if (usernames.contains(player)) {
                    message.append(server.hostname + " player joined: " + player + "\n");
                }
            }
        }

        String notificationBody = message.toString().trim();

        if (notificationBody.length() > 0) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_NOISY)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Renegade Server List")
                    .setContentText(notificationBody)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationBody))
                    .setAutoCancel(true);

            NotificationManagerCompat.from(this).notify(ID_B, builder.build());

        }
    }
}