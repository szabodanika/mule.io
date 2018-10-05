package client;

import common.Node;

import java.net.Socket;

public abstract class Client extends Node<ClientConnection> {

    private ClientConnection connection;
    private byte[] id;

    public void connect(String host, int port) {
        if(id!=null) {
            try {
                Socket socket = new Socket(host, port);
                connection = new ClientConnection(socket, this);
                onConnecting(connection);
                connected = true;
            } catch (Exception e) {
                if (connected) onDisconnected(connection, e);
                else {
                    connection = new ClientConnection();
                    connection.setAddress(host);
                    connection.setPort(port);
                    connectionFailed(connection, e);
                }
            }
        } else {
            System.out.println("Missing ID");
        }
    }

    public void send(String id, byte[] content) throws Exception {
        connection.send(id.getBytes(),content);
    }

    public byte[] request(String id, byte[] content){
        return connection.request(id.getBytes(),content);
    }

    public void setId(String id){
        this.id = id.getBytes();
    }

    public void setId(byte[] id){
        this.id = id;
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public byte[] getId() {
        return id;
    }
}
