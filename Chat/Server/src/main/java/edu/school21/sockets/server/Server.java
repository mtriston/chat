package edu.school21.sockets.server;

import edu.school21.sockets.config.SocketsApplicationConfig;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.Room;
import edu.school21.sockets.services.MessageService;
import edu.school21.sockets.services.RoomsService;
import edu.school21.sockets.services.UsersService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private final List<ClientHandler> clients = Collections.synchronizedList(new LinkedList<>());
    private List<Room> rooms;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private UsersService usersService;
    private MessageService messageService;
    private RoomsService roomsService;

    public void start(int port) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
        usersService = context.getBean(UsersService.class);
        messageService = context.getBean(MessageService.class);
        roomsService = context.getBean(RoomsService.class);

        serverSocket = new ServerSocket(port);
        System.out.println("The server is running.");

        roomsService.getAllRooms();
        rooms = Collections.synchronizedList(roomsService.getAllRooms());
        while (true) {
            clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(this, clientSocket);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public boolean signIn(String username, String password) {
        return usersService.signIn(username, password);
    }

    public boolean signUp(String username, String password) {
        return usersService.signUp(username, password);
    }

    public void sendMessageToAllClients(String msg, ClientHandler sender) {
        messageService.saveMessage(msg, sender.getUsername(), sender.getRoom());
        for (ClientHandler o : clients) {
            if (o.getRoom().equals(sender.getRoom()) && o != sender) {
                o.sendMessage(msg);
            }
        }
    }

    public List<Message> getRoomHistory(String roomName) {
        Room room = roomsService.getRoomByName(roomName).orElse(null);
        if (room != null) {
            return room.getMessages();
        }
        return null;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Room addRoom(String roomName, String owner) {
        roomsService.saveRoom(roomName, owner);
        Room room = roomsService.getRoomByName(roomName).orElse(null);
        rooms.add(room);
        return room;
    }

    public void removeClient(ClientHandler clientHandler) {
        try {
            clientHandler.disconnect();
        } catch (IOException ignored) {
        }
        clients.remove(clientHandler);
    }

    public void stop() throws IOException {
        clientSocket.close();
        serverSocket.close();
    }
}