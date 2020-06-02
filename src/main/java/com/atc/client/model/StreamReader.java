package com.atc.client.model;

import com.atc.client.controller.GameCreatorController;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static com.atc.server.Message.msgTypes.*;

public class StreamReader extends StreamController {

    protected ObjectInputStream in;
    public ObjectOutputStream out;
    protected Socket socket;
    private GameActivity gameActivity;
    private VBox chatHistory;

    public Semaphore streamNotifier = new Semaphore(0);
    public boolean connected = false;
    public boolean terminated = false;

    private Message.msgTypes lastMsgType;


    public StreamReader(GameSettings gs, GameActivity gameActivity, VBox chatHistory){
        this.chatHistory = chatHistory;
        this.gameActivity= gameActivity;
    }


    @Override
    public void run()  {
            try {
                System.out.println(GameSettings.getInstance().getClientUUID().toString());
                socket = new Socket(GameSettings.getInstance().getIpAddress(), 2137);
                System.out.println("Connected!");
                System.out.println(socket.toString());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                System.out.println("Unable to connect. Are you sure about IP address?");
            }

        if(in==null || out==null || socket.isClosed()) {
            streamNotifier.release();
            return;
        }
        streamNotifier.release();
        connected=true;

        try {
            out.writeObject(new Message());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(!GameCreatorController.creatorMessage.msgSet)
                out.writeObject(new Message(GameSettings.getInstance()));
            else
                out.writeObject(GameCreatorController.creatorMessage.msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(()-> {
            try {
                out.writeObject(new Message(FETCH_AIRPLANES));
                out.writeObject(new Message(FETCH_CHECKPOINTS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        while(connected && !terminated){
            Message msg;
            try {
                msg = (Message) in.readObject();
                lastMsgType = msg.getMsgType();
                switch (lastMsgType){
                    case AIRPLANES_LIST:
                        gameActivity.updateAirplanes(msg.getAirplanes());
//                        msg.getAirplanes().forEach((k, airplane) -> gameActivity.updateAirplane(airplane));
                        Platform.runLater(
                                () -> gameActivity.wrapPrinting());
                        System.out.println("Got airplanes");
                        break;
                    case CHECKPOINTS_LIST:
                        ConcurrentHashMap<UUID, Checkpoint> tempMap = msg.getCheckpoints();
                        msg.getCheckpointsAirplanesMapping().forEach(pair ->
                                tempMap.get(pair.getKey()).passAirplane(pair.getValue()));
                        gameActivity.setCheckpoints(tempMap);
                        Platform.runLater(
                                () -> gameActivity.wrapPrinting());
                        System.out.println("Got checkpoints");
                        break;
                    case CHAT_MESSAGE:
                        Label msgLabel = new Label(msg.getChatMsg());
                        msgLabel.setFont(new Font("Comic Sans MS", 14));
                        msgLabel.setTextFill(Color.GREEN);
                        Platform.runLater(
                                () -> chatHistory.getChildren().add(msgLabel)
                        );
                        break;
                    case SERVER_GOODBYE:
                        connected=false;
                        break;
                }
                System.out.println("received data");
            }
            catch (SocketException sex){
                try {
                    socket.close();
                    System.out.println("Socket close SR.run(1)");
                } catch (IOException e) {
                    System.out.println("Can't close socket");
                }
                try {
                    socket = new Socket(GameSettings.getInstance().getIpAddress(), 2137);
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
        }
        try {
            socket.close();
            System.out.println("Socket close SR.run(2)"+terminated+connected);
        } catch (IOException e) {
            System.out.println("Can't close socket");
        }
    }

    @Override
    public void terminate() {
        System.out.println("StreamReader terminated");
        terminated = true;
    }
}
