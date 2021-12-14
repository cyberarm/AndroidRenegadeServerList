package dev.cyberarm.android_renegade_server_list.library;

import java.util.ArrayList;

public class Settings {
    public String renegadeUsername;
    public int serviceAutoRefreshInterval;
    public boolean serviceAutoStartAtBoot;
    public boolean refreshOnMeteredConnections;
    public ServerSettings globalServerSettings;
    public ArrayList<ServerSettings> serverSettings;

    public Settings(String renegadeUsername, int serviceAutoRefreshInterval, boolean serviceAutoStartAtBoot, boolean refreshOnMeteredConnections, ServerSettings globalServerSettings, ArrayList<ServerSettings> serverSettings) {
        this.renegadeUsername = renegadeUsername;
        this.serviceAutoRefreshInterval = serviceAutoRefreshInterval;
        this.serviceAutoStartAtBoot = serviceAutoStartAtBoot;
        this.refreshOnMeteredConnections = refreshOnMeteredConnections;
        this.globalServerSettings = globalServerSettings;
        this.serverSettings = serverSettings;
    }
}
