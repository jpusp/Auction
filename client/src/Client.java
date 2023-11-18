import constants.Constants;
import listener.UIListener;
import model.ActionType;
import ui.AuctionUI;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static constants.Constants.*;
import static model.ActionType.*;


public class Client implements UIListener {
    private AtomicInteger currentMinBid = new AtomicInteger(0);
    private Socket socket;
    private String name;

    private AuctionUI ui;

    public Client() {
        SwingUtilities.invokeLater(() -> ui = new AuctionUI(this));
    }

    public void start() {
        try {
            socket = new Socket("127.0.0.1", Constants.SERVER_PORT);
            SwingUtilities.invokeLater(() -> ui.setConnectionStatus(true));
            Properties bidValueRequest = new Properties();
            bidValueRequest.setProperty(ACTION, LOGIN.getValue());
            bidValueRequest.setProperty(VALUE, String.valueOf(0));
            bidValueRequest.setProperty(NAME, name);
            sendMessage(bidValueRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("FromServer: " + fromServer);
                fromServer = fromServer.replace(";", "\n");

                StringReader reader = new StringReader(fromServer);
                Properties receivedProperties = new Properties();
                receivedProperties.load(reader);

                String actionValue = receivedProperties.getProperty(ACTION);
                String value = receivedProperties.getProperty(VALUE);
                String name = receivedProperties.getProperty(NAME);
                ActionType action = ActionType.fromValue(actionValue);

                if (action != null) {
                    switch (action) {
                        case TIMER_TICK:
                            if (ui != null) {
                                ui.setTimerValue(intValue(value));
                            }
                            break;
                        case BID_NEW_VALUE:
                            if (ui != null) {
                                currentMinBid.set(intValue(value));
                                ui.setMinValue(currentMinBid.intValue());
                                String message = "Novo lance";
                                if (name != null && !name.isEmpty()) {
                                    message += " feito pelo usuário " + name;
                                }
                                message += ": R$ " + value;
                                ui.setMessage(message);
                            }
                            break;

                        case BID_CURRENT_VALUE:
                            if (ui != null) {
                                currentMinBid.set(intValue(value));
                                ui.setMinValue(currentMinBid.intValue());
                            }
                            break;

                        case BID_SUCCESSFUL:
                            if (ui != null) {
                                ui.setMessage("Seu lance de R$ " + value + " foi aceito!");
                            }
                            break;

                        case BID_ERROR:
                            if (ui != null) {
                                ui.setMessage("Valor de lance tem que ser maior que valor mínimo: R$ " + value);
                            }
                            break;

                        case NEW_USER:
                            if (ui != null && name != null && !name.isEmpty()) {
                                ui.setMessage("Novo participante do leilão: " + name);
                            }
                            break;

                        case ALL_USERS:
                            if (ui != null) {
                                ui.setMessage("Participantes do leilão: " + value);
                            }
                            break;

                        case AUCTION_WINNER:
                            if (ui != null) {
                                ui.setMessage("Parabéns, você foi o vencedor do leilão");
                            }
                            break;

                        case END_OF_AUCTION:
                            if (ui != null) {
                                ui.setMessage("O usuário " + name + " arrematou o objeto por R$" + value);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
            SwingUtilities.invokeLater(() -> ui.setConnectionStatus(false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int intValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onSendBidClicked(int value) {
        Properties message = new Properties();
        message.setProperty(ACTION, AUCTION_BID.getValue());
        message.setProperty(VALUE, String.valueOf(value));
        sendMessage(message);
    }

    @Override
    public void onButtonConnectClicked(String name) {
        this.name = name;
        new Thread(this::start).start();
    }

    private void sendMessage(Properties message) {
        StringWriter writer = new StringWriter();
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            message.store(new PrintWriter(writer), "");
            String serializedResponse = writer.getBuffer().toString();
            serializedResponse = serializedResponse.replaceAll("\\n", ";");
            serializedResponse = serializedResponse + "\n";
            out.println(serializedResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
