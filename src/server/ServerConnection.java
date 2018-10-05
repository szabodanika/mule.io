package server;

import common.ByteUtils;
import common.Connection;
import common.Message;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerConnection extends common.Connection<Server>{


    public byte[] id = null;
    private int sourcePort;

    public ServerConnection(Socket socket, Server server) throws Exception {
        this.socket = socket;
        this.inetAddress = socket.getInetAddress();
        this.sourcePort = socket.getPort();
        this.setAddress(inetAddress.getHostAddress());
        this.node = server;

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        sendPublicKey();
        listen();
    }

    public ServerConnection() {
        //do not call empty constructor, use getAddressHolder() instead
    }

    public static ServerConnection getAddressHolder(String address){
        if(address == null) return null;
        ServerConnection serverConnection = new ServerConnection();
        serverConnection.setAddress(address);
        return serverConnection;
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
                if(handshakedone) break;
                aes.setKey(received.getContent());
                break;
            case 1:
                if(handshakedone) break;
                aes.setIv(new IvParameterSpec(received.getContent()));
                handshakeDone();
                break;
            case 2:
                if(this.id != null) break;
                if(node.isIDInUse(new String(received.getContent()))){
                    send("error".getBytes(), "Someone has already connected with that ID.".getBytes());
                    close();
                    break;
                }

                Pattern allowedCharacters = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = allowedCharacters.matcher(new String(received.getContent()));
                if(!matcher.find()){
                    id = received.getContent();
                    if(!node.getFirewall().verifyConnection(new String(id))){
                        this.close();
                    }
                    node.onIntroduction(this);
                } else {
                    send("error".getBytes(), "Special character in ID.".getBytes());
                    close();
                }
                break;
            default:
                if(this.id == null) break;
                if (received.isRequest()) {
                    node.onRequested(this, received.getRequestId(), received.getID(), received.getContent());
                }
                if (received.isResult()) {
                    if (requested == null) requested = received.withoutNext();
                    else requested.enqueueMessage(received.withoutNext());
                    requested.notifyAll();
                }
                node.onReceived(this, received.getID(), received.getContent());

        }

        received = received.getNext();
        if(received != null) interpretNext();
    }

    private void sendPublicKey() throws Exception {
        send(new byte[]{0}, node.getRsa().publicKeyToBytes(node.getRsa().getPublicKey()));
    }

    @Override
    protected boolean verifyMessage(Connection<Server> connection) {
        return node.getFirewall().verifyMessage(connection);
    }


    public void handshakeDone() {
        this.handshakedone = true;
        node.getConnections().add(this);
        node.onConnected(this);
    }

    public byte[] getId() {
        return id;
    }

    public int getSourcePort() {
        return sourcePort;
    }
}
