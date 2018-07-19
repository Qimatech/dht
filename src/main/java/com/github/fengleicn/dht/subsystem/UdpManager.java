package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.bencode.Bcd;
import com.github.fengleicn.dht.node.Node;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.utils.KademliaBucket;
import com.github.fengleicn.dht.utils.DhtUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpManager {
    public static Map<String, Integer> get = new ConcurrentHashMap<>();
    public static Map<String, Integer> announce = new ConcurrentHashMap<>();

    public KademliaBucket kademliaBucket;

    public UdpManager(KademliaBucket kademliaBucket) {
        this.kademliaBucket = kademliaBucket;
    }

    public UdpPacket manage(UdpPacket p, byte[] localNodeId) {
        try {
            Bcd b = p.bcd;
            InetSocketAddress remoteSocketAddress = p.address;
            if (b.get("y").castB()[0] == 'q') {
                String q = new String(b.get("q").castB(), Bcd.DEFAULT_CHARSER);
                switch (q) {
                    case "ping":
                        byte[] transId = b.get("t").cast();
                        return DhtUtil.rspPing(transId, localNodeId, remoteSocketAddress);
                    case "find_node":
                        transId = b.get("t").cast();
                        byte[] target = b.get("a").get("target").cast();
                        List<Node> nodes = kademliaBucket.get(new Node(target, null, null));
                        return DhtUtil.rspFindNode(transId, localNodeId, nodes, remoteSocketAddress);
                    case "get_peers":
                        byte[] bytes = b.get("a").get("info_hash").cast();
                        StringBuilder sb = new StringBuilder();
                        for (byte a : bytes) {
                            sb.append(String.format("%02X", a));
                        }
                        save(b, BtLibrary.GET);
                        break;
                    case "announce_peer":
                        transId = b.get("t").castB();
                        save(b, BtLibrary.ANNOUNCE);
                        return DhtUtil.rspAnnouncePeer(transId, localNodeId, remoteSocketAddress); //TODO check token
                }
            } else {
                if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'p', 'g'})) {
                    //ping
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'f', 'n'})) {
                    //find node
                    List<Node> nodes = DhtUtil.decodeNodes(b.get("r").get("nodes").castB());
                    for (Node node : nodes)
                        kademliaBucket.add(node);
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'g', 'p'})) {
                    //get peer
                } else if (DhtUtil.byteArraysEqual(b.get("t").castB(), new byte[]{'a', 'p'})) {
                    //announce peer
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void save(Bcd recv, int type) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = recv.get("a").get("info_hash").castB();
        for (byte a : bytes) {
            sb.append(String.format("%02X", a));
        }
        BtLibrary.getInstence().addInfoHash(sb.toString(), type);
    }
}