package server;

import common.Node;
import common.RSA;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class Server extends Node<ServerConnection> {

    private ServerSocket serverSocket;
    private ArrayList<ServerConnection> connections = new ArrayList<>();
    private Thread listener;
    private Firewall firewall = new Firewall();
    private Config config = new Config();

    public abstract void onReceived(ServerConnection connection, byte[] id, byte[] content);

    public abstract void onSent(ServerConnection connection, byte[] id, byte[] content);

    protected abstract void onDataStreamError(ServerConnection connection);

    @Override
    protected void onDisconnected(ServerConnection connection, Exception e) {
        connections.remove(connection);
    }

    protected abstract void onConnecting(ServerConnection serverConnection);

    protected abstract void onStarted();

    public abstract void onConnected(ServerConnection connection);

    protected void onIntroduction(ServerConnection connection){
        firewall.addID(connection.getAddress(), new String(connection.getId()));
    }

    protected abstract void onBlockAddress(ServerConnection[] connections);

    protected abstract void onMuteAddress(ServerConnection[] connections);

    protected abstract void onUnblockAddress(ServerConnection[] connections);

    protected abstract void onUnmuteAddress(ServerConnection[] connections);

    protected abstract void onBlockUser(String id);

    protected abstract void onUnblockUser(String id);

    protected abstract void onMuteUser(String id);

    protected abstract void onUnmuteUser(String id);

    protected abstract void onKickAddress(ServerConnection[] connections);

    protected abstract void onKickUser(String id);

    protected abstract ServerConnection[] onMultipleConnectionsFromAddress(ServerConnection[] connections);

    public void blockAddress(String address, int minutes){
        ServerConnection[] connections = getConnectionsByAddress(address);

        for(ServerConnection connection : connections){
            this.firewall.blockAddresses(new String[]{connection.getAddress()}, minutes);
        }

        onBlockAddress(connections);
    }

    public void unblockAddress(String address){
        ServerConnection[] connections = getConnectionsByAddress(address);

        for(ServerConnection connection : connections){
            this.firewall.unblockAddresses(new String[]{connection.getAddress()});
        }

        onUnblockAddress(connections);
    }

    public void muteAddress(String address, int minutes){
        ServerConnection[] connections = getConnectionsByAddress(address);

        for(ServerConnection connection : connections){
            this.firewall.muteAddresses(new String[]{connection.getAddress()}, minutes);
        }

        onMuteAddress(connections);
    }

    public void unmute(String id){
        this.firewall.unmuteUsers(new String[]{id});

        onUnmuteUser(id);
    }

    public void block(String id, int minutes){
        this.firewall.blockUsers(new String[]{id}, minutes);
        onBlockUser(id);
    }

    public void unblock(String id){
        this.firewall.unblockUsers(new String[]{id});
        onUnblockUser(id);
    }

    public void mute(String id, int minutes){
        this.firewall.muteUsers(new String[]{id}, minutes);
        onMuteUser(id);
    }

    public void kick(String id){

        try {
            getActiveConnectionByIDorAddress(id).close();
        } catch (IOException e) {

        }

        onKickUser(id);
    }

    public void unmuteAddress(String address){
        ServerConnection[] connections = getConnectionsByAddress(address);

        for(ServerConnection connection : connections){
            this.firewall.unmuteAddresses(new String[]{connection.getAddress()});
        }

        onUnmuteAddress(connections);
    }

    public void kickAddress(String address){
        for(ServerConnection serverConnection : getActiveConnectionsByAddress(address)) {
            try {
                serverConnection.close();
            } catch (IOException e) { }
        }
        onKickAddress(getActiveConnectionsByAddress(address));
    }

    public void init() throws NoSuchAlgorithmException, IOException {
        firewall.init(this);
        config.init(this);
        try {
            serverSocket = new ServerSocket(config.getIntValue("port"));
        }catch (BindException e){
            System.out.println("Port " + Config.getIntValue("port") + " is already in use.");
            System.exit(-1);
        }
        rsa.setKeyPair(RSA.buildKeyPair());
        waitConnection();
        onStarted();
    }

    public void send(InetAddress address, String id, byte[] content) throws Exception {
        for(ServerConnection connection : getActiveConnectionsByAddress(address.getHostAddress())){
            connection.send(id.getBytes(),content);
        }
    }

    public void send(String clientId, String id, byte[] content) throws Exception {
        getConnectionByIDorAddress(clientId).send(id.getBytes(),content);
    }

    public void request(InetAddress address, String id, byte[] content){
        for(ServerConnection connection : getActiveConnectionsByAddress(address.getHostAddress())) connection.request(id.getBytes(),content);
    }

    public void broadcast(String id, byte[] content) throws Exception {
        for(ServerConnection connection : connections) connection.send(id.getBytes(),content);
    }

    public void broadcastExcept(ServerConnection sender, String id, byte[] content) throws Exception {
        for(ServerConnection connection : connections) if(connection != sender) connection.send(id.getBytes(),content);
    }

    private void waitConnection(){

        listener = new Thread(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new common.UncaughtExceptionHandler());
            while (true){
                if(!firewall.isBlocked()){
                    ServerConnection connection = null;
                    try {
                        Socket socket = serverSocket.accept();
                        if(firewall.verifyConnection(socket)){
                            connection = new ServerConnection(socket, this);
                            onConnecting(connection);
                        }
                    } catch (Exception e) {
                        if(connection != null) onDisconnected(connection, e);
                        e.printStackTrace();
                    }
                }
            }
        });

        listener.start();
    }

    public ArrayList<ServerConnection> getConnections() {
        return connections;
    }

    public Firewall getFirewall() {
        return firewall;
    }

    protected ServerConnection[] getActiveConnectionsByAddress(String address){
        ArrayList<ServerConnection> results = new ArrayList<>();
        for(ServerConnection connection : connections) if (connection.getAddress().equals(address))results.add(connection);

        ServerConnection[] resultArray = new ServerConnection[results.size()];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = results.get(i);
        }

        if(resultArray.length > 1) return onMultipleConnectionsFromAddress(resultArray);
        else if (resultArray.length == 0) {
            System.out.println("Client not found with the address " + address + ".");
        }
        return resultArray;
    }

    protected ServerConnection[] getConnectionsByAddress(String address){
        if(getActiveConnectionsByAddress(address) == null) {
            if(getConnectionByIDorAddress(address) != null) return new ServerConnection[]{getConnectionByIDorAddress(address)};
            System.out.println("Client not found with the address " + address + ".");
        }
        return getActiveConnectionsByAddress(address);
    }

    public String getBestName(ServerConnection connection){
        String[] prevIDs = firewall.getID(connection.getAddress());
        if(connection.getId()!=null) return new String(connection.getId());
        else{
            if(prevIDs != null) if(prevIDs.length == 1) return prevIDs[0];
            return connection.getAddress() + ":" + connection.getSourcePort();
        }
    }

    //if an ID is given, it checks current connections, then past connections, then returns null
    //if an address is given, it checks current connections, then returns null
    public ServerConnection getConnectionByIDorAddress(String IDorAddress){
        if(getActiveConnectionsByAddress(IDorAddress) != null) return getActiveConnectionByIDorAddress(IDorAddress);
        if(IDorAddress.contains(".")) return ServerConnection.getAddressHolder(IDorAddress);
        return ServerConnection.getAddressHolder(firewall.getAddress(IDorAddress));
    }

    public ServerConnection getActiveConnectionByIDorAddress(String IDorAddress){
        for(ServerConnection serverConnection : this.connections){
            if(serverConnection.getId() != null){
                if(new String(serverConnection.getId()).equals(IDorAddress)) return serverConnection;
            }
            if(new String(serverConnection.getAddress()).equals(IDorAddress)) return serverConnection;
        }
        return null;
    }

    public boolean isIDInUse(String id){
        for(ServerConnection connection : connections) {
            if (connection.getId() != null) {
                if (new String(connection.getId()).equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

    public void stop(){
        try {
            System.out.println("Server stopped.");
            System.exit(1);
        }catch (Exception e){ }
    }
}
