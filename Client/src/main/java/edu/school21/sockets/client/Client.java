package edu.school21.sockets.client;

import com.google.gson.Gson;
import edu.school21.sockets.models.JSONMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    Scanner scanner = new Scanner(System.in);
    Gson gson = new Gson();
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean CONTINUE = true;

    public void start(String ip, int port) throws IOException, InterruptedException {
        try {
            clientSocket = new Socket(ip, port);
        } catch (Exception e) {
            System.err.println("Failed to connect to server");
            System.exit(0);
        }
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        Thread reader = new Thread(() -> {
            try {
                getMessage();
            } catch (IOException ignored) {
            }
        });

        Thread sender = new Thread(() -> {
            try {
                sendMessage();
            } catch (IOException ignored) {
            }
        });

        reader.start();
        sender.start();
        reader.join();
    }

    public void getMessage() throws IOException {
        while (CONTINUE) {
            String msg = in.readLine();
            if (msg == null) {
                CONTINUE = false;
                System.exit(500);
            }
            JSONMessage message = gson.fromJson(msg, JSONMessage.class);
            System.out.println(message.message);
        }
    }

    public void sendMessage() throws IOException {
        while (CONTINUE) {
            if (scanner.hasNext()) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    CONTINUE = false;
                    System.exit(0);
                }
                out.println(gson.toJson(new JSONMessage(message, "null", "null")));
            }
        }
    }

    public void stopConnection() throws IOException {
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (clientSocket != null)
            clientSocket.close();
    }
}
