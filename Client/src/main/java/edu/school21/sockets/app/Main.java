package edu.school21.sockets.app;

import edu.school21.sockets.client.Client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].matches("--server-port=[0-9]") || args[0].length() <= "--server-port=".length()) {
            System.err.println("Invalid argument! Expected --server-port=8081");
            System.exit(-1);
        }
        Client client = new Client();
        try {
            client.start("127.0.0.1", Integer.parseInt(args[0].substring("--server-port=".length())));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.stopConnection();
                System.err.println("Exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}