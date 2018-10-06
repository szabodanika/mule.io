# Mule.io

Mule.io is a light weight event-driven socket communication framework written in Java. It can be used in various environments, currently mostly usable as a quick but appropriate solution for small-scale projects in need of safe server-client communication.

- Mule.io uses AES and RSA for hybrid encryption, communication is both secure and fast. Also modifying the client does not let you connect to the server without authorization
- Event-driven messaging makes development easier. You can set ID's for different kind of messages that helps you create your own message handler
- Super simple configuration for your  server and client

Currently looking for testers and contributors

### Demo

You can find the demo classes (Server and Client) in the Demo package, they let you try a simple messaging application built on mule.io.

### Setting up the server

First the server will generate the config and other files, and on the second run it will start running on the port specified in the config file.

```sh
public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    Server server = new Server() {
        //called when a connected and authorized client sends a message
        @Override 
        public void onReceived(ServerConnection connection, byte[] id, byte[] content) {}
        
        //called when a connected and authorized client sends a request
        @Override 
        public void onRequested(ServerConnection connection, byte[] bytes, byte[] bytes1, byte[] bytes2) {
            new byte[] content = new byte[0];
            //this is how you send results for a request
            connection.sendResult(requestId, id, content);
        }
        
        //called when the server sends a request to a client but it does not reply in time
        @Override 
        public void onRequestTimeout(ServerConnection connection, byte[] bytes, byte[] bytes1) {}
        
        //called when a message is sent to a client
        @Override 
        public void onSent(ServerConnection serverConnection, byte[] id, byte[] content) {}
        
        //called when the calculated and reported checksum does not match for a message
        @Override 
        protected void onDataStreamError(ServerConnection serverConnection) {}
        
        //called a client disconnects
        @Override
        protected void onDisconnected(ClientConnection connection, Exception e) {}
        
        //called when a client initiated a connection but something happened
        @Override 
        protected void connectionFailed(ServerConnection connection, Exception e) {}
        
        //called when a client started the connection process
        @Override
        protected void onConnecting(ServerConnection serverConnection) {}
        
        //called on server startup
        @Override
        protected void onStarted() {}
        
        //called on successful client connection
        @Override
        public void onConnected(ServerConnection serverConnection) {}
        
        //called when a client finishes the connection process by sending their ID
        @Override
        public void onIntroduction(ServerConnection connection) {}
        
        //called when an ip gets blocked
        @Override
        protected void onBlockAddress(ServerConnection[] serverConnections) {}
        
        //called when an ip gets muted 
        @Override
        protected void onMuteAddress(ServerConnection[] serverConnections) {}
        
        //called when an ip gets unblocked
        @Override
        protected void onUnblockAddress(ServerConnection[] serverConnections) {}
        
        //called when an ip gets unmuted
        @Override
        protected void onUnmuteAddress(ServerConnection[] serverConnections) {}
        
        //called when an ID gets blocked
        @Override
        protected void onBlockUser(String id) {}
        
        //called when an ID gets unblocked
        @Override
        protected void onUnblockUser(String id) {}
        
        //called when an ID gets muted
        @Override
        protected void onMuteUser(String id) {}
        
        //called when an ID gets unmuted
        @Override
        protected void onUnmuteUser(String id) {}
        
        //called when all connections from an address gets closed
        @Override
        protected void onKickAddress(ServerConnection[] serverConnections) {}
        
        //called when an individual client gets kicked
        @Override
        protected void onKickUser(String id) {}
        
        //called when a command that searches for connections with a certain address or id results in multiple connections
        //you have to return all that you want to run the command for
        @Override
        protected ServerConnection[] onMultipleConnectionsFromAddress(ServerConnection[] serverConnections) {
            return new ServerConnection[0];
        }
    };
    
    server.init();
    
}
```

### Setting up the client
```sh
public static void main(String[] args) {
    Client client = new Client() {
        //called when the client received a message from the server after authorization
        @Override
        public void onReceived(ClientConnection connection, byte[] id, byte[] content) {}
        
        //called when the server sends a request
        //answering method same as in server
        @Override
        public void onRequested(ClientConnection connection, byte[] requestId, byte[] id, byte[] content) {}
        
        //called when the server does not answer the request in time
        @Override
        public void onRequestTimeout(ClientConnection connection, byte[] id, byte[] content) {}
        
        //called when a message is sent to the server after authorization
        @Override
        public void onSent(ClientConnection connection, byte[] id, byte[] content) {}
        
        //called when calculated and reported checksums do not match for a message
        @Override
        protected void onDataStreamError(ClientConnection connection) {}
        
        //called when disconnecting from the server
        @Override
        protected void onDisconnected(ClientConnection connection, Exception e) {}
        
        //called when the client tried to connect to a server but failed (either by error or refusal)
        @Override
        protected void connectionFailed(ClientConnection connection, Exception e) {}
        
        //called when trying to connect to a server
        @Override
        protected void onConnecting(ClientConnection connection) {}
        
        //called when a connection is successful
        @Override
        public void onConnected(ClientConnection connection) {}
    };

    int SERVER_PORT = 0;
    client.setId("ID"); //THE SERVER WILL CLOSE THE CONNECTION IF IT DOES NOT RECEIVE A VALID ID
    client.connect("SERVER_ADDRESS", SERVER_PORT);
}
```

### Communication basics

These are the same for clients and servers, and they get the same methods called in each other, 
the only difference is that in the server's receiver and requester methods it gets the connection object of 
the sender client.


```sh
public static void main(String[] args) {

    MyClient client = new MyClient();
    int SERVER_PORT = 0;
    client.setId("ID"); //THE SERVER WILL CLOSE THE CONNECTION IF IT DOES NOT RECEIVE A VALID ID
    client.connect("SERVER_ADDRESS", SERVER_PORT);
    
    
    //sends the message "Hello" in byte array format with the id "greeting"
    String message = "Hello";
    client.send("greeting", message.getBytes());
    
    
    //always send requests on a separate thread, if you don't want to slow down the main thread!!!
    //sends a request to the server with the id "address" and message "client82734"
    //the server could be set for example to send the address of that client as a result
    new Thread(() -> {
        String request = "client82734";
        byte[] result = client.request("address", request.getBytes());
    });
}
```

### Projects using mule.io

If you use mule.io in your project, please tell us so we can provide specialized support for it, and maybe even showcase it as an example.
Until there are no sample projects available, please take a look at the Demo classes.


### Changelog

Mule.io is continuously developed, with better stability and a few new functions in each release.
Currently looking for contributors and testers.

| Version | Date |Changes |
| ------ | ------ | ------ |
| 1.0 | 2018. 09. 18 | Initial release |
| 1.1 | 2018. 10. 06 | <ul><li>New firewall with ip and id blocking, muting and kicking</li><li>Added support for requests</li><li>Some configurations are now set in a file</li><li>Id's or now compulsory to connect, and they are saved in a file</li><li>No special characters in id's</li></ul> |

### Contact

Dániel Szabó <br/>
szabo.daniel@mail.com