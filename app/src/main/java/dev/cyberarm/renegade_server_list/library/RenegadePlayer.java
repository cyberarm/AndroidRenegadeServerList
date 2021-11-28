package dev.cyberarm.renegade_server_list.library;

public class RenegadePlayer {
    public String team, name;
    public int score, kills, deaths, ping;

    public RenegadePlayer(String name, String team, int score, int kills, int deaths, int ping) {
        this.name = name;
        this.team = team;
        this.score = score;
        this.kills = kills;
        this.deaths = deaths;
        this.ping = ping;
    }
}
