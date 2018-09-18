package demo;

import client.Client;
import client.ClientConnection;

import java.util.Scanner;

public class muleio_client {

    public static void main(String[] args) throws Exception {
        Client client = new Client() {

            @Override
            public void onReceived(ClientConnection connection, byte[] id, byte[] content) {

                if(connection.getAddress().equals(this.getConnection().getAddress()) && new String(id).equals("message")){
                    System.out.println("server: " + new String(content));
                } else {
                    //process hidden message
                }

            }

            @Override
            public void onSent(ClientConnection connection, byte[] id, byte[] content) {
                if(new String(id).equals("message")) System.out.println(new String(this.getId()) + ": " + new String(content));
            }

            @Override
            protected void onDataStreamError(ClientConnection connection) {
                System.out.println("Data stream error");
            }

            @Override
            protected void onConnecting(ClientConnection connection) {
                System.out.println("Connecting to " + connection.getAddress() + "...");
            }

            @Override
            public void onConnected(ClientConnection connection) {
                System.out.println("Successfully connected to " + connection.getAddress());
            }
        };

        client.setId("dani".getBytes());
        client.connect("localhost", 2929);

        String msg = "";
        while(!msg.equals("exit")){
            msg = new Scanner(System.in).nextLine();
            client.send("message", msg.getBytes());
        }

    }

}
