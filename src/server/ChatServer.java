package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5555;
    private static final int MAX_CLIENTS = 50;

    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();

    private static List<ChatMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_HISTORY = 100;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("    SERWER CZATU - TCP/IP");
        System.out.println("    Wpisz '/history' aby zobaczyć logi");
        System.out.println("    Wpisz '/users' aby zobaczyć obecnych");
        System.out.println("=================================");

        new Thread(new ServerCommandListener()).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[INFO] Serwer nasłuchuje na porcie: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }
    public static void broadcast(String messageContent, ClientHandler sender) {
        ChatMessage msg = new ChatMessage(sender.getUsername(), messageContent, false);
        addToHistory(msg);

        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(msg.toString());
            }
        }
        System.out.println(msg.toString());
    }
    public static void broadcastSystem(String messageContent) {
        ChatMessage msg = new ChatMessage("SERWER", messageContent, true);
        addToHistory(msg);

        for (ClientHandler client : clientHandlers) {
            client.sendMessage(msg.toString());
        }
        System.out.println(msg.toString());
    }
    private static void addToHistory(ChatMessage msg) {
        synchronized (messageHistory) {
            if (messageHistory.size() >= MAX_HISTORY) {
                messageHistory.remove(0);
            }
            messageHistory.add(msg);
        }
    }
    public static List<ChatMessage> getMessageHistory() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
    public static List<String> getActiveUsers() {
        List<String> users = new ArrayList<>();
        for (ClientHandler client : clientHandlers) {
            users.add(client.getUsername());
        }
        return users;
    }

    public static int getClientCount() {
        return clientHandlers.size();
    }
}
class ServerCommandListener implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("/history")) {
                    System.out.println("\n=== PEŁNA HISTORIA SERWERA ===");
                    List<ChatMessage> history = ChatServer.getMessageHistory();
                    if(history.isEmpty()) System.out.println("(Pusto)");
                    for (ChatMessage msg : history) {
                        System.out.println(msg.toString());
                    }
                    System.out.println("==============================\n");

                } else if (command.equalsIgnoreCase("/users")) {
                    System.out.println("\n=== AKTYWNI UŻYTKOWNICY (" + ChatServer.getClientCount() + ") ===");
                    for (String user : ChatServer.getActiveUsers()) {
                        System.out.println(" - " + user);
                    }
                    System.out.println("==============================\n");
                } else if (command.equalsIgnoreCase("/quit")) {
                    System.out.println("Zamykanie serwera...");
                    System.exit(0);
                } else {
                    System.out.println("[SERVER ADMIN] Nieznana komenda. Dostępne: /history, /users, /quit");
                }
            }
        }
    }
}