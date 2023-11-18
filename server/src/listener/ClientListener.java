package listener;

import handler.ClientHandler;

public interface ClientListener {

    void onNewBidReceived(int newValue, ClientHandler handler);

    void onBidCurrentValueRequest(ClientHandler handler);

    void onUserLogin(ClientHandler handler);
}
