package demo.server;

import server.ServerConnection;
import server.Server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class muleio_server {

    static Server server;

    public static void main(String[] args) {

        server = new Server() {

            @Override
            public void onReceived(ServerConnection connection, byte[] id, byte[] content) {
                if(new String(id).equals("broadcast")){
                    System.out.println(getBestName(connection) +": "+ new String(content));
                    try {
                        broadcastExcept(connection, new String(connection.getId()), content);
                    } catch (Exception e) { }
                } else if(new String(id).startsWith("whisper")) {
                    try {
                        String receiverID = new String(id).substring("whisper@".length());
                        if(server.getActiveConnectionByIDorAddress(receiverID) == null) {
                            connection.send( "error".getBytes(), "User not online.".getBytes());
                        }else {
                            server.getActiveConnectionByIDorAddress(receiverID).send(("whisper@" + new String(connection.getId())).getBytes(), content);
                        }
                    } catch (Exception e) { }
                }
            }

            @Override
            public void onRequested(ServerConnection connection, byte[] requestId, byte[] id, byte[] content) {
                System.out.println(getBestName(connection) + " requested " + new String(id));
                if(new String(id).equals("address ")) {
                    try {
                        connection.sendResult(requestId, id, getActiveConnectionByIDorAddress(new String(content)).getAddress().getBytes());
                    } catch (Exception e) { }
                }
            }

            @Override
            public void onRequestTimeout(ServerConnection connection, byte[] id, byte[] content) {
                System.out.println(getBestName(connection) + " did not answer the request " + new String(id) + " in time.");

            }

            @Override
            public void onSent(ServerConnection connection, byte[] id, byte[] content) {
                if(new String(id).equals("broadcast") || new String(id).equals("whisper")) System.out.println(">> " + getBestName(connection) + ": (" + new String(id) + ") " + new String(content));
            }

            @Override
            public void onDataStreamError(ServerConnection connection) {
                if(connection.id == null) System.out.println("Communication error with " + getBestName(connection));
            }

            @Override
            protected void onDisconnected(ServerConnection connection, Exception e) {
                super.onDisconnected(connection,e);
                System.out.println(getBestName(connection) + " has disconnected.");
            }

            @Override
            protected void connectionFailed(ServerConnection connection, Exception e) {
                System.out.println("Failed to establish connection with " + connection.getAddress());
            }

            @Override
            public void onConnecting(ServerConnection serverConnection) {
                System.out.println(serverConnection.getAddress() + " is connecting...");
            }

            @Override
            protected void onStarted() {
                System.out.println("The server has successfully started and is listening to connections on port " + getPort());
                System.out.println("Commands:");
                System.out.println("/stop");
                System.out.println("/restart");
                System.out.println("/broadcast");
                System.out.println("/listclients");
                System.out.println("/whois [id]");
                System.out.println("/whisper [id or ip inetAddress] [message]");
                System.out.println("/kick [id]");
                System.out.println("/muteuser [id ] [time in minutes]");
                System.out.println("/unmuteuser [id] [time in minutes]");
                System.out.println("/banuser [id] [time in minutes]");
                System.out.println("/unbanuser [id]");
                System.out.println("/kickaddress [id or ip address]");
                System.out.println("/muteaddress [id or ip address] [time in minutes]");
                System.out.println("/unmuteaddress [id or ip address] ");
                System.out.println("/banaddress [id or ip address] [time in minutes]");
                System.out.println("/unbanaddress [id or ip address] ");
            }

            @Override
            public void onConnected(ServerConnection connection) {
                System.out.println(connection.getAddress() + " has connected.");
            }

            @Override
            public void onIntroduction(ServerConnection connection) {
                super.onIntroduction(connection);
                System.out.println(connection.getAddress() + " has introduced themself as " + new String(connection.id));
            }

            @Override
            protected void onBlockAddress(ServerConnection[] connections) {
                for(ServerConnection connection : connections){
                    System.out.println(getBestName(connection) + " has been blocked.");
                }
            }

            @Override
            protected void onMuteAddress(ServerConnection[] connections) {
                for(ServerConnection connection : connections){
                    System.out.println(getBestName(connection) + " has been muted.");
                }
            }

            @Override
            protected void onUnblockAddress(ServerConnection[] connections) {
                for(ServerConnection connection : connections){
                    System.out.println(getBestName(connection) + " has been unblocked.");
                }
            }

            @Override
            protected void onUnmuteAddress(ServerConnection[] connections) {
                for(ServerConnection connection : connections){
                    System.out.println(getBestName(connection) + " has been unmuted.");
                }
            }

            @Override
            protected void onBlockUser(String id) {
                System.out.println(id + " has been blocked.");
            }

            @Override
            protected void onUnblockUser(String id) {
                System.out.println(id + " has been unblocked.");
            }

            @Override
            protected void onMuteUser(String id) {
                System.out.println(id + " has been muted.");
            }

            @Override
            protected void onUnmuteUser(String id) {
                System.out.println(id + " has been unmuted.");
            }

            @Override
            protected void onKickAddress(ServerConnection[] connections) {
                for(ServerConnection connection : connections){
                    System.out.println(getBestName(connection) + " has been kicked.");
                }
            }

            @Override
            protected void onKickUser(String id) {
                System.out.println(id + " has been kicked.");
            }

            @Override
            protected ServerConnection[] onMultipleConnectionsFromAddress(ServerConnection[] connections) {

                return connections;

//                System.out.println("Your request resulted in multiple connections. Please type the \n" +
//                                    "id or the number of inetAddress you want to use for the previous request.\n" +
//                                    "You can type 'all' for executing the request on all of them.");
//
//                for (ServerConnection connection : connections) System.out.println(getBestName(connection));
//
//                String answer = new Scanner(System.in).nextLine();
//                if(answer.equals("all")){
//                    return connections;
//                } else {
//                    for (ServerConnection connection : connections){
//                        answer.equals(getBestName(connection));
//                    }
//                }
//
//                return null;
            }

        };

        try {
            server.init();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String msg = "";

            while (!msg.equals("exit")) {
                try {
                    msg = new Scanner(System.in).nextLine();
                    if (!checkForCommand(msg)) server.broadcast("broadcast", msg.getBytes());
                } catch (NullPointerException e){
                } catch (NumberFormatException e){
                    System.out.println("Error: Missing argument.");
                } catch (Exception e){
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

    }

    private static boolean checkForCommand(String msg) {
        if(!msg.startsWith("/")) return false;
        if(msg.startsWith("/banaddress")){

            server.blockAddress(getParam(msg,1,1), Integer.parseInt(getParam(msg, 2, 1)));
            server.kickAddress(getParam(msg,1,1));

        } else if(msg.startsWith("/whois")){

            System.out.println(server.getConnectionByIDorAddress(getParam(msg,1,1)).getAddress());

        } else if(msg.startsWith("/kickaddress")){

            server.kickAddress(getParam(msg,1,1));

        } else if(msg.startsWith("/unbanaddress")){

            server.unblockAddress(getParam(msg,1,1));

        } else if(msg.startsWith("/kickuser")){

            server.kick(getParam(msg,1,1));

        } else if(msg.startsWith("/unbanuser")){

            server.unblock(getParam(msg,1,1));

        } else if(msg.startsWith("/banuser")){

            server.block(getParam(msg,1,1), Integer.parseInt(getParam(msg, 2, 1)));
            server.kick(getParam(msg,1,1));

        } else if(msg.startsWith("/stop")){

            server.stop();

        } else if(msg.startsWith("/restart")){

            try {
                server.stop();
                server.init();
            } catch (IOException e) { } catch (NoSuchAlgorithmException e) { }

        } else if(msg.startsWith("/broadcast")){

            try {
                server.broadcast("broadcast", getParam(msg, 1, -1).getBytes());
            } catch (Exception e) { }

        } else if(msg.startsWith("/whisper")){

            try {
                server.getActiveConnectionByIDorAddress(getParam(msg,1,1)).send( "whisper@server".getBytes(), getParam(msg,2,-1).getBytes());
            } catch (Exception e) {  }

        } else if(msg.startsWith("/muteaddress")){

            server.muteAddress(getParam(msg,1,1), Integer.parseInt(getParam(msg, 2, 1)));

        } else if(msg.startsWith("/unmuteaddress")){

            server.unmuteAddress(getParam(msg,1,1));

        } else if(msg.startsWith("/muteuser")){

            server.mute(getParam(msg,1,1), Integer.parseInt(getParam(msg, 2, 1)));

        } else if(msg.startsWith("/unmuteuser")){

            server.unmute(getParam(msg,1,1));

        } else if(msg.startsWith("/listclients")){

            for(ServerConnection connection : server.getConnections()){
                System.out.println(connection.getAddress() + ":" + connection.getSourcePort() + " (" + (connection.getId() == null ? "no id" : new String(connection.getId())) + ")" );
            }

        }else if(msg.startsWith("/help")){

            System.out.println("Commands:");
            System.out.println("/stop");
            System.out.println("/restart");
            System.out.println("/broadcast");
            System.out.println("/listclients");
            System.out.println("/whois [id]");
            System.out.println("/whisper [id or ip inetAddress] [message]");
            System.out.println("/kick [id]");
            System.out.println("/muteuser [id ] [time in minutes]");
            System.out.println("/unmuteuser [id] [time in minutes]");
            System.out.println("/banuser [id] [time in minutes]");
            System.out.println("/unbanuser [id]");
            System.out.println("/kickaddress [id or ip address]");
            System.out.println("/muteaddress [id or ip address] [time in minutes]");
            System.out.println("/unmuteaddress [id or ip address] ");
            System.out.println("/banaddress [id or ip address] [time in minutes]");
            System.out.println("/unbanaddress [id or ip address] ");

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
