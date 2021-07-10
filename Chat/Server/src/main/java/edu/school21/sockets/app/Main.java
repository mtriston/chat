package edu.school21.sockets.app;

import edu.school21.sockets.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].matches("--port=[0-9]")) {
            System.err.println("Invalid argument! Expected --port=8081");
            System.exit(-1);
        }
        Server server = new Server();
        try {
            server.start(Integer.parseInt(args[0].substring("--port=".length())));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}