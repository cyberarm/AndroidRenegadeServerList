package dev.cyberarm.cncnet_renegade_servers.library;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RenegadeServer {
    public String country, countrycode, ip, timeleft, hostname, mapname, website;
    public int players, maxPlayers;
    public boolean password = false;
    public ArrayList<RenegadePlayer> playerList;

    public RenegadeServer(String country, String countrycode, String timeleft, String hostname,
                          String mapname, String website, int players, int maxPlayers,
                          boolean password, ArrayList<RenegadePlayer> playerList) {
        this.country = country;
        this.countrycode = countrycode;
        this.timeleft = timeleft;
        this.hostname = hostname;
        this.mapname = mapname;
        this.website = website;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.password = password;

        this.playerList = playerList;
    }
}
