package common;

public abstract class Node <T extends Connection> {

    protected RSA rsa = new RSA();

    protected boolean connected = false;

    public abstract void onReceived(T connection, byte[] id, byte[] content);

    public abstract void onRequested(T connection, byte[] requestId, byte[] id, byte[] content);

    public abstract void onRequestTimeout(T connection, byte[] id, byte[] content);

    public abstract void onSent(T connection, byte[] id, byte[] content);

    protected abstract void onDataStreamError(T connection);

    protected abstract void onDisconnected(T connection, Exception e);

    protected abstract void connectionFailed(T connection, Exception e);

    protected abstract void onConnecting(T connection);

    public abstract void onConnected(T connection);

    public RSA getRsa() {
        return rsa;
    }

}
