package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class Connection<T extends Node> {

    protected T node;
    protected Socket socket;
    protected String address;
    protected AES aes = new AES();
    protected Thread thread;
    protected Message received, sending;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected boolean handshakedone = false;

    abstract protected void send(byte[] id, byte[] content) throws Exception;

    abstract protected void sendNext() throws Exception;

    abstract protected void interpretNext() throws Exception;

    abstract protected void interpret(byte[] id, byte[] content) throws Exception;

    protected void listen() {
        thread = new Thread(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new common.UncaughtExceptionHandler());
            try {
                while (true) {
                    byte[] encryption = new byte[1];
                    in.readFully(encryption);

                    byte[] lengthBytes = new byte[Config.ARG_LENGTH_BYTES];
                    in.readFully(lengthBytes);
                    int length = ByteUtils.bytesToInteger(lengthBytes);

                    byte[] stream = new byte[length];
                    in.readFully(stream, Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION,length- Config.ARG_LENGTH_BYTES - Config.ARG_ENCRYPTION);

                    ByteUtils.copy(lengthBytes, stream, 0, Config.ARG_ENCRYPTION, Config.ARG_LENGTH_BYTES);

                    ByteUtils.copy(encryption, stream, 0, 0, 1);

                    byte[] checkSumBytes = ByteUtils.cut(stream,
                            Config.ARG_LENGTH_BYTES + Config.ARG_ENCRYPTION,
                            Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ENCRYPTION);

                    int idlength = ByteUtils.bytesToInteger(ByteUtils.cut(stream,
                            Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ENCRYPTION,
                            Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES));

                    byte[] id = ByteUtils.cut(stream,
                            Config.ARG_LENGTH_BYTES  + Config.ARG_CHECKSUM_BYTES + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES,
                            Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES  + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES + idlength);

                    byte[] content = ByteUtils.cut(stream,
                            Config.ARG_LENGTH_BYTES + Config.ARG_CHECKSUM_BYTES + Config.ARG_ENCRYPTION + Config.ARG_ID_LENGTH_BYTES + idlength,
                                length);

                    int checkSum = ByteUtils.checkSum(id, content);
                    if(checkSum != ByteUtils.bytesToInteger(checkSumBytes)) node.onDataStreamError(this);

                    switch (encryption[0]){
                        case 0:
                            break;
                        case 1:
                            id = aes.decrypt(id);
                            content = aes.decrypt(content);
                            break;
                        case 2:
                            id = node.rsa.decrypt(id);
                            content = node.rsa.decrypt(content);
                            break;
                    }

                    interpret(id, content);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    public void close() throws IOException {
        socket.close();
    }

    public Node getNode() {
        return node;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public AES getAes() {
        return aes;
    }

    public void setAes(AES aes) {
        this.aes = aes;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Message getReceived() {
        return received;
    }

    public void setReceived(Message received) {
        this.received = received;
    }

    public Message getSending() {
        return sending;
    }

    public void setSending(Message sending) {
        this.sending = sending;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public boolean isHandshakedone() {
        return handshakedone;
    }

    public void setHandshakedone(boolean handshakedone) {
        this.handshakedone = handshakedone;
    }
}
