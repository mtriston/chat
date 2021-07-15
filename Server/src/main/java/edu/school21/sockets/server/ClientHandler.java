package edu.school21.sockets.server;

import com.google.gson.Gson;
import edu.school21.sockets.models.JSONMessage;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    public static final String DELIMITER = "------------------";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    private final Gson gson;
    private final Server server;
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Room room;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            if (!logIn() || !chooseRoom()) {
                server.removeClient(this);
                return;
            }
            List<Message> messages = server.getRoomHistory(room.getName());
            if (messages.size() > 30)
                messages = messages.subList(messages.size() - 30, messages.size());
            messages.forEach(m -> sendMessage(m.getText()));

            sendMessage(ANSI_GREEN + "Start messaging" + ANSI_RESET);
            JSONMessage jsonMessage;
            server.sendMessageToAllClients(String.format("%sserver: client %s just arrived%s", ANSI_GREEN, username, ANSI_RESET), this);
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    server.sendMessageToAllClients(String.format("%sserver: client %s just left%s", ANSI_YELLOW, username, ANSI_RESET), this);
                    server.removeClient(this);
                    break;
                }
                jsonMessage = gson.fromJson(message, JSONMessage.class);
                if (jsonMessage.message.equalsIgnoreCase("exit")) {
                    server.sendMessageToAllClients(String.format("%sserver: client %s just left%s", ANSI_YELLOW, username, ANSI_RESET), this);
                    server.removeClient(this);
                    break;
                }
                server.sendMessageToAllClients(username + ": " + jsonMessage.message, this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            server.removeClient(this);
        } finally {
            try {
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean logIn() throws IOException {
        sendMessage("Hello from Server!");
        int cmd;
        while (true) {
            sendMessage(DELIMITER);
            sendMessage("1. signIn");
            sendMessage("2. SignUp");
            sendMessage("3. Exit");
            sendMessage("Choose an option:");
            sendMessage(DELIMITER);
            try {
                cmd = Integer.parseInt(getMessage());
            } catch (NumberFormatException ignored) {
                sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                continue;
            }
            if (cmd == 3)
                return false;
            if (cmd < 1 || cmd > 3) {
                sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                continue;
            }
            sendMessage("Enter username:");
            username = getMessage();
            sendMessage("Enter password:");
            String password = getMessage();
            if (cmd == 1) {
                if (server.signIn(username, password)) {
                    sendMessage(ANSI_GREEN + "You have successfully signed in" + ANSI_RESET);
                    return true;
                }
                else
                    sendMessage(ANSI_RED + "Incorrect login or password." + ANSI_RESET);
            } else if (cmd == 2) {
                if (server.signUp(username, password)) {
                    sendMessage(ANSI_GREEN + "You have successfully signed up and signed in" + ANSI_RESET);
                    return true;
                }
                else
                    sendMessage(ANSI_RED +"This username already exists." + ANSI_RESET);
            }
        }
    }

    private boolean chooseRoom() throws IOException {
        while (true) {
            int cmd;
            sendMessage(DELIMITER);
            sendMessage("1. Create room");
            sendMessage("2. Choose room");
            sendMessage("3. Exit");
            sendMessage("Choose an option:");
            sendMessage(DELIMITER);
            try {
                cmd = Integer.parseInt(getMessage());
            } catch (NumberFormatException ignored) {
                sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                continue;
            }
            if (cmd == 3)
                return false;
            if (cmd < 1 || cmd > 3) {
                sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                continue;
            }
            if (cmd == 1) {
                sendMessage("Enter room name:");
                String roomName = getMessage();
                if (!server.isRoomNameUnique(roomName)) {
                    sendMessage(ANSI_RED + "This room name already exists." + ANSI_RESET);
                    continue;
                }
                room = server.addRoom(roomName, username);
                return true;
            } else if (cmd == 2) {
                if (server.getRooms().size() == 0) {
                    sendMessage(ANSI_RED + "No rooms! Try to create one." + ANSI_RESET);
                    continue;
                }
                sendMessage(DELIMITER);
                sendMessage("Rooms:");
                final int[] i = {1};
                server.getRooms().forEach(r -> sendMessage(i[0]++ + ". " + r.getName()));
                sendMessage(i[0] + ". Exit\n");
                sendMessage("Choose an option:");
                sendMessage(DELIMITER);
                try {
                    cmd = Integer.parseInt(getMessage());
                } catch (NumberFormatException ignored) {
                    sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                    continue;
                }
                if (cmd == i[0])
                    return false;
                if (cmd < 0 || cmd > server.getRooms().size()) {
                    sendMessage(ANSI_RED + "Invalid option" + ANSI_RESET);
                    continue;
                }
                room = server.getRooms().get(cmd - 1);
                return true;
            }
        }
    }

    public void sendMessage(String msg) {
        String roomName = room == null ? "null" : room.getName();
        out.println(gson.toJson(new JSONMessage(msg, username, roomName)));
    }

    private String getMessage() throws IOException {
        String json = in.readLine();
        if (json == null)
            return "null";
        return gson.fromJson(json, JSONMessage.class).message;
    }

    public String getUsername() {
        return username;
    }

    public String getRoom() {
        if (room == null)
            return null;
        return room.getName();
    }

    public void disconnect() throws IOException {
        if (out != null)
            out.close();
        if (in != null)
            in.close();
        if (clientSocket.isConnected())
            clientSocket.close();
    }
}