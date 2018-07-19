package com.github.fengleicn.dht.subsystem;

import com.github.fengleicn.dht.utils.DhtUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExtendBepNo9 {



    public String request(Socket socket, String infoHash) throws Exception {
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        byte[] prefix = {
                19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114,
                111, 116, 111, 99, 111, 108, 0, 0, 0, 0, 0, 16, 0, 0,
        };
        byte[] infoHashByte = DhtUtil.hexToByteArray(infoHash);
        byte[] peerId = DhtUtil.randomByteArray(20);
        byte[] handShakeHeader = ByteBuffer.allocate(68).put(prefix).put(infoHashByte).put(peerId).array();

        outputStream.write(handShakeHeader);
        outputStream.flush();

        byte[] remoteHSHeader = new byte[68];
        inputStream.read(remoteHSHeader);

        if (!DhtUtil.byteArraysEqual(Arrays.copyOfRange(remoteHSHeader, 0, 20), Arrays.copyOfRange(prefix, 0, 20)) || remoteHSHeader[25] != 16) {
            return null;
        }

        String extHandShakePayload = "d1:md11:ut_metadatai1eee";
        int len = extHandShakePayload.length();
        int extHandShakeLen = 2 + len + 4;
        byte[] extHandShake = ByteBuffer.allocate(extHandShakeLen).putInt(2 + len).put((byte) 20).put((byte) 0).put(extHandShakePayload.getBytes("ASCII")).array();

        outputStream.write(extHandShake);
        outputStream.flush();

        byte[] remoteHsContentLenByte = new byte[4];
        inputStream.read(remoteHsContentLenByte);

        int remoteHsContentLenInt = ByteBuffer.allocate(4).put(remoteHsContentLenByte).getInt(0);

        if (remoteHsContentLenInt > 100000) {
            return null;
        }

        byte[] remoteHsContent = new byte[remoteHsContentLenInt];
        inputStream.read(remoteHsContent);

        if (remoteHsContent[0] != 20 || remoteHsContent[1] != 0) {
            return null;
        }

        byte[] remoteHsPayload = Arrays.copyOfRange(remoteHsContent, 2, remoteHsContent.length);

        String req = "d8:msg_typei0e5:piecei0ee";
        len = req.length();
        int requestLen = 2 + len + 4;
        byte[] requestMD = ByteBuffer.allocate(requestLen).putInt(2 + len).put((byte) 20).put((byte) 2).put(req.getBytes("ASCII")).array();

        outputStream.write(requestMD);
        outputStream.flush();

//        byte[] recv_4 = new byte[4];
//        inputStream.read(recv_4);

//        remoteHsContentLenInt = ByteBuffer.allocate(4).put(recv_4).getInt(0);
//        if (remoteHsContentLenInt > 100000) {
//            return null;
//        }
//
//        byte[] bitMap = new byte[remoteHsContentLenInt];
//        inputStream.read(bitMap);

        Thread.sleep(3000);

        int i = 0;
        int time = 0;
        end:
        while (true) {
            if (inputStream.read() != '4') {
                if (time > 10000) {
                    break;
                }
                time++;
                continue;
            }
            byte[] pattern = new byte[5];
            inputStream.read(pattern);
            if (DhtUtil.byteArraysEqual(pattern, ":name".getBytes())) {
                StringBuilder sb = new StringBuilder();
                for (; ; ) {
                    int c = inputStream.read();
                    if (c >= '0' && c <= '9') {
                        sb.append(Character.toChars(c));
                        if(sb.length() > 5){
                            break end;
                        }
                    } else if (c == ':') {
                        int nameLen = Integer.valueOf(sb.toString());
                        if (nameLen > 10000) {
                            return null;
                        }
                        byte[] name = new byte[nameLen];
                        inputStream.read(name);

                        socket.close();
                        BtLibrary.getInstence().recordMetaData(infoHash, new String(name, "utf8"));
                        break end;
                    } else {
                        break end;
                    }
                }
            } else {
                if (i > 500) {
                    return null;
                }
                i++;
                continue;
            }
        }
        socket.close();
        return "OK";
    }
}
