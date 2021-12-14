package dev.cyberarm.android_renegade_server_list.library;

import java.util.ArrayList;

public class ServerSettings {
    public String ID;
    public int notifyPlayerCount;
    public ArrayList<String> notifyMapNames, notifyUsernames;
    public boolean notifyRequireMultipleConditions;

    public ServerSettings(String ID, int notifyPlayerCount, ArrayList<String> notifyMapNames, ArrayList<String> notifyUsernames, boolean notifyRequireMultipleConditions) {
        this.ID = ID;
        this.notifyPlayerCount = notifyPlayerCount;
        this.notifyMapNames = notifyMapNames;
        this.notifyUsernames = notifyUsernames;
        this.notifyRequireMultipleConditions = notifyRequireMultipleConditions;
    }
}
