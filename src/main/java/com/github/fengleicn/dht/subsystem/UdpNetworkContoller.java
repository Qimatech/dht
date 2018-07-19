package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.bencode.Bcd;
import com.github.fengleicn.dht.bencode.BcdCoder;
import com.github.fengleicn.dht.packet.UdpPacket;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;

public
/**
 * author Lei Feng
 */


class UdpNetworkContoller {
    public static FileWriter sendWriter;
    public static FileWriter recvWriter;

    static {
        try {
            recvWriter = new FileWriter("recv.txt", true);
            sendWriter = new FileWriter("send.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    DatagramSocket datagramSocket;

    public UdpNetworkContoller(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void send(UdpPacket udpPacket) throws IOException {
        Bcd bcd = udpPacket.bcd;
        byte[] b = BcdCoder.encode(bcd);
        printByte(sendWriter, b, udpPacket.address.getHostString(), udpPacket.address.getPort());
        datagramSocket.send(new DatagramPacket(b, b.length, udpPacket.address));
    }

    public UdpPacket recv() throws Exception {
        byte[] buf = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        datagramSocket.receive(packet);
        byte[] b = Arrays.copyOf(packet.getData(), packet.getLength());
        printByte(recvWriter, b, packet.getAddress().getHostAddress(), packet.getPort());
        Bcd bcd;
        try {
            bcd = BcdCoder.decode(b);
        }catch (Exception e){
            byte[] p = b.clone();
            for (int i = 0; i < p.length; i++) {
                p[i] = p[i] >= ' ' && p[i] <= '~' ? p[i] : (byte) '.';
            }
            System.err.println(new String(p));
            throw e;
        }
        return new UdpPacket((InetSocketAddress) packet.getSocketAddress(), bcd);
    }

    public void printByte(FileWriter w, byte[] b, String ip, int port) throws IOException {
//        byte[] p = b.clone();
//        for (int i = 0; i < p.length; i++) {
//            p[i] = p[i] >= ' ' && p[i] <= '~' ? p[i] : (byte) '.';
//        }
//        w.append(new Date().toString()).append(" : ").append(String.format("%22s", ip + ":" + port))
//                .append(" => ").append(new String(p)).append("\n");
//        w.flush();
    }
}
