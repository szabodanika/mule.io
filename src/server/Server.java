package server;

import common.Node;
import common.RSA;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class Server extends Node<ServerConnection> {

    private ServerSocket serverSocket;
    ArrayList<ServerConnection> connections = new ArrayList<>();
    static Thread listener;

    public abstract void onReceived(ServerConnection connection, byte[] id, byte[] content);

    public abstract void onSent(ServerConnection connection, byte[] id, byte[] content);

    protected abstract void onDataStreamError(ServerConnection connection);

    protected abstract void onConnecting(ServerConnection serverConnection);

    public abstract void onConnected(ServerConnection connection);

    protected abstract void onIntroduction(ServerConnection connection);

    public void init(int port) throws NoSuchAlgorithmException, IOException {
        serverSocket = new ServerSocket(port);
        rsa.setKeyPair(RSA.buildKeyPair());
        waitConnection();
    }

    public void send(String address, String id, byte[] content) throws Exception {
        for(ServerConnection connection : connections) if(connection.getAddress() == address) connection.send(id.getBytes(), content);
    }

    public void broadcast(String id, byte[] content) throws Exception {
        for(ServerConnection connection : connections) send(connection.getAddress(),id, content);
    }

    private void waitConnection(){

        listener = new Thread(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new common.UncaughtExceptionHandler());
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    onConnecting(new ServerConnection(socket, this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        listener.start();
    }

}
