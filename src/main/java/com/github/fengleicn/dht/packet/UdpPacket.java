package com.github.fengleicn.dht.packet;

import com.github.fengleicn.dht.bencode.Bcd;

import java.net.InetSocketAddress;

public class UdpPacket {
    public InetSocketAddress address;
    public Bcd bcd;

    public UdpPacket(InetSocketAddress address, Bcd bcd) {
        this.address = address;
        this.bcd = bcd;
    }
}