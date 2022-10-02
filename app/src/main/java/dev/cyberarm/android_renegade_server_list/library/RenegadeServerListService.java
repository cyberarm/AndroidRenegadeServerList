package dev.cyberarm.android_renegade_server_list.library;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java8.util.stream.StreamSupport;
import java8.util.stream.Collectors;
import java8.lang.Iterables;

import dev.cyberarm.android_renegade_server_list.MainActivity;
import dev.cyberarm.android_renegade_server_list.R;

public class RenegadeServerListService extends Service {
    private static final String TAG = "ServerListService";
    private static final String CHANNEL_ID = "Renegade Server List Quite";
    private static final String CHANNEL_ID_NOISY = "Renegade Server List Noisy";
    private static final int ID = 2002_02_27;
    private static final int ID_B = 2021_02_27;
    private long lastTrigger;
    private boolean runService;
    private long lastUpdatedServiceNotification = 0;
    private long lastDeliveredNotification = 0;

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

        new Thread(this::runUpdater).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        runService = false;
    }

    private void foregroundify() {
        startForeground(ID, createServiceNotification(0, 0));
    }

    private Notification createServiceNotification(int totalServerCount, int totalPlayerCount) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Renegade Server List")
                .setContentText(String.format(Locale.US, "Servers: %d   Players: %d", totalServerCount, totalPlayerCount))
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true);

        return builder.build();
    }

    private void createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription("Used for creating foreground server notification icon");

            int importance_b = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel_b = new NotificationChannel(CHANNEL_ID_NOISY, CHANNEL_ID_NOISY, importance_b);
            channel_b.setDescription("Used for notifying server changes");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channel_b);
        }
    }

    private void runUpdater() {
        while(runService) {
            if (AppSync.getLastInterfaceServerListUpdate() - lastUpdatedServiceNotification >= 1) {
                updateServiceNotification();
            }

            if (AppSync.settings.serviceAutoRefreshInterval > 0) {
                if (System.currentTimeMillis() - lastTrigger > Math.max(AppSync.softFetchLimit, AppSync.settings.serviceAutoRefreshInterval * 1000.0)) {
                    lastTrigger = System.currentTimeMillis();

                    AppSync.fetchList(getApplicationContext(), new Callback() {
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

        for (RenegadeServer server : AppSync.serverList) {
            RenegadeServer oldDataServer = null;

            if (AppSync.lastServerList != null) {
                for (RenegadeServer oldServer : AppSync.lastServerList) {
                    if (AppSync.serverUID(server).equals(AppSync.serverUID(oldServer))) {
                        oldDataServer = oldServer;
                        break;
                    }
                }
            }

            if (oldDataServer == null) { continue;}

            ServerSettings serverSettings = AppSync.serverSettings(AppSync.serverUID(server));
            int notifyPlayerCount = AppSync.settings.globalServerSettings.notifyPlayerCount;
            ArrayList<String> mapnames = AppSync.settings.globalServerSettings.notifyMapNames;
            ArrayList<String> usernames = AppSync.settings.globalServerSettings.notifyUsernames;

            boolean checkedPlayerHasJoined = false, checkedPlayerCountMeant = false, checkedServerMapActive = false;

            StringBuilder serverMessage = new StringBuilder();

            // Skip if user is in-game
            if (StreamSupport.stream(server.status.players).map(obj -> obj.nick).anyMatch(obj -> obj.equals(AppSync.settings.renegadeUsername))) {
                Log.i(TAG, "Skipping notification processing for server '" + server.status.name + "' since '" + AppSync.settings.renegadeUsername + "' is in-game.");
                continue;
            }

            if (serverSettings != null) {
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
            }

            if (notifyPlayerCount != 0 && server.status.numPlayers > 0 && server.status.numPlayers >= notifyPlayerCount && server.status.numPlayers > oldDataServer.status.numPlayers) {
                checkedPlayerCountMeant = true;
                serverMessage.append(server.status.name).append(" has ").append(server.status.numPlayers).append(" players\n");
            }

            if (!server.status.map.equals(oldDataServer.status.map)) {
                for(String mapname : mapnames) {
                    if (mapname.trim().length() == 0) {
                        continue;
                    }

                    if (server.status.map.contains(mapname)) {

                        checkedServerMapActive = true;
                        serverMessage.append(server.status.name).append(" current map: ").append(server.status.map).append("\n");
                        break;
                    }
                }
            }

            ArrayList<String> joinedPlayers = StreamSupport.stream(server.status.players).map(obj -> obj.nick).collect(Collectors.toCollection(ArrayList::new));

            for (RenegadeServerStatusPlayer player : oldDataServer.status.players) {
                Iterables.removeIf(joinedPlayers, obj -> obj.equals(player.nick));
            }

            Iterables.removeIf(joinedPlayers, obj -> obj.equalsIgnoreCase("gdi") || obj.equalsIgnoreCase("nod"));

            for (String player : joinedPlayers) {
                if (usernames.contains(player)) {
                    checkedPlayerHasJoined = true;
                    serverMessage.append(server.status.name).append(" player joined: ").append(player).append("\n");
                }
            }

            if (serverSettings != null ? serverSettings.notifyRequireMultipleConditions : AppSync.settings.globalServerSettings.notifyRequireMultipleConditions) {
                if (
                        (checkedPlayerHasJoined && checkedPlayerCountMeant) ||
                        (checkedServerMapActive && checkedPlayerCountMeant) ||
                        (checkedPlayerHasJoined && checkedServerMapActive)
                ) {
                    message.append(serverMessage.toString().trim());
                }
            } else {
                message.append(serverMessage.toString().trim());
            }
        }

        String notificationBody = message.toString().trim();

        if (notificationBody.length() > 0) {
            lastDeliveredNotification = System.currentTimeMillis();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_NOISY)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle("Renegade Server List")
                    .setContentText(notificationBody)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationBody))
                    .setAutoCancel(true);

            NotificationManagerCompat.from(this).notify(ID_B, builder.build());
        } else {
            // Remove notification if it has become stale
            if (System.currentTimeMillis() - lastDeliveredNotification >= 60_000 * 15) { // 15 minutes
                NotificationManagerCompat.from(this).cancel(ID_B);
            }
        }

        updateServiceNotification();
    }

    private void updateServiceNotification() {
        lastUpdatedServiceNotification = System.currentTimeMillis();

        int totalPlayerCount = StreamSupport.stream(AppSync.serverList).mapToInt(server -> server.status.players.size()).sum();
        int totalServerCount = AppSync.serverList.size();

        Log.i(TAG, "update service notification: servers: " + totalServerCount + ", players: " + totalPlayerCount);
        Notification notification = createServiceNotification(totalServerCount, totalPlayerCount);
        NotificationManagerCompat.from(this).notify(ID, notification);
    }
}