package dev.cyberarm.cncnet_renegade_servers.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Callback implements Runnable {
    public ArrayList<RenegadeServer> serverList;

    public void setServerList(RenegadeServer[] serverList) {
        List<RenegadeServer> list = Arrays.asList(serverList);
        this.serverList = new ArrayList<>(list);

        Collections.sort(this.serverList, new Comparator<RenegadeServer>() {
            @Override
            public int compare(RenegadeServer a, RenegadeServer b) {
                return Integer.compare(b.numplayers, a.numplayers);
            }
        });
    }
    @Override
    public void run() {

    }
}
