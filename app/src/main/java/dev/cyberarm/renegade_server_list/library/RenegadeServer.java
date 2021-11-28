package dev.cyberarm.renegade_server_list.library;

import java.util.ArrayList;

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
