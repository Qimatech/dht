package com.github.fengleicn.dht.utils;

import com.github.fengleicn.dht.node.Node;

import java.math.BigInteger;
import java.util.*;

public class KademliaBucket {
    List<Node>[] buket;
    Node localNode;
    final static int K = 20;


    @SuppressWarnings("unchecked")
    public KademliaBucket(Node localNode) {
        buket = new List[160];
        for (int i = 0; i < 160; i++) {
            buket[i] = new Vector<>();
        }
        this.localNode = localNode;
    }

    void remove(List<Node> list) {
        list.remove(0);
    }

    public BigInteger xor(Node node1, Node node2) {
        byte[] buf = new byte[21];
        for (int i = 1; i < 21; i++) {
            buf[i] = (byte) (node1.nodeId[i - 1] ^ node2.nodeId[i - 1]);
        }
        return new BigInteger(buf);
    }

    public Node getRandom() throws Exception {
        Random r  = new Random();
        List<Integer> notEmptyList = new ArrayList<>();
        for (int i = 0; i < 160; i++){
            if(buket[i].size() != 0){
                notEmptyList.add(i);
            }
        }
        if(notEmptyList.size() == 0)
            throw new Exception("notEmptyList is empty");
        int i = notEmptyList.get(r.nextInt(notEmptyList.size()));
        int size = buket[i].size();
        if(Arrays.equals(buket[i].get(r.nextInt(size)).ip, localNode.ip)){
            throw new Exception("a loop udp");
        }
        return buket[i].get(r.nextInt(size));
    }

    public synchronized void add(Node node) {
        BigInteger bigInteger = xor(node, localNode);
        for (int i = 1; i <= 160; i++) {
            if (bigInteger.compareTo(BigInteger.TWO.pow(i)) < 0) {
                if(Arrays.equals(node.ip, localNode.ip)){
                    return;
                }
                if (buket[i - 1].size() >= K) {
                    remove(buket[i - 1]);
                }
                for(Node bucketNode : buket[i - 1]){
                    if(Arrays.equals(bucketNode.nodeId, node.nodeId)){
                        bucketNode.ip = node.ip;
                        bucketNode.port = node.port;
                        return;
                    }
                }
                buket[i - 1].add(node);
                return;
            }
        }
    }

    public List<Node> get(Node node){
        BigInteger bigInteger = xor(node, localNode);
        for (int i = 1; i <= 160; i++) {
            if (bigInteger.compareTo(BigInteger.TWO.pow(i)) < 0) {
                return buket[i - 1];
            }
        }
        return null;
    }

    public int showNodeSize(){
        int sum = 0;
        for (List<Node> list : buket){
            sum += list.size();
        }
        return sum;
    }

}
