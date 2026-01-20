package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private MessageReceiver messageReceiver;
    private boolean isClosed = false;

    public ChatClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            System.out.println("Łączenie z " + SERVER_HOST + ":" + SERVER_PORT + "...");
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[OK] Połączono!");

            if (!login()) return; // Jeśli login zawiedzie, skocz do finally

            messageReceiver = new MessageReceiver(in);
            new Thread(messageReceiver).start();

            handleUserInput();
        } catch (IOException e) {
            System.err.println("[BŁĄD] Połączenie: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private boolean login() throws IOException {
        String serverMessage = in.readLine();
        if ("PODAJ_NAZWE".equals(serverMessage)) {
            System.out.print("Podaj nazwę użytkownika: ");
            String username = scanner.nextLine().trim();
            out.println(username);

            String response = in.readLine();
            if ("NAZWA_ZAJETA".equals(response)) {
                System.out.println("[BŁĄD] Nazwa użytkownika jest już zajęta!");
                return false;
            }
            if ("POLACZONO".equals(response)) {
                System.out.println("Witaj na czacie!");
                return true;
            }
        }
        return false;
    }

    private void handleUserInput() {
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            out.println(msg);
            if (msg.equalsIgnoreCase("/quit") || msg.equalsIgnoreCase("/exit")) break;
        }
    }

    private synchronized void closeConnection() {
        if (isClosed) return;
        isClosed = true;
        try {
            if (messageReceiver != null) messageReceiver.stop();
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("[INFO] Rozłączono z serwerem.");
        } catch (IOException e) {
            // ciche zamknięcie
        }
    }

    public static void main(String[] args) {
        new ChatClient().start();
    }
}