package handler;

import listener.ClientListener;
import model.ActionType;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static constants.Constants.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ClientListener listener;
    private String name = "";

    public ClientHandler(Socket clientSocket, ClientListener listener) {
        this.clientSocket = clientSocket;
        this.listener = listener;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String receivedString;
            while (true) {
                receivedString = in.readLine();
                if (receivedString != null) {
                    System.out.println("From Client: " + receivedString);
                    receivedString = receivedString.replace(";", "\n");
                    StringReader reader = new StringReader(receivedString);
                    Properties receivedProperties = new Properties();
                    receivedProperties.load(reader);

                    String value = receivedProperties.getProperty(VALUE);
                    String actionValue = receivedProperties.getProperty(ACTION);
                    String name = receivedProperties.getProperty(NAME);
                    ActionType action = ActionType.fromValue(actionValue);

                    if (action != null) {
                        switch (action) {
                            case AUCTION_BID:
                                listener.onNewBidReceived(Integer.parseInt(value), this);
                                break;
                            case LOGIN:
                                if (name != null) {
                                    this.name = name;
                                }
                                listener.onUserLogin(this);
                                listener.onBidCurrentValueRequest(this);
                                break;
                            case BID_CURRENT_VALUE:
                                listener.onBidCurrentValueRequest(this);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Properties messageProperties) {
        if (!clientSocket.isClosed()) {
            StringWriter writer = new StringWriter();
            try{
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                messageProperties.store(new PrintWriter(writer), "");
                String serializedResponse = writer.getBuffer().toString();
                serializedResponse = serializedResponse.replaceAll("\\n", ";");
                serializedResponse = serializedResponse + "\n";
                out.println(serializedResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClosed() {
        return clientSocket.isClosed();
    }

    public String getName() {
        return name;
    }
}
