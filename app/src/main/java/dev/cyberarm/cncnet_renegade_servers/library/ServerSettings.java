package dev.cyberarm.cncnet_renegade_servers.library;

import java.util.ArrayList;

public class ServerSettings {
    public String ID;
    public int notifyPlayerCount;
    public ArrayList<String> notifyMapNames, notifyUsernames;

    public ServerSettings(String ID, int notifyPlayerCount, ArrayList<String> notifyMapNames, ArrayList<String> notifyUsernames) {
        this.ID = ID;
        this.notifyPlayerCount = notifyPlayerCount;
        this.notifyMapNames = notifyMapNames;
        this.notifyUsernames = notifyUsernames;
    }
}