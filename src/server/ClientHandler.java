package server;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private boolean isClosed = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("PODAJ_NAZWE");
            socket.setSoTimeout(30000);
            String inputName = in.readLine();
            socket.setSoTimeout(0);

            if (inputName == null || inputName.trim().isEmpty()) {
                closeConnection();
                return;
            }

            inputName = inputName.trim();

            if (isUsernameTaken(inputName)) {
                out.println("NAZWA_ZAJETA");
                closeConnection();
                return;
            }

            this.username = inputName;
            ChatServer.addClient(this);

            out.println("POLACZONO");
            sendMessageHistory();
            ChatServer.broadcastSystem(username + " dołączył do czatu");
            sendUserList();

            String message;
            while ((message = in.readLine()) != null) {
                if (message.trim().isEmpty()) continue;
                if (message.startsWith("/")) handleCommand(message);
                else ChatServer.broadcast(username + ": " + message, this);
            }

        } catch (IOException e) {
        } finally {
            closeConnection();
        }
    }

    private boolean isUsernameTaken(String name) {
        for (String user : ChatServer.getActiveUsers()) {
            if (user.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public String getUsername() {
        return username;
    }

    private synchronized void closeConnection() {
        if (isClosed) return;
        isClosed = true;

        try {
            if (username != null) {
                ChatServer.broadcastSystem(username + " opuścił czat");
                System.out.println("[ROZŁĄCZENIE] Użytkownik '" + username + "' rozłączył się");
            }
            ChatServer.removeClient(this);
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("[BŁĄD] Zamykanie: " + e.getMessage());
        }
    }

    private void sendMessageHistory() {
        try {
            List<ChatMessage> history = ChatServer.getMessageHistory();

            out.println("=== HISTORIA WIADOMOŚCI (" + history.size() + ") ===");
            for (ChatMessage msg : history) {
                out.println(msg.toString());
            }
            out.println("=== KONIEC HISTORII ===");
            out.flush();
        } catch (Exception e) {
            System.err.println("[BŁĄD] Nie udało się wysłać historii: " + e.getMessage());
        }
    }

    private void sendUserList() {
        out.println("=== AKTYWNI UŻYTKOWNICY (" + ChatServer.getClientCount() + ") ===");
        for (String user : ChatServer.getActiveUsers()) out.println("  - " + user);
        out.println("=================================");
    }

    private void handleCommand(String command) {
        command = command.toLowerCase().trim();
        if (command.equals("/quit") || command.equals("/exit")) {
            out.println("[SYSTEM] Do zobaczenia!");
            closeConnection();
        } else if (command.equals("/help")) {
            out.println("Komendy: /help, /users, /time, /quit");
        } else if (command.equals("/users")) {
            sendUserList();
        } else if (command.equals("/time")) {
            out.println("[SYSTEM] Czas: " + new java.util.Date());
        }
    }
}