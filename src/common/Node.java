package common;

public abstract class Node <T extends Connection> {

    protected RSA rsa = new RSA();

    public abstract void onReceived(T connection, byte[] id, byte[] content);

    public abstract void onSent(T connection, byte[] id, byte[] content);

    protected abstract void onDataStreamError(T connection);

    protected abstract void onConnecting(T connection);

    public abstract void onConnected(T connection);

    public RSA getRsa() {
        return rsa;
    }

}
