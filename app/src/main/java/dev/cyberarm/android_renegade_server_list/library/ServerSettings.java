package dev.cyberarm.android_renegade_server_list.library;

import java.util.ArrayList;

public class ServerSettings {
    public String ID, name;
    public int notifyPlayerCount;
    public ArrayList<String> notifyMapNames, notifyUsernames;
    public boolean notifyRequireMultipleConditions;
    public boolean orphaned = false; // Used internally for checking for orphaned server settings, not saved to JSON

    public ServerSettings(String ID, String name, int notifyPlayerCount, ArrayList<String> notifyMapNames, ArrayList<String> notifyUsernames, boolean notifyRequireMultipleConditions) {
        this.ID = ID;
        this.name = name;
        this.notifyPlayerCount = notifyPlayerCount;
        this.notifyMapNames = notifyMapNames;
        this.notifyUsernames = notifyUsernames;
        this.notifyRequireMultipleConditions = notifyRequireMultipleConditions;
    }
}
