package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5555;
    private static final int MAX_CLIENTS = 50;

    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();

    // Zmieniamy List<String> na List<ChatMessage> - teraz historia to obiekty!
    private static List<ChatMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_HISTORY = 100;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("    SERWER CZATU - TCP/IP");
        System.out.println("    Wpisz '/history' aby zobaczyć logi");
        System.out.println("    Wpisz '/users' aby zobaczyć obecnych");
        System.out.println("=================================");

        // Uruchamiamy wątek do obsługi komend administratora (konsola serwera)
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

    // --- Metody zarządzania wiadomościami ---

    public static void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    // Rozgłaszanie wiadomości od użytkownika
    public static void broadcast(String messageContent, ClientHandler sender) {
        // Tworzymy obiekt wiadomości przypisany do konkretnego nadawcy
        ChatMessage msg = new ChatMessage(sender.getUsername(), messageContent, false);
        addToHistory(msg);

        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(msg.toString());
            }
        }
        // Wyświetlamy też na konsoli serwera bieżący ruch
        System.out.println(msg.toString());
    }

    // Rozgłaszanie wiadomości systemowej
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

    // Metoda zwracająca historię (bezpieczna kopia)
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

/**
 * Klasa wewnętrzna do obsługi komend wpisywanych BEZPOŚREDNIO w konsoli serwera
 */
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
                        // Tutaj wyświetlamy sformatowaną wiadomość
                        System.out.println(msg.toString());
                        // Możemy też wyświetlić szczegóły, np: "Nadawca: " + msg.getSender()
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