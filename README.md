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
            @Override
            public void onReceived(ServerConnection serverConnection, byte[] bytes, byte[] bytes1) {}
            @Override
            public void onRequested(ServerConnection connection, byte[] bytes, byte[] bytes1, byte[] bytes2) {}
            @Override
            public void onRequestTimeout(ServerConnection connection, byte[] bytes, byte[] bytes1) {}
            @Override
            public void onSent(ServerConnection serverConnection, byte[] bytes, byte[] bytes1) {}
            @Override
            protected void onDataStreamError(ServerConnection serverConnection) {}
            @Override
            protected void connectionFailed(ServerConnection connection, Exception e) {}
            @Override
            protected void onConnecting(ServerConnection serverConnection) {}
            @Override
            protected void onStarted() {}
            @Override
            public void onConnected(ServerConnection serverConnection) {}
            @Override
            protected void onBlockAddress(ServerConnection[] serverConnections) {}
            @Override
            protected void onMuteAddress(ServerConnection[] serverConnections) {}
            @Override
            protected void onUnblockAddress(ServerConnection[] serverConnections) {}
            @Override
            protected void onUnmuteAddress(ServerConnection[] serverConnections) {}
            @Override
            protected void onBlockUser(String s) {}
            @Override
            protected void onUnblockUser(String s) {}
            @Override
            protected void onMuteUser(String s) {}
            @Override
            protected void onUnmuteUser(String s) {}
            @Override
            protected void onKickAddress(ServerConnection[] serverConnections) {}
            @Override
            protected void onKickUser(String s) {}
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
            @Override
            public void onReceived(ClientConnection connection, byte[] bytes, byte[] bytes1) {}
            @Override
            public void onRequested(ClientConnection connection, byte[] bytes, byte[] bytes1, byte[] bytes2) {}
            @Override
            public void onRequestTimeout(ClientConnection connection, byte[] bytes, byte[] bytes1) {}
            @Override
            public void onSent(ClientConnection connection, byte[] bytes, byte[] bytes1) {}
            @Override
            protected void onDataStreamError(ClientConnection connection) {}
            @Override
            protected void onDisconnected(ClientConnection connection, Exception e) {}
            @Override
            protected void connectionFailed(ClientConnection connection, Exception e) {}
            @Override
            protected void onConnecting(ClientConnection connection) {}
            @Override
            public void onConnected(ClientConnection connection) {}
        };

        int SERVER_PORT = 0;
        client.setId("ID"); //THE SERVER WILL CLOSE THE CONNECTION IF IT DOES NOT RECEIVE A VALID ID
        client.connect("SERVER_ADDRESS", SERVER_PORT);
    }
```

### Projects using mule.io

If you use mule.io in your project and you would be happy to showcase it as a sample, please let us know.
Until there are no sample projects available, please take a look at the Demo classes.


### Changelog

Mule.io is continuously developed, with better stability and a few new functions in each release.

| Version | Date |Changes |
| ------ | ------ | ------ |
| 1.0 | 2018. 09. 18 | Initial release |
| 1.1 | 2018. 10. 06 | <ul><li>New firewall with ip and id blocking, muting and kicking</li><li>Added support for requests</li><li>Some configurations are now set in a file</li><li>Id's or now compulsory to connect, and they are saved in a file</li><li>No special characters in id's</li></ul> |

