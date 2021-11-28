package dev.cyberarm.renegade_server_list.library;

public class RenegadeServerStatusTeam {
    public String name;
    public int id, score, kills, deaths;

    public RenegadeServerStatusTeam(int id, String name, int score, int kills, int deaths) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.kills = kills;
        this.deaths = deaths;
    }
}
