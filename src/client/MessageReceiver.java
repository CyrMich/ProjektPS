package client;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver implements Runnable {
    private BufferedReader in;
    private volatile boolean running = true;

    public MessageReceiver(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (running) System.err.println("[INFO] Połączenie zakończone.");
        }
    }

    public void stop() {
        running = false;
    }
}