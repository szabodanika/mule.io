package common;

import server.Server;
import server.ServerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public abstract class Connection<T extends Node> {

    protected T node;
    protected Socket socket;
    protected String address;
    protected int port;
    protected InetAddress inetAddress;
    protected AES aes = new AES();
    protected Thread listenerThread;
    public Object waiter = new Object();
    protected Message received, sending, requested;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected boolean handshakedone = false, listen = true;

    public void send(byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this, id, content, null);
        if(sending == null) {
            sending = message;
            sendNext();
        } else sending.enqueueMessage(message);
    }

    protected void sendRequest(byte[] requestId, byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this,id, content, null);
        message.markAsRequest(requestId);
        if(sending == null) {
            sending = message;
            sendNext();
        } else sending.enqueueMessage(message);
    }

    public void sendResult(byte[] requestId, byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this,id, content, null);
        message.markAsResult(requestId);
        if(sending == null) {
            sending = message;
            sendNext();
        } else sending.enqueueMessage(message);
    }

    public byte[] request(byte[] id, byte[] content) {
        final byte[][] result = new byte[1][1];

        final byte[] requestId = ByteUtils.generateRandom(Config.ARG_REQUEST_ID_LENGTH);

        try {
            sendRequest(requestId, id, content);
            while(true){
                sleep(100);
                try {
                    result[0] = findResult(requested, requestId).getContent();
                    break;
                }catch (NullPointerException e){ }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result[0];
    }

    private Message findResult(Message head, byte[] requestID){
        if(head == null) return null;
        if(Arrays.equals(head.getRequestId(),requestID)) return head;
        return findResult(head.getNext(),requestID);
    }

    abstract protected void sendNext() throws Exception;

    abstract protected void interpretNext() throws Exception;

    public void interpret(byte[] id, byte[] content) throws Exception {
        Message message = new Message(node,this, id, content, null);
        if(received == null) {
            received = message;
            interpretNext();
        } else received.enqueueMessage(message);
    }

    protected void interpretRequest(byte[] id, byte[] requestId, byte[] content) throws Exception {

        Message message = new Message(node,this, id, content, null);
        message.markAsRequest(requestId);
        if(received == null) {
            received = message;
            interpretNext();
        } else received.enqueueMessage(message);

    }

    protected void interpretResult(byte[] id, byte[] requestId, byte[] content) {

        Message message = new Message(node,this, id, content, null);
        message.markAsRequest(requestId);
        if(requested == null) {
            requested = message;
        } else requested.enqueueMessage(message);

    }

    abstract protected boolean verifyMessage(Connection<T> connection);

    protected void listen() {
        listenerThread = new Thread(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new common.UncaughtExceptionHandler());
            while (true) {
                if(listen) {
                    try {
                        byte[] encryption = new byte[1];
                        in.readFully(encryption);

                        byte[] lengthBytes = new byte[Config.ARG_LENGTH_BYTES];
                        in.readFully(lengthBytes);
                        int length = ByteUtils.bytesToInteger(lengthBytes);

                        byte[] stream = new byte[length];
                        in.readFully(stream, Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION, length - Config.ARG_LENGTH_BYTES - Config.ARG_ENCRYPTION);

                        ByteUtils.copy(lengthBytes, stream, 0, Config.ARG_ENCRYPTION, Config.ARG_LENGTH_BYTES);

                        ByteUtils.copy(encryption, stream, 0, 0, 1);

                        byte[] messageType = ByteUtils.cut(stream,
                                Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION,
                                Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE);

                        byte[] checkSumBytes = new byte[0], requestid = new byte[0], id = new byte[0], content = new byte[0];
                        int idlength = 0;

                        //TODO: use dynamic cursor instead
                        if(messageType[0] == 0) {
                            checkSumBytes = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION);

                            idlength = ByteUtils.bytesToInteger(ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES));

                            id = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES + idlength);

                            content = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES + idlength,
                                    length);
                        } else if(messageType[0] == 1 || messageType[0] == 2){
                            requestid = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_REQUEST_ID_LENGTH
                                    );

                            checkSumBytes = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_REQUEST_ID_LENGTH,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION + Config.ARG_MESSAGE_TYPE + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_CHECKSUM_BYTES);

                            idlength = ByteUtils.bytesToInteger(ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_REQUEST_ID_LENGTH,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_ID_LENGTH_BYTES));

                            id = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_ID_LENGTH_BYTES,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_ID_LENGTH_BYTES + idlength);

                            content = ByteUtils.cut(stream,
                                    Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_MESSAGE_TYPE + Config.ARG_ENCRYPTION + Config.ARG_REQUEST_ID_LENGTH + Config.ARG_ID_LENGTH_BYTES + idlength,
                                    length);
                        }

                        int checkSum = ByteUtils.checkSum(id, content);
                        if (checkSum != ByteUtils.bytesToInteger(checkSumBytes)) node.onDataStreamError(this);

                        switch (encryption[0]) {
                            case 0:
                                break;
                            case 1:
                                if(!verifyMessage(this)) break;
                                id = aes.decrypt(id);
                                content = aes.decrypt(content);
                                break;
                            case 2:
                                if(handshakedone) break;
                                id = node.rsa.decrypt(id);
                                content = node.rsa.decrypt(content);
                                break;
                        }

                        if (messageType[0] == 1) {
                            interpretRequest(id, requestid, content);
                        } else if (messageType[0] == 2) {
                            interpretResult(id, requestid, content);
                        } else {
                            interpret(id, content);
                        }

                    }catch (Exception e) {
                        node.onDisconnected(this, e);
                        break;
                    }
                }
            }
        });

        listenerThread.start();
    }

    public void close() throws IOException {
        socket.close();
        if(this instanceof ServerConnection){
            ((Server)this.node).getConnections().remove(this);
        }
    }

    public InetAddress getInetAddress() {
        if(socket != null) return socket.getInetAddress();
        else return inetAddress;
    }

    public String getAddress(){
        if(socket != null) return socket.getInetAddress().getHostAddress();
        else if(inetAddress != null){
            return inetAddress.getHostAddress();
        }else return address;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Thread getListenerThread() {
        return listenerThread;
    }

    public boolean isHandshakedone() {
        return handshakedone;
    }
}
