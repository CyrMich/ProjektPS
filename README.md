Projekt stanowi implementację wielowątkowego serwera czatu oraz dedykowanego klienta konsolowego. Aplikacja wykorzystuje gniazda sieciowe oraz protokół TCP/IP, zapewniając stabilną komunikację w czasie rzeczywistym.


Projekt podzielony jest na dwa główne pakiety: server (logika zarządzania) oraz client (interfejs użytkownika).

Pakiet server:
- ChatServer.java: Centralny punkt systemu. Odpowiada za akceptowanie połączeń, zarządzanie listą aktywnych klientów oraz prowadzenie historii wiadomości.
- ClientHandler.java: Klasa obsługująca konkretnego klienta w osobnym wątku. Realizuje proces logowania, komendy oraz przesyłanie wiadomości.
- ChatMessage.java: Model danych reprezentujący wiadomość. Przechowuje informację o nadawcy, treści, czasie wysłania oraz typie wiadomości.

Pakiet client:
- ChatClient.java: Główna klasa klienta. Odpowiada za połączenie z hostem, wysyłanie danych oraz proces autoryzacji.
- MessageReceiver.java: Wątek pomocniczy, który stale nasłuchuje strumienia wejściowego i wyświetla wiadomości od innych użytkowników bez blokowania klawiatury.


Funkcjonalności i Komendy

Konsola Serwera (Admin) - Administrator może wpisywać komendy bezpośrednio w oknie serwera:

/history – Wyświetla wszystkie wiadomości od momentu startu serwera.
/users – Wyświetla listę zalogowanych użytkowników wraz z ich liczbą.
/quit – Bezpiecznie zamyka serwer.

Konsola Klienta - Użytkownik ma dostęp do następujących poleceń:

/help – Wyświetla pomoc.
/users – Wyświetla listę osób online.
/time – Wyświetla czas systemowy serwera.
/quit lub /exit – Powoduje wylogowanie i zamknięcie aplikacji.


Instrukcja Uruchomienia
1. Uruchom klasę server.ChatServer.
2. Uruchom jedną lub więcej instancji client.ChatClient.
3. W oknie klienta podaj nazwę użytkownika.
