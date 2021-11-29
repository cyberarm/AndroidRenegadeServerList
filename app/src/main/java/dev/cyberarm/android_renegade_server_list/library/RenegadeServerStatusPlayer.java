package dev.cyberarm.android_renegade_server_list.library;

public class RenegadeServerStatusPlayer {
    public String nick;
    public int team, score, kills, deaths;

    // nick, team (index of teams array), score, kills, deaths
    public RenegadeServerStatusPlayer(String nick, int team, int score, int kills, int deaths) {
        this.nick = nick;
        this.team = team;
        this.score = score;
        this.kills = kills;
        this.deaths = deaths;
    }
}
