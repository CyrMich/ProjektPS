package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;
    private boolean isSystemMessage;

    public ChatMessage(String sender, String content, boolean isSystemMessage) {
        this.sender = sender;
        this.content = content;
        this.isSystemMessage = isSystemMessage;
        this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    // Metoda formatująca wiadomość do wysłania klientowi
    @Override
    public String toString() {
        if (isSystemMessage) {
            return "[" + timestamp + "] [SYSTEM] " + content;
        }
        return "[" + timestamp + "] " + sender + ": " + content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
}