package com.github.fengleicn.dht.node;

import org.junit.Test;

import java.util.Arrays;

public class Node {
    public byte[] nodeId;
    public byte[] ip;
    public byte[] port;

    public Node(byte[] buf) {
        nodeId = Arrays.copyOfRange(buf, 0, 20);
        ip = Arrays.copyOfRange(buf, 20, 24);
        port = Arrays.copyOfRange(buf, 24, 26);
    }

    public Node(byte[] nodeId, byte[] ip, byte[] port) {
        this.nodeId = nodeId;
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        StringBuilder sb = new StringBuilder();
        for (byte b : ip) {
            sb.append(b & 0xFF).append(".");
        }
        int l = sb.length();
        return sb.replace(l - 1, l, "").toString();
    }



    public int getPort() {
        return ((port[0] & 0xFF) << 8) + (port[1] & 0xFF);
    }

    public String getId() {
        StringBuilder sb = new StringBuilder();
        for (byte b : nodeId) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getId() + "_" + getIp() + ":" + getPort();
    }
}