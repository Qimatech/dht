package com.github.fengleicn.dht.bencode;

import java.util.HashMap;

public class BcdHashMap extends HashMap<byte[], Bcd> {
    public Bcd put(String key, Bcd value) {
        return super.put(key.getBytes(Bcd.DEFAULT_CHARSER), value);
    }
}
