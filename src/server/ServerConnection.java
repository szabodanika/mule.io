package server;

import common.ByteUtils;
import common.Message;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerConnection extends common.Connection<Server>{


    public byte[] id = null;

    public ServerConnection(Socket socket, Server server) throws Exception {
        this.socket = socket;
        this.address = socket.getInetAddress().getHostName();
        this.node = server;

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        sendPublicKey();
        listen();
    }

    public void send(byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this,id, content, null);
        if(sending == null) {
            sending = message;
            sendNext();
        } else sending.enqueueCommand(message);
    }

    public void sendNext() throws Exception {

        if(handshakedone) socket.getOutputStream().write(sending.getFinal(Message.encryptionType.AES));
        else socket.getOutputStream().write(sending.getFinal(Message.encryptionType.RAW));

        if(ByteUtils.checkSum(sending.getID()) != 0) sending.sent();

        socket.getOutputStream().flush();
        sending = sending.getNext();
        if(sending != null) sendNext();

    }

    @Override
    public void interpretNext() throws Exception {

        switch (ByteUtils.checkSum(received.getID())){
            case 0:
                aes.setKey(received.getContent());
                break;
            case 1:
                aes.setIv(new IvParameterSpec(received.getContent()));
                handshakeDone();
                break;
            case 2:
                id = received.getContent();
                node.onIntroduction(this);
                break;
            default:
                node.onReceived(this,received.getID(),received.getContent());
        }

        received = received.getNext();
        if(received != null) interpretNext();
    }

    private void sendPublicKey() throws Exception {
        send(new byte[]{0}, node.getRsa().publicKeyToBytes(node.getRsa().getPublicKey()));
    }

    @Override
    public void interpret(byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this, id, content, null);
        if(received == null) {
            received = message;
            interpretNext();
        } else received.enqueueCommand(message);
    }


    public void handshakeDone() {
        this.handshakedone = true;
        node.connections.add(this);
        node.onConnected(this);
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public Server getServer() {
        return node;
    }

    public void setServer(Server server) {
        this.node = server;
    }
}
