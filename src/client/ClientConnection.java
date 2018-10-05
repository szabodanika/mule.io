package client;

import common.ByteUtils;
import common.Connection;
import common.Message;
import common.RSA;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends common.Connection<Client> {

    public ClientConnection(Socket socket, Client client) throws Exception {
        this.node = client;
        this.socket = socket;
        if(socket!=null) this.inetAddress = socket.getInetAddress();

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        listen();
    }

    public ClientConnection(){

    }
    @Override
    public void sendNext() throws Exception {

        if(!handshakedone){
            socket.getOutputStream().write(sending.getFinal(Message.encryptionType.RSA));
        } else {
            socket.getOutputStream().write(sending.getFinal(Message.encryptionType.AES));
        }

        if(ByteUtils.checkSum(sending.getID()) != (0|1|2)) sending.sent();

        socket.getOutputStream().flush();

        sending = sending.getNext();
        if(sending != null) sendNext();
    }

    @Override
    public void interpretNext() throws Exception {

        switch (ByteUtils.checkSum(received.getID())){
            case 0:
                if(handshakedone) break;
                node.getRsa().setPublicKey(RSA.bytesToPublicKey(received.getContent()));
                sendAES();
                sendID();
                break;
            default:
                node.onReceived(this,received.getID(),received.getContent());
        }

        received = received.getNext();
        if(received != null) interpretNext();
    }

    private void sendAES() throws Exception {
        aes.init();
        send(new byte[]{0}, aes.getKey());
        send(new byte[]{1}, aes.getIv().getIV());
        handshakeDone();
    }

    private void sendID() throws Exception {
        send(new byte[]{2}, node.getId());
    }

    @Override
    protected boolean verifyMessage(Connection<Client> connection) {
        return true;
    }


    public void handshakeDone() {
        node.onConnected(this);
        this.handshakedone = true;
    }

    public void close() throws IOException {
        socket.close();
    }

}
