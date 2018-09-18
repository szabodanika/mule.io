package client;

import common.ByteUtils;
import common.Node;

import java.net.Socket;

public abstract class Client extends Node<ClientConnection> {

    private ClientConnection connection;
    private byte[] id = ByteUtils.generateRandom(32);

    public void connect(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        connection = new ClientConnection(socket,this);
        onConnecting(connection);
    }

    public void send(String id, byte[] content) throws Exception {
        connection.send(id.getBytes(),content);
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
