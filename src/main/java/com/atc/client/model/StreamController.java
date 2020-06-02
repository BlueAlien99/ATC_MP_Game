package com.atc.client.model;

import java.net.Socket;
import java.util.logging.SocketHandler;

public abstract class StreamController implements Runnable {
    private static StreamController singleton;
    protected int scType=0;
    public static final int SC_TYPE_STREAMREADER = 1;
    public static final int SC_TYPE_HISTORYSTREAM = 2;

    final Object terminateMonitor = new Object();

    public static boolean checkInstance(int targetScType){
        if(singleton==null)
            return false;
        return singleton.scType == targetScType;
    }

    public static StreamController getInstance(){
        return singleton;
    }

    public static StreamController setInstance(StreamController sc){
        if(singleton!=null)
            singleton.terminate();
        singleton=sc;
        return singleton;
    }


    abstract void terminate();

}
