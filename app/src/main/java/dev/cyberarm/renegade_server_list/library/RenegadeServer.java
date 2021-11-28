package dev.cyberarm.renegade_server_list.library;

import java.util.ArrayList;

public class RenegadeServer {
    public String country, countrycode, ip, timeleft, hostname, mapname, website;
    public int numplayers, maxplayers, hostport;
    public boolean password = false;
    public ArrayList<RenegadePlayer> players;

    public RenegadeServer(String country, String countrycode, String timeleft, String ip, int hostport, String hostname,
                          String mapname, String website, int numplayers, int maxplayers,
                          boolean password, ArrayList<RenegadePlayer> players) {
        this.country = country;
        this.countrycode = countrycode;
        this.timeleft = timeleft;
        this.ip = ip;
        this.hostport = hostport;
        this.hostname = hostname;
        this.mapname = mapname;
        this.website = website;
        this.numplayers = numplayers;
        this.maxplayers = maxplayers;
        this.password = password;

        this.players = players;
    }
}
