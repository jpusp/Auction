package ui;

import listener.UIListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AuctionUI extends JFrame {
    private JTextArea messageArea;
    private JTextField nameInputField;
    private JButton connectButton;
    private JLabel nameLabel;
    private JLabel statusLabel;
    private JLabel connectionStatusLabel;

    private JTextField inputField;
    private JLabel inputFieldLabel;
    private JButton sendButton;
    private JLabel minimumBidLabel;
    private JLabel countdownLabel;
    private JPanel connectionStatusPanel;

    public AuctionUI(UIListener uiListener) {
        setTitle("Leilão");

        nameLabel = new JLabel("Digite seu nome:");
        nameInputField = new JTextField(20);
        nameInputField.setToolTipText("Digite seu nome");

        connectButton = new JButton("Conectar");

        statusLabel = new JLabel("Status: ");
        connectionStatusLabel = new JLabel("Desconectado");
        minimumBidLabel = new JLabel("Valor mínimo: ");

        connectionStatusPanel = new JPanel();
        connectionStatusPanel.add(connectionStatusLabel);
        connectionStatusPanel.setBackground(Color.RED);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameInputField, BorderLayout.CENTER);
        namePanel.add(connectButton, BorderLayout.EAST);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        statusPanel.add(connectionStatusPanel);

        JPanel minimumBidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minimumBidPanel.add(minimumBidLabel);

        headerPanel.add(namePanel);
        headerPanel.add(statusPanel);
        headerPanel.add(minimumBidPanel);

        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);


        inputFieldLabel = new JLabel("Valor do lance:");
        inputField = new JTextField(10);
        inputField.setToolTipText("Valor do lance");

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiListener.onSendBidClicked(Integer.parseInt(inputField.getText()));
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiListener.onButtonConnectClicked(nameInputField.getText());
            }
        });

        countdownLabel = new JLabel("Contagem regressiva para lances:");

        JPanel bidInputPanel = new JPanel();
        bidInputPanel.setLayout(new BoxLayout(bidInputPanel, BoxLayout.X_AXIS));
        bidInputPanel.add(inputFieldLabel);
        bidInputPanel.add(inputField);
        bidInputPanel.add(sendButton);

        JPanel groupedPanel = new JPanel();
        groupedPanel.setLayout(new BoxLayout(groupedPanel, BoxLayout.Y_AXIS));

        groupedPanel.add(bidInputPanel);
        groupedPanel.add(countdownLabel);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(messageScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(groupedPanel, BorderLayout.NORTH);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void setConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionStatusLabel.setText("Conectado");
            connectionStatusPanel.setBackground(Color.GREEN);
        } else {
            connectionStatusLabel.setText("Desconectado");
            connectionStatusPanel.setBackground(Color.RED);
        }

        connectButton.setEnabled(!isConnected);
    }

    public void setTimerValue(int remainingTime) {
        countdownLabel.setText("Contagem regressiva para lances: " + remainingTime);
    }

    public void setMinValue(int minValue) {
        minimumBidLabel.setText("Valor atual: R$ " + minValue);
    }

    public void setMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
}
