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
            for (int i = messages.size() - 1; i >= 0; --i) {
                if (messages.size() - i > 29)
                    break;
                sendMessage(messages.get(i).getText());
            }
            sendMessage("Start messaging");
            JSONMessage jsonMessage;
            server.sendMessageToAllClients(String.format("server: client %s just arrived", username), this);
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    server.removeClient(this);
                    break;
                }
                jsonMessage = gson.fromJson(message, JSONMessage.class);
                if (jsonMessage.message.equalsIgnoreCase("exit")) {
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
            sendMessage("1. signIn");
            sendMessage("2. SignUp");
            sendMessage("3. Exit");
            sendMessage("Choose an option:");
            try {
                cmd = Integer.parseInt(getMessage());
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (cmd == 3)
                return false;
            sendMessage("Enter username:");
            username = getMessage();
            sendMessage("Enter password:");
            String password = getMessage();
            if (cmd == 1) {
                if (server.signIn(username, password))
                    return true;
                else
                    sendMessage("Incorrect login or password.");
            } else if (cmd == 2) {
                if (server.signUp(username, password))
                    return true;
                else
                    sendMessage("This username already exists.");
            }
        }
    }

    private boolean chooseRoom() throws IOException {
        while (true) {
            int cmd;
            sendMessage("1. Create room");
            sendMessage("2. Choose room");
            sendMessage("3. Exit");
            sendMessage("Choose an option:");
            try {
                cmd = Integer.parseInt(getMessage());
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (cmd == 3)
                return false;
            if (cmd == 1) {
                sendMessage("Enter room name:");
                String roomName = getMessage();
                if (!server.isRoomNameUnique(roomName)) {
                    sendMessage("This room name already exists.");
                    continue;
                }
                room = server.addRoom(roomName, username);
                return true;
            } else if (cmd == 2) {
                if (server.getRooms().size() == 0) {
                    sendMessage("No rooms! Try to create one.");
                    continue;
                }
                sendMessage("Rooms:");
                final int[] i = {1};
                server.getRooms().forEach(r -> sendMessage(i[0]++ + ". " + r.getName()));
                sendMessage(i[0] + ". Exit");
                try {
                    cmd = Integer.parseInt(getMessage());
                } catch (NumberFormatException ignored) {
                    continue;
                }
                if (cmd == i[0])
                    return false;
                if (cmd < 0 || cmd > server.getRooms().size())
                    continue;
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
        return gson.fromJson(json, JSONMessage.class).message;
    }

    public String getUsername() {
        return username;
    }

    public String getRoom() {
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