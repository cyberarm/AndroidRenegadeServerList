package dev.cyberarm.cncnet_renegade_servers.library;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RenegadeServer {
    public String country, countrycode, ip, timeleft, hostname, mapname, website;
    public int numplayers, maxplayers;
    public boolean password = false;
    public ArrayList<RenegadePlayer> players;

    public RenegadeServer(String country, String countrycode, String timeleft, String ip, String hostname,
                          String mapname, String website, int numplayers, int maxplayers,
                          boolean password, ArrayList<RenegadePlayer> players) {
        this.country = country;
        this.countrycode = countrycode;
        this.timeleft = timeleft;
        this.ip = ip;
        this.hostname = hostname;
        this.mapname = mapname;
        this.website = website;
        this.numplayers = numplayers;
        this.maxplayers = maxplayers;
        this.password = password;

        this.players = players;
    }
}
