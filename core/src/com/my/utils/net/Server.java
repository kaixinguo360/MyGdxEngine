package com.my.utils.net;

import com.badlogic.gdx.utils.Json;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final Json JSON = new Json();
    private static final Map<String, String> jsonMap = new HashMap<>();

    private byte[] buffer = new byte[10240];
    private InetAddress clientAddress = null;
    private int clientPort;
    private DatagramSocket socket = null;
    private DatagramPacket packet = null;
    public Server(String ip, int port, String clientIp, int clientPort) throws SocketException, UnknownHostException {
        this.clientAddress = Inet4Address.getByName(clientIp);
        this.clientPort = clientPort;
        InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
        socket = new DatagramSocket(socketAddress);
        packet = new DatagramPacket(buffer, buffer.length);
    }
    public void send(String data) {
        jsonMap.clear();
        jsonMap.put("data", data);
        jsonMap.put("time", "" + System.currentTimeMillis());

        packet.setAddress(clientAddress);
        packet.setPort(clientPort);
        packet.setData(JSON.toJson(jsonMap).getBytes());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
