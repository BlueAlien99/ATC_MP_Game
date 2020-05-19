package com.atc.server.model;

import com.atc.client.model.GameActivity;
import com.atc.client.model.GameSettings;
import com.atc.server.Message;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class StreamReader extends Thread {

    protected ObjectInputStream in;
    public ObjectOutputStream out;
    protected Socket socket;
    private GameSettings gameSettings;
    private GameActivity gameActivity;
    private VBox chatHistory;

    public StreamReader(GameSettings gs, GameActivity gameActivity, VBox chatHistory){
        this.chatHistory = chatHistory;
        this.gameActivity= gameActivity;
        this.gameSettings = gs;
    }

    @Override
    public void run()  {
        try {
            System.out.println(gameSettings.getClientUUID().toString());
            socket = new Socket(gameSettings.getIpAddress(), 2137);
            System.out.println("Connected!");
            System.out.println(socket.toString());
            in = new ObjectInputStream(socket.getInputStream());
            out  = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        try {
            out.writeObject(new Message());
            out.writeObject(new Message(gameSettings));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            Message msg;
            try {
                msg = (Message) in.readObject();
                switch (msg.getMsgType()){
                    case 3:
                        msg.getAirplanes().forEach((k, airplane) -> gameActivity.updateAirplane(airplane));
                        break;
                    case 1:
                        Label msgLabel = new Label(msg.getChatMsg());
                        msgLabel.setFont(new Font("Comic Sans MS", 14));
                        msgLabel.setTextFill(Color.GREEN);
                        Platform.runLater(
                                () -> chatHistory.getChildren().add(msgLabel)
                        );
                }
                System.out.println("received data");
            }
            catch (SocketException sex){
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Can't close socket");
                }
                try {
                    socket = new Socket(gameSettings.getIpAddress(), 2137);
                    System.out.println("Connected!");
                    System.out.println(socket.toString());
                    in = new ObjectInputStream(socket.getInputStream());
                    out  = new ObjectOutputStream(socket.getOutputStream());
                }
                catch (IOException e){
                    System.out.println("Can't reconnect");
                    break;
                }
            }
            catch (IOException | ClassNotFoundException | NullPointerException ex) {
                ex.printStackTrace();
            }
            Platform.runLater(
                    () -> gameActivity.wrapPrinting());
        }
    }
}
