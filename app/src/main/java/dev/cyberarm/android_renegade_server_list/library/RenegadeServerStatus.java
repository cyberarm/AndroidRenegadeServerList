package dev.cyberarm.android_renegade_server_list.library;

import java.util.ArrayList;

public class RenegadeServerStatus {
    public String name, map, remaining;
    public int numPlayers, maxPlayers;
    public String started;
    public boolean password;

    public ArrayList<RenegadeServerStatusTeam> teams;
    public ArrayList<RenegadeServerStatusPlayer> players;

    // name, map, maxplayers, numplayers, started (DateTime), and remaining (RenTime)

    public RenegadeServerStatus(String name, String map, String remaining, int numPlayers, int maxPlayers, String started, boolean password, ArrayList<RenegadeServerStatusTeam> teams, ArrayList<RenegadeServerStatusPlayer> players) {
        this.name = name;
        this.map = map;
        this.remaining = remaining;
        this.numPlayers = numPlayers;
        this.maxPlayers = maxPlayers;
        this.password = password;
        this.started = started;

        this.teams = teams;
        this.players = players;
    }
}
