package com.github.fengleicn.dht.bencode;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

// for Bencode encoding / decoding
public class BcdCoder {
    static class BvCarrier {
        public Bcd bcd;
    }

    public static Bcd decode(final byte[] src) throws IOException {
        BvCarrier bvCarrier = new BvCarrier();
        findNext(src, 0, bvCarrier);
        return bvCarrier.bcd;
    }

    public static int findNext(final byte[] src, final int ptr, final BvCarrier bvCarrier) throws IOException {
        byte header = src[ptr];
        Bcd v = (bvCarrier.bcd = new Bcd());
        int next = ptr;
        switch (header) {
            case 'd':
                next++;
                v.set(new HashMap<byte[], Bcd>());
                while (src[next] != 'e') {
                    BvCarrier w = new BvCarrier();
                    next = findNext(src, next, w);
                    byte[] key = w.bcd.cast();
                    next = findNext(src, next, w);
                    v.put(key, w.bcd);
                }
                return ++next;
            case 'l':
                next++;
                v.set(new ArrayList<Bcd>());
                while (src[next] != 'e') {
                    BvCarrier w = new BvCarrier();
                    next = findNext(src, next, w);
                    v.add(w.bcd);
                }
                return ++next;
            case 'i':
                next++;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte b;
                while ((b = src[next]) != 'e') {
                    if (b != '-' && !(b >= '0' && b <= '9')) {
                        throw new RuntimeException("error in BcdCoder.findNext(int) token: " + b);
                    }
                    byteArrayOutputStream.write(b);
                    next++;
                }
                byte[] num = byteArrayOutputStream.toByteArray();
                if (num[0] == '-' && num[1] == '0' // -0123
                        || num[0] == '0' && num.length != '0') // 0123
                    throw new RuntimeException("error in BcdCoder.findNext(int) [num]: " + num);
                v.set(new BigInteger(new String(num, Bcd.DEFAULT_CHARSER)));
                return ++next;
            default:
                if(header == '0'){
                      v.set(new byte[0]);
                      return next + 2;
                } else if (header >= '1' && header <= '9') {
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    while ((b = src[next]) != ':') {
                        if (!(b >= '0' && b <= '9')) {
                            throw new RuntimeException("error in BcdCoder.findNext(int) token: " + b);
                        }
                        byteArrayOutputStream.write(b);
                        next++;
                    }
                    num = byteArrayOutputStream.toByteArray();
                    if (num[0] == '0')
                        throw new RuntimeException("error in BcdCoder.findNext(byte) [num]: " + num);
                    next++;
                    int len = Integer.valueOf(new String(num, Bcd.DEFAULT_CHARSER));
                    v.set(Arrays.copyOfRange(src, next, next + len));
                    return next + len;
                } else {
                    throw new RuntimeException("error in BcdCoder.findNext: header == " + header);
                }
        }
    }

    // encode
    public static byte[] encode(Bcd bcd) {
        byte[] ret = {};
        switch (bcd.type()) {
            case Bcd.MAP:
                ret = new byte[]{'d'};
                Map<byte[], Bcd> map = bcd.cast();
                TreeMap<byte[], Bcd> treeMap = new TreeMap<>((o1, o2) -> {
                    int len = o1.length > o2.length ? o2.length : o1.length;
                    for (int i = 0; i < len; i++) {
                        if (o1[i] == o2[i]) {
                            continue;
                        } else {
                            return o1[i] - o2[i];
                        }
                    }
                    return 0;
                });
                treeMap.putAll(map);
                for (Map.Entry<byte[], Bcd> entry : treeMap.entrySet()) {
                    ret = concat(ret,
                            String.valueOf(entry.getKey().length).getBytes(Bcd.DEFAULT_CHARSER),
                            new byte[]{':'},
                            entry.getKey(),
                            encode(entry.getValue()));
                }
                break;
            case Bcd.LIST:
                ret = new byte[]{'l'};
                List<Bcd> list = bcd.cast();
                for (Bcd b : list) {
                    ret = concat(ret, encode(b));
                }
                break;
            case Bcd.BTARR:
                byte[] bytes = bcd.cast();
                ret = concat(String.valueOf(bytes.length).getBytes(Bcd.DEFAULT_CHARSER),
                        new byte[]{':'}, bytes);
                break;
            case Bcd.BINT:
                BigInteger i = bcd.cast();
                ret = concat(new byte[]{'i'}, i.toString().getBytes(Bcd.DEFAULT_CHARSER));
                break;
            case Bcd.ERR:
                throw new RuntimeException("Bcd.Err");
        }
        if (bcd.type() != Bcd.BTARR)
            ret = concat(ret, new byte[]{'e'});
        return ret;
    }

    private static byte[] concat(byte[]... a) {
        byte[] ret = {};
        for (byte[] i : a) {
            ret = concat0(ret, i);
        }
        return ret;
    }

    private static byte[] concat0(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
