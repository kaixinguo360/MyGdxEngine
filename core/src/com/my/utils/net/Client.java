package com.my.utils.net;

import com.badlogic.gdx.utils.Json;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private static final Json JSON = new Json();

    public Client(String ip, int port, Listener listener) throws SocketException {
        byte[] buffer = new byte[102400];
        InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
        DatagramSocket socket = new DatagramSocket(socketAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        new Thread(() -> {
            while (true) {
                try {
                    socket.receive(packet);
                    if (listener == null) continue;
                    String data = new String(packet.getData(), 0, packet.getLength());
                    Map<String, String> jsonMap = JSON.fromJson(HashMap.class, data);
                    listener.receive(jsonMap.get("data"), Long.valueOf(jsonMap.get("time")));
                } catch (IOException ignored) {}
            }
        }).start();
    }
    public interface Listener {
        void receive(String data, long time);
    }
}
