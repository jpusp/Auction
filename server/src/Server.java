import constants.Constants;
import handler.ClientHandler;
import listener.ClientListener;
import model.ActionType;
import timer.AuctionTimer;
import timer.TimerListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import static constants.Constants.*;
import static model.ActionType.*;

public class Server implements ClientListener, TimerListener {
    private int minValue = 0;
    Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());
    boolean isAuctionOngoing = true;
    private AuctionTimer timer;
    private boolean isTimerInitiated;
    private ClientHandler lastBidder;

    public Server(int minValue, int timeoutInSeconds) {
        this.minValue = minValue;
        timer = new AuctionTimer(timeoutInSeconds, this);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            if (!isTimerInitiated) {
                timer.startTimer();
                isTimerInitiated = true;
            }

            while (isAuctionOngoing) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }

            clientHandlers.forEach(ClientHandler::disconnect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserLogin(ClientHandler handler) {
        Properties response = createResponse(NEW_USER, 0);
        response.setProperty(NAME, handler.getName());
        broadcastMessage(response);

        Properties handlerResponse = new Properties();
        handlerResponse.setProperty(ACTION, ALL_USERS.getValue());
        handlerResponse.setProperty(
                VALUE,
                clientHandlers.stream()
                        .map(ClientHandler::getName)
                        .collect(Collectors.joining(", "))
        );
        broadcastMessage(handlerResponse);
    }

    @Override
    public void onNewBidReceived(int newValue, ClientHandler handler) {
        if (newValue > minValue) {
            minValue = newValue;
            lastBidder = handler;
            sendSuccessfulBid(handler, newValue);
            Properties response = createResponse(BID_NEW_VALUE, newValue);
            response.setProperty(NAME, handler.getName());
            broadcastMessage(response);
        } else {
            sendBidError(handler);
        }
    }

    @Override
    public void onBidCurrentValueRequest(ClientHandler handler) {
        Properties response = createResponse(BID_CURRENT_VALUE, minValue);
        handler.sendMessage(response);
    }

    private void sendSuccessfulBid(ClientHandler handler, int newValue) {
        if (!isTimerInitiated) {
            timer.startTimer();
            isTimerInitiated = true;
        }
        Properties response = createResponse(BID_SUCCESSFUL, newValue);
        handler.sendMessage(response);
    }

    private void sendBidError(ClientHandler handler) {
        Properties response = createResponse(BID_ERROR, minValue);
        handler.sendMessage(response);
    }

    @Override
    public void onTimerTick(int remainingTime) {
        Properties response = createResponse(TIMER_TICK, remainingTime);
        broadcastMessage(response);
    }

    @Override
    public void onTimeOver() {
        Properties response = createResponse(END_OF_AUCTION, minValue);
        response.setProperty(NAME, lastBidder.getName());
        broadcastMessage(response);

        Properties winnerMessage = createResponse(AUCTION_WINNER, 0);
        lastBidder.sendMessage(winnerMessage);
        isAuctionOngoing = false;
    }

    private void broadcastMessage(Properties message) {
        cleanClosedSockets();
        clientHandlers.forEach(handler -> {
            handler.sendMessage(message);
        });
    }

    private void cleanClosedSockets() {
        List<ClientHandler> handlersToBeRemoved = new ArrayList<>();
        clientHandlers.stream()
                .filter(ClientHandler::isClosed)
                .forEach(handlersToBeRemoved::add);

        handlersToBeRemoved.forEach(clientHandler -> clientHandlers.remove(clientHandler));
    }

    public static void main(String[] args) {
        int minValue = 200;
        int timeoutInSeconds = 150;
        if (args.length > 0) {
            minValue = Integer.parseInt(args[0]);
            if (args.length > 1) {
                timeoutInSeconds = Integer.parseInt(args[1]);
            }
        }
        new Server(minValue, timeoutInSeconds).start();
    }

    private Properties createResponse(ActionType action, int value) {
        Properties response = new Properties();
        response.setProperty(ACTION, action.getValue());
        response.setProperty(VALUE, String.valueOf(value));
        return response;
    }
}
