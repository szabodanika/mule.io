package server;

import common.Connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Firewall {

    private ArrayList<Object[]> connectionAttempts = new ArrayList<>();
    private ArrayList<Object[]> blockedAddresses = new ArrayList<>();
    private ArrayList<Object[]> blockedIDs = new ArrayList<>();

    private ArrayList<Object[]> messagesReceived = new ArrayList<>();
    private ArrayList<Object[]> mutedAddresses = new ArrayList<>();
    private ArrayList<Object[]> mutedIDs = new ArrayList<>();

    private ArrayList<String[]> clientIDs = new ArrayList<>();

    static Timer timer;
    private int attemptCounter;
    private boolean blockConnections;
    private Server server;

    void init(Server server){
        loadCriminals();
        loadIDlist();
        if(timer!=null) return;
        this.server = server;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timedTask();
            }
        }, 0,60 * 1000);
    }

    boolean verifyConnection(Socket socket){
        if(isAddressBlocked(socket.getInetAddress())) return false;
        if(Config.getIntValue("autoConnectionShutEnabled") == 1){
            attemptCounter++;
            if(attemptCounter > Config.getIntValue("connectionsAttemptsPerMinute")) blockAll(Config.getIntValue("connectionsAttemptsPerMinute_blocklength"));
        }
        if(Config.getIntValue("autoBanEnabled") == 1) {
            for (Object[] object : connectionAttempts) {
                if (object[0].equals(socket.getInetAddress())) {
                    if ((Integer) (object[1]) >= Config.getIntValue("reconnectionAttemptsPerMinute")) {
                        blockAddresses((String[]) object[0], Config.getIntValue("reconnectionsAttemptsPerMinute_blocklength"));
                    }
                }
            }
        }
        return true;
    }

    boolean verifyConnection(String id){
        if(isIDblocked(id)) return false;
        return true;
    }

    private boolean isIDblocked(String id) {
        for(Object[] blockedID : blockedIDs){
            if(blockedID[0].equals(id)) return true;
        }
        return false;
    }

    private boolean isMuted(Connection connection) {
        for(Object[] mutedAddress : mutedAddresses){
            if(mutedAddress[0].equals(connection.getAddress())) return true;
        }

        if(((ServerConnection)connection).getId() == null) return false;

        for(Object[] mutedId : mutedIDs){
            if(mutedId[0].equals(new String(((ServerConnection)connection).getId()))) return true;
        }

        return false;
    }

    private boolean isAddressBlocked(InetAddress inetAddress) {
        for(Object[] blockedAddress : blockedAddresses){
            if(blockedAddress[0].equals(inetAddress.getHostAddress())) return true;
        }
        return false;
    }

    boolean verifyMessage(Connection connection){
        if(isMuted(connection)) return false;

        if(Config.getIntValue("autoMuteEnabled") == 1){
            for(Object[] object : messagesReceived){
                if(object[0].equals(connection.getAddress())){
                    if((Integer)(object[1]) >= Config.getIntValue("messagesPerMinute")){
                        muteAddresses((String[]) object[0],Config.getIntValue("messagesPerMinute_blocklength"));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void blockAll(int minutes) {
        blockConnections = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                blockConnections = false;
            }
        }, minutes * 60 * 1000);
    }

    private void unblockAll(){
        blockConnections = false;
    }

    private void timedTask(){
        for(Object[] object : connectionAttempts){
            object[1] = 0;
            attemptCounter = 0;
        }
        ArrayList<String> addressesToUnblock = new ArrayList<>();
        for(Object[] object : blockedAddresses){
            if(((Date) object[1]).after(new Date())) addressesToUnblock.add((String) object[0]);
        }
        if(addressesToUnblock.size() == 0) return;
        unblockAddresses(addressesToUnblock.toArray(new String[0]));
    }

    void blockAddresses(String[] addresses, Integer minutes){
        for(String address : addresses) {
            Calendar currentDate = Calendar.getInstance();
            long timeInMillis = currentDate.getTimeInMillis();
            Date expiryDate = new Date(timeInMillis + (minutes * 60000));
            blockedAddresses.add(new Object[]{address, expiryDate});

            ArrayList<ServerConnection> connectionsToClose = new ArrayList<>();
            for(ServerConnection serverConnection : server.getConnections()){
                if(serverConnection.getAddress().equals(address)) {
                    connectionsToClose.add(serverConnection);
                }
            }
            for(ServerConnection serverConnection : connectionsToClose) {
                try {
                    serverConnection.close();
                } catch (IOException e) { }
            }
        }
        saveCriminals();
    }


    void unblockAddresses(String[] addresses){
        if(addresses.length==0) return;
        for(String address : addresses) {
            for (int i = 0; i < blockedAddresses.size(); i++) {
                if (blockedAddresses.get(i)[0].equals(address)) blockedAddresses.remove(i);
            }
        }
        saveCriminals();
    }

    @SuppressWarnings("Duplicates")
    void muteAddresses(String[] addresses, Integer minutes){
        for(String address : addresses) {
            Calendar currentDate = Calendar.getInstance();
            long timeInMillis = currentDate.getTimeInMillis();
            Date expiryDate = new Date(timeInMillis + (minutes * 60000));
            mutedAddresses.add(new Object[]{address, expiryDate});
        }
        saveCriminals();
    }

    void unmuteAddresses(String[] addresses){
        for(String address : addresses) {
            for (int i = 0; i < mutedAddresses.size(); i++) {
                if (mutedAddresses.get(i)[0].equals(address)) mutedAddresses.remove(i);
            }
        }
        saveCriminals();
    }

    void blockUsers(String[] id, Integer minutes){
        for(String address : id) {
            Calendar currentDate = Calendar.getInstance();
            long timeInMillis = currentDate.getTimeInMillis();
            Date expiryDate = new Date(timeInMillis + (minutes * 60000));
            blockedIDs.add(new Object[]{address, expiryDate});

            ArrayList<ServerConnection> connectionsToClose = new ArrayList<>();
            for(ServerConnection serverConnection : server.getConnections()){
                if(serverConnection.getId().equals(address)) {
                    connectionsToClose.add(serverConnection);
                }
            }
            for(ServerConnection serverConnection : connectionsToClose) {
                try {
                    serverConnection.close();
                } catch (IOException e) { }
            }
        }
        saveCriminals();
    }


    void unblockUsers(String[] id){
        if(id.length==0) return;
        for(String address : id) {
            for (int i = 0; i < blockedIDs.size(); i++) {
                if (blockedIDs.get(i)[0].equals(address)) blockedIDs.remove(i);
            }
        }
        saveCriminals();
    }

    @SuppressWarnings("Duplicates")
    void muteUsers(String[] id, Integer minutes){
        for(String address : id) {
            Calendar currentDate = Calendar.getInstance();
            long timeInMillis = currentDate.getTimeInMillis();
            Date expiryDate = new Date(timeInMillis + (minutes * 60000));
            mutedIDs.add(new Object[]{address, expiryDate});
        }
        saveCriminals();
    }

    void unmuteUsers(String[] id){
        for(String address : id) {
            for (int i = 0; i < mutedIDs.size(); i++) {
                if (mutedIDs.get(i)[0].equals(address)) mutedIDs.remove(i);
            }
        }
        saveCriminals();
    }

    public boolean isBlocked(){
        return blockConnections;
    }

    public static void generateDefaultCriminals(){
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("criminals.txt"), charset)) {
            writer.flush();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        System.out.println("Successfully generated criminals.txt");
    }

    private boolean loadCriminals(){
        Charset charset = Charset.forName("US-ASCII");
        String line = null;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("criminals.txt"), charset)) {
            while ((line = reader.readLine()) != null) {
                if(line.length()<=1) break;
                Object[] address = new Object[2];
                address[0] = line.substring(5,line.indexOf("%"));
                Date date = new Date();
                date.setTime(Long.parseLong(line.substring(line.indexOf("%")+1)));
                address[1] = date;
                if(line.startsWith("B_ip:")) blockedAddresses.add(address);
                else if(line.startsWith("M_ip:")) mutedAddresses.add(address);
                else if(line.startsWith("B_id:")) blockedIDs.add(address);
                else if(line.startsWith("M_id:")) mutedIDs.add(address);
            }
            reader.close();
            System.out.println("Successfully loaded criminals.txt");
            return true;
        } catch (IOException x) {
            generateDefaultCriminals();
            return false;
        }
    }

    //TODO: add ID blocks and mutes
    private void saveCriminals(){
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("criminals.txt"), charset)) {
            for(Object[] blockedAddress : blockedAddresses){
                writer.write("B_ip:" + blockedAddress[0] + "%" + ((Date)blockedAddress[1]).getTime());
                writer.newLine();
            }
            for(Object[] mutedAddress : mutedAddresses){
                writer.write("M_ip:" + mutedAddress[0] + "%" + ((Date)mutedAddress[1]).getTime());
                writer.newLine();
            }
            for(Object[] blockedAddress : blockedIDs){
                writer.write("B_id:" + blockedAddress[0] + "%" + ((Date)blockedAddress[1]).getTime());
                writer.newLine();
            }
            for(Object[] mutedAddress : mutedIDs){
                writer.write("M_id:" + mutedAddress[0] + "%" + ((Date)mutedAddress[1]).getTime());
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public void addID(String address, String id){
        for(int i = 0; i < clientIDs.size(); i++){
            if(clientIDs.get(i)[0].equals(id)){
                clientIDs.get(i)[1] = address;
                saveIDlist();
                return;
            }
        }
        clientIDs.add(new String[]{id,address});
        saveIDlist();
    }

    public String[] getID(String address){
        ArrayList<String> IDs = new ArrayList<>();
        for(Object[] client : clientIDs) if(client[1].equals(address)) IDs.add((String) client[0]);
        return IDs.toArray(new String[0]);
    }

    public String getAddress(String id){
        for(Object[] client : clientIDs) if(client[0].equals(id)) return ((String) client[1]);
        return null;
    }

    public static void generateDefaultIDlist(){
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("clients.txt"), charset)) {
            writer.flush();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        System.out.println("Successfully generated clients.txt");
    }

    private boolean loadIDlist(){
        Charset charset = Charset.forName("US-ASCII");
        String line = null;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("clients.txt"), charset)) {
            while ((line = reader.readLine()) != null) {
                if(line.length()<=1) break;
                clientIDs.add(new String[]{line.substring(0,line.indexOf(":")), line.substring(line.indexOf(":")+1)});
            }
            reader.close();
            System.out.println("Successfully loaded clients.txt");
            return true;
        } catch (IOException x) {
            generateDefaultIDlist();
            return false;
        }
    }

    private void saveIDlist(){
        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("clients.txt"), charset)) {
            for(Object[] clientID : clientIDs){
                writer.write(clientID[0] + ":" + clientID[1]);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

}
