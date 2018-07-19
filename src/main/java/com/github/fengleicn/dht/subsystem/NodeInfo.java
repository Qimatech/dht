package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.node.Node;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.utils.KademliaBucket;
import com.github.fengleicn.dht.utils.DhtUtil;

import java.net.InetSocketAddress;


public class NodeInfo {

    static KademliaBucket kademliaBucket;

    public NodeInfo(KademliaBucket kademliaBucket) {
        this.kademliaBucket = kademliaBucket;
    }

    public static Node getRandomNode() throws Exception {
        return kademliaBucket.getRandom();
    }

    public UdpPacket getRandomFindNodePacket(byte[] transId, Node localNode, byte[] targetId) throws Exception {
        Node node = kademliaBucket.getRandom();
        return DhtUtil.findNode(transId, localNode.nodeId, targetId, new InetSocketAddress(node.getIp(), node.getPort()));
    }
}
