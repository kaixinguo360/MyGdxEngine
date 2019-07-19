package com.my.utils.net;

import java.io.IOException;
import java.net.*;

public class Server {
    private byte[] buffer = new byte[10240];
    private InetAddress clientAddress = null;
    private int clientPort;
    private DatagramSocket socket = null;
    private DatagramPacket packet = null;
    public Server(String ip, int port) throws SocketException, UnknownHostException {
        clientAddress = Inet4Address.getByName(ip);
        clientPort = port;
        socket = new DatagramSocket();
        packet = new DatagramPacket(buffer, buffer.length);
    }
    public void send(String data) {
        packet.setAddress(clientAddress);
        packet.setPort(clientPort);
        packet.setData(data.getBytes());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
