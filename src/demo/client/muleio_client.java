package demo.client;

import client.Client;
import client.ClientConnection;
import java.util.Scanner;

public class muleio_client {

    private static final String address = "localhost";
    private static final int port = 2929;
    private static int reconnectAttempts = 3;
    private static Client client;

    public static void main(String[] args) throws Exception {

        client = new Client() {

            @Override
            public void onReceived(ClientConnection connection, byte[] id, byte[] content) {

                if(new String(id).equals("broadcast")){
                    System.out.println("server: " + new String(content));
                } else if(new String(id).startsWith("whisper")) {
                    System.out.println("(whisper) " + new String(id).substring("whisper@".length()) + ": " + getParam(new String(content),0,-1));
                } else if(new String(id).equals("error")) {
                    System.out.println("ERROR: " + new String(content));
                } else {
                    System.out.println(new String(id) + ": " + new String(content));
                }

            }

            @Override
            public void onRequested(ClientConnection connection, byte[] requestId, byte[] id, byte[] content) {

            }

            @Override
            public void onRequestTimeout(ClientConnection connection, byte[] id, byte[] content) {

            }

            @Override
            public void onSent(ClientConnection connection, byte[] id, byte[] content) {
                if(new String(id).equals("broadcast")) System.out.println(new String(this.getId()) + ": " + new String(content));
            }

            @Override
            protected void onDataStreamError(ClientConnection connection) {
                System.out.println("Data stream error");
            }

            @Override
            protected void onDisconnected(ClientConnection connection, Exception e) {
                System.out.println("Disconnected from " + connection.getAddress());
            }

            @Override
            protected void connectionFailed(ClientConnection connection, Exception e) {
                System.out.println("Failed to connect to " + connection.getAddress());

                if(reconnectAttempts == 0) return;
                System.out.println("Trying to reconnect...");
                reconnectAttempts--;
                connect(address, port);
            }


            @Override
            protected void onConnecting(ClientConnection connection) {
                System.out.println("Connecting to " + connection.getAddress() + "...");
            }

            @Override
            public void onConnected(ClientConnection connection) {
                System.out.println("Successfully connected to " + connection.getAddress());
                System.out.println("Commands:");
                System.out.println("/help");
                System.out.println("/whois [id]");
                System.out.println("/whisper [id or ip inetAddress] [message]");
            }
        };

        System.out.println("Please enter your name to connect to the server.");
        String name;

        while (true){
            name = new Scanner(System.in).nextLine();
            if(name.contains(" ")) System.out.println("Your name cannot contain any spaces. Please enter a new one.");
            else break;
        }

        client.setId(name.getBytes());

        client.connect(address, port);

        String msg = "";
        try {
            while (!msg.equals("exit")) {
                msg = new Scanner(System.in).nextLine();
                if (!checkForCommand(msg)) client.send("broadcast", msg.getBytes());
            }
        } catch (Exception e){
//            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean checkForCommand(String msg) {
        if(!msg.startsWith("/")) return false;
        if(msg.startsWith("/whisper")){

            try {
                client.send("whisper@" + getParam(msg,1,1), getParam(msg,2,-1).getBytes());
            } catch (Exception e) {  }

        } else if(msg.startsWith("/whois")){

            new Thread(() -> System.out.println(new String(client.request("inetAddress", getParam(msg,1,1).getBytes())))).start();

        } else if(msg.startsWith("/help")){

            System.out.println("Commands:");
            System.out.println("/help");
            System.out.println("/whois");
            System.out.println("/whisper [id or ip inetAddress] [message]");

        } else {
            System.out.println("Unrecognized command");
        }

        return true;
    }

    @SuppressWarnings("Duplicates")
    private static String getParam(String message, int paramNumber, int numberOfWords){
        String param = "";
        int currentParamNumber = 0, currentNumberOfWords = 0, currentIndex = 0;
        do {
            currentParamNumber++;
            if(currentParamNumber > paramNumber && (currentNumberOfWords < numberOfWords || numberOfWords == -1)){
                param += message.substring(currentIndex, message.indexOf(" ", currentIndex) == -1 ? message.length() : message.indexOf(" ", currentIndex)) + " ";
                currentNumberOfWords++;
            }
            currentIndex = message.indexOf(" ", currentIndex)+1;
        } while((currentNumberOfWords < numberOfWords || numberOfWords == -1) && currentIndex != 0);
        return param.trim();
    }

}
