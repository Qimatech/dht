package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.starter.BtInfoFinder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BtLibrary {
    private volatile static BtLibrary instance;
    public static final int ANNOUNCE = 1;
    public static final int GET = 2;
    public static PrintWriter result;

    static {
        try {
            result = new PrintWriter(new FileWriter("result.txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Torrent {
        String content;
        int weight;
    }

    Map<String, Torrent> torrentMap = new ConcurrentHashMap<>();

    public static BtLibrary getInstence() {
        if (instance == null) {
            synchronized (BtLibrary.class) {
                if (instance == null) {
                    instance = new BtLibrary();
                    new Thread(() -> {
                        instance.downloadMetadata();
                    }).start();
                }
            }
        }
        return instance;
    }

    private BtLibrary() {
    }

    public synchronized void addInfoHash(String infoHash, int type) {
        int weight;
        switch (type) {
            case ANNOUNCE:
                weight = 50;
                break;
            case GET:
                weight = 1;
                break;
            default:
                throw new RuntimeException("type error");
        }
        Torrent torrent = torrentMap.get(infoHash);
        if (torrent == null) {
            torrent = new Torrent();
            torrent.weight = weight;
        } else {
            torrent.weight += weight;
        }
        torrentMap.put(infoHash, torrent);
    }

    public void downloadMetadata() {

        while (true) {
            try {
                Thread.sleep(500);
                if (torrentMap.size() > 1000) {
                    synchronized (this) {
                        torrentMap.clear();
                    }
                    continue;
                }
                if (torrentMap.entrySet().isEmpty()) {
                    continue;
                }
                List<Map.Entry<String, Torrent>> l = new ArrayList<>(torrentMap.entrySet());
                l.sort((o1, o2) -> {
                    if (o1.getValue().content != null && o2.getValue().content != null) {
                        return 0;
                    } else if (o1.getValue().content != null) {
                        return 1;
                    } else if (o2.getValue().content != null) {
                        return -1;
                    } else {
                        return o2.getValue().weight - o1.getValue().weight;
                    }
                });
                l.get(0).getValue().weight -= 100;
                new Thread(() -> {
                    try {
                        BtTracker.request(l.get(0).getKey());
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void recordMetaData(String infoHash, String data) {
        synchronized(this) {
            if(infoHash.equalsIgnoreCase(BtInfoFinder.GET_PEER_INFO_HASH))
                return;
            Torrent torrent = torrentMap.get(infoHash);
            if (!data.equals(torrent.content)) {
                result.write(new Date().toString() + " " + infoHash + "\t" + data + "\n");
                result.flush();
                torrent.content = data;
            }
        }
    }
}
