package dev.cyberarm.android_renegade_server_list.library;

public class RenegadeServer {
    public String id, game, address, region;
    public int port;
    public RenegadeServerStatus status;

    public RenegadeServer(String id, String game, String address, int port, String region, RenegadeServerStatus status) {
        this.id = id;
        this.game = game;
        this.address = address;
        this.port = port;
        this.region = region;

        this.status = status;
    }
}
