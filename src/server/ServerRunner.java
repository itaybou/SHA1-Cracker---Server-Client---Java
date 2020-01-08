package server;

public class ServerRunner {

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.run();
    }
}
