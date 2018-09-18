package demo;

import server.ServerConnection;
import server.Server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class muleio_server {

    public static void main(String[] args) throws Exception {

        Server server = new Server() {

            @Override
            public void onReceived(ServerConnection connection, byte[] id, byte[] content) {
                if(new String(id).equals("message")){
                    if(connection.getId() == null){
                        System.out.println(connection.getAddress() +": "+ new String(content));
                    } else {
                        System.out.println(new String(connection.id) +": "+ new String(content));
                    }

                } else {
                    //process hidden message
                }
            }

            @Override
            public void onSent(ServerConnection connection, byte[] id, byte[] content) {
                if(connection.getId() == null) System.out.println(connection.getAddress() + ": (" + new String(id) + ") " + new String(content));
                else System.out.println(new String(connection.id) + ": (" + new String(id) + ") " + new String(content));
            }

            @Override
            public void onDataStreamError(ServerConnection connection) {
                if(connection.id == null) System.out.println("Communication error with " + connection.getAddress());
                else System.out.println("Communication error with " + new String(connection.id));
            }

            @Override
            public void onConnecting(ServerConnection serverConnection) {
                System.out.println(serverConnection.getAddress() + " is connecting...");
            }

            @Override
            public void onConnected(ServerConnection connection) {
                System.out.println(connection.getAddress() + " has connected.");
            }

            @Override
            public void onIntroduction(ServerConnection connection) {
                System.out.println(connection.getAddress() + " has introduced themself as " + new String(connection.id));
            }

        };

        try {
            server.init(2929);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String msg = "";
        while(!msg.equals("exit")){
            msg = new Scanner(System.in).nextLine();
            server.broadcast("message", msg.getBytes());
        }
    }
}
