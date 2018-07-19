package com.github.fengleicn.dht.starter;

import com.github.fengleicn.dht.node.Node;
import com.github.fengleicn.dht.packet.UdpPacket;
import com.github.fengleicn.dht.subsystem.NodeInfo;
import com.github.fengleicn.dht.subsystem.UdpManager;
import com.github.fengleicn.dht.subsystem.UdpNetworkContoller;
import com.github.fengleicn.dht.utils.KademliaBucket;
import com.github.fengleicn.dht.utils.DhtUtil;

import java.net.*;
import java.util.Arrays;

public class BtInfoFinder {
    public static Node localNode;
    UdpNetworkContoller networkController;
    UdpManager udpManager;
    NodeInfo nodeInfo;
    int localPort;

    private static final int SEC = 1000;
    private static final int GET_PEER_MS = 5;
    private static final int FIND_NODE_MS = 5;
    private static final int CHANGE_NODE_ID_MS = 2 * SEC;
    public static final String GET_PEER_INFO_HASH = "EEB7C79987A49F3CA816A951C404350A83C23C3C";

    public BtInfoFinder(String ip, int port) throws SocketException, UnknownHostException {
        this.localPort = port;
        localNode = new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(ip), DhtUtil.portToByte(port));
        System.out.println("DEBUG ip: " + Arrays.toString(localNode.ip));
        networkController = new UdpNetworkContoller(new DatagramSocket(port));
        KademliaBucket kademliaBucket = new KademliaBucket(localNode);
        udpManager = new UdpManager(kademliaBucket);
        nodeInfo = new NodeInfo(kademliaBucket);
    }

    private void init() throws UnknownHostException {
        String urlA = "router.utorrent.com";
        String urlB = "router.bittorrent.com";
        String urlC = "dht.transmissionbt.com";
        String urlD = "112.10.240.235";
        String urlE = "72.178.134.65";
        String urlF = "121.239.242.138";
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlA), DhtUtil.portToByte(6881)));
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlB), DhtUtil.portToByte(6881)));
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlC), DhtUtil.portToByte(6881)));
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlD), DhtUtil.portToByte(26529)));
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlE), DhtUtil.portToByte(51978)));
        udpManager.kademliaBucket.add(new Node(DhtUtil.randomByteArray(20), DhtUtil.hostToByte(urlF), DhtUtil.portToByte(6339)));

    }

    private void findNodeTask() {
        while (true) {
            byte[] transId = new byte[]{'f', 'n'};
            byte[] targetId = DhtUtil.randomByteArray(20);
            try {
                Thread.sleep(FIND_NODE_MS);
                networkController.send(nodeInfo.getRandomFindNodePacket(transId, localNode, targetId)); //send
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void gerPeerTask() {
        byte[] transId = new byte[]{'g', 'p'};
        while (true) {
            try {
                Thread.sleep(GET_PEER_MS);
                Node node = NodeInfo.getRandomNode();
                UdpPacket udpPacket = DhtUtil.getPeer(transId, localNode.nodeId,
                        DhtUtil.hexToByteArray(GET_PEER_INFO_HASH), new InetSocketAddress(node.getIp(), node.getPort()));
                networkController.send(udpPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void manageUdpNetworkTask() {
        while (true) {
            try {
                UdpPacket udpPacket = networkController.recv();
                if (udpPacket != null) {
                    UdpPacket resp;
                    resp = udpManager.manage(udpPacket, localNode.nodeId);
                    if (resp != null)
                        networkController.send(resp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeNodeIdTask() {
        while (true) {
            try {
                Thread.sleep(CHANGE_NODE_ID_MS);
                localNode.nodeId = DhtUtil.randomByteArray(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void start() {
        new Thread(this::findNodeTask, "FIND_NODE_THREAD").start();
        new Thread(this::gerPeerTask, "GET_PEER_THREAD").start();
        new Thread(this::manageUdpNetworkTask, "HANDLER_THREAD").start();
        new Thread(this::changeNodeIdTask, "CHG_NODE_ID_THREAD").start();
    }

    public void run() throws Exception {
        init();
        start();
    }
}





