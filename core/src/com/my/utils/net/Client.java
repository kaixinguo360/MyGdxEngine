package com.my.utils.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Client {
    public Client(String ip, int port, Listener listener) throws SocketException {
        byte[] buffer = new byte[102400];
        InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
        DatagramSocket socket = new DatagramSocket(socketAddress);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        new Thread(() -> {
            while (true) {
                try {
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());
                    listener.receive(data);
                } catch (IOException ignored) {}
            }
        }).start();
    }
    public interface Listener {
        void receive(String data);
    }
}
