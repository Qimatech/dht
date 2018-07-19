package com.github.fengleicn.dht.bencode;

import com.github.fengleicn.dht.utils.DhtUtil;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Usage:
 * Bcd bcd = ...;
 * bcd.get(int, byte[], String) => bcd;
 * bcd.cast() => map, list, bigInteger, byte[];
 * bcd.castI/L/S => int, long, String of bcd;
 * bcd.set(int, long, string, byte[], map<byte[], Bcd>, list<Bcd>)
 * bcd.put/add(...)
 */
public class Bcd {
    private Object data;
    public final static Charset DEFAULT_CHARSER = Charset.forName("UTF8");

    public Bcd() {
    }

    public <T> Bcd(T o) {
        set(o);
    }

    public <T> void add(T o) {
        if (data instanceof List) {
            ((List<Bcd>) data).add(new Bcd().set(o));
        } else {
            throw new RuntimeException("not a list");
        }
    }


    public <T> void put(byte[] k, T o) {
        if (data instanceof Map) {
            ((Map<byte[], Bcd>) data).put(k, new Bcd(o));
        } else {
            throw new RuntimeException("not a map");
        }
    }

    public <T> void put(String k, T o) {
        put(k.getBytes(DEFAULT_CHARSER), o);
    }

    public <T> Bcd set(T o) {
        if (o instanceof List || o instanceof Map || o instanceof BigInteger || o instanceof byte[]) {
            data = o;
        } else if (o instanceof String) {
            data = ((String) o).getBytes(DEFAULT_CHARSER);
        } else if (o instanceof Number) {
            if (o instanceof Double || o instanceof Float)
                throw new RuntimeException("illegal arg");
            data = new BigInteger(String.valueOf(((Number) o).longValue()));
        } else if (o instanceof Bcd) {
            data = ((Bcd) o).data;
        } else if(o == null) {
            throw new RuntimeException("arg is null");
        }else{
            throw new RuntimeException("illegal arg");
        }
        return this;
    }

    public Bcd get(byte[] k) {
        Map<byte[], Bcd> map = ((Map<byte[], Bcd>) data);
        Set<byte[]> keySet = map.keySet();
        for (byte[] key : keySet) {
            if (DhtUtil.byteArraysEqual(k, key)) {
                return map.get(key);
            }
        }
        System.err.println(this.toString());
        throw new RuntimeException("not find key-value");
    }

    public Bcd get(String k) {
        byte[] key = k.getBytes(DEFAULT_CHARSER);
        return get(key);
    }

    public Bcd get(int k) {
        List<Bcd> list = (List<Bcd>) data;
        return list.get(k);
    }

    public <T> T cast() {
        return (T) data;
    }

    public byte[] castB() {
        return (byte[]) data;
    }

    public long castL() {
        return ((BigInteger) data).longValue();
    }

    public int castI() {
        return Math.toIntExact(castL());
    }

    public String castS() {
        byte[] bytes = cast();
        return new String(bytes, DEFAULT_CHARSER);
    }

    public final static String MAP = "Map";
    public final static String LIST = "List";
    public final static String BTARR = "byte[]";
    public final static String BINT = "BigInteger";
    public final static String ERR = "null";

    String type() {
        if (data instanceof Map) {
            return MAP;
        } else if (data instanceof List) {
            return LIST;
        } else if (data instanceof BigInteger) {
            return BINT;
        } else if (data instanceof byte[]) {
            return BTARR;
        } else {
            return ERR;
        }
    }

    @Test
    public void test001() {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (data instanceof Map) {
            sb.append("{");
            for (Map.Entry<byte[], Bcd> e : ((Map<byte[], Bcd>) data).entrySet()) {
                sb.append(new String(e.getKey(), DEFAULT_CHARSER));
                sb.append(" : ");
                sb.append(e.getValue().toString());
                sb.append(" , ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("}");
            return sb.toString();
        } else if (data instanceof List) {
            sb.append("[");
            for (Bcd o : ((List<Bcd>) data)) {
                if (o.data instanceof byte[]) {
                    sb.append(new String(o.cast(), DEFAULT_CHARSER));
                } else {
                    sb.append(o.toString());
                }
                sb.append(" , ");
            }
            sb.replace(sb.length() - 3, sb.length(), "");
            sb.append("]");
            return sb.toString();
        }else if (data instanceof byte[]) {
            return new String(((byte[]) data), DEFAULT_CHARSER);
        } else {
            return data.toString();
        }
    }
}