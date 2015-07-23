package com.autodesk.drone.iw.asdk.socket.cmd;

import com.autodesk.drone.iw.asdk.app.DroneApplication;
import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dji.sdk.api.GroundStation.DJIGroundStationTask;

public class CommandServer implements Runnable {
    private boolean isRunning;
    public static final int PORT = 9999;
    private ServerSocket server = null;

    // Ground station task for caching way points
    private static DJIGroundStationTask mTask;

    public CommandServer() {
        initGroundStation();
    }

    public int getPort() {
        return PORT;
    }

    public void stop() {
        if (server != null) {
            try {
                server.close();
                server = null;
                SocketLogger.log(SocketLogger.LogPriority.info, CommandServer.class, "## Socket server stop! port: " + PORT);
            } catch (IOException e) {
                SocketLogger.log(SocketLogger.LogPriority.info, CommandServer.class, "CommandServer Stop Exception ", e);
            }
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void start() {
        if (isRunning()) {
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        isRunning = true;
        Socket client = null;
        try {
            server = new ServerSocket(PORT);
            do {
                client = server.accept();
                String toastMsg = "## Open CommandSocket: " + client.getRemoteSocketAddress().toString() + "!";
                SocketLogger.log(SocketLogger.LogPriority.info, CommandServer.class, toastMsg);
                ConsoleManager.console(toastMsg);

                // Display a toast message to notice a command listener
                DroneApplication.getApplication().toastLongMessage(toastMsg);

                CommandDispatcher.getInstance().registSocket(client);
            } while (server != null && !server.isClosed());
        } catch (Exception e) {
            SocketLogger.log(SocketLogger.LogPriority.info, CommandServer.class, "Caution : Socket closed for CommandServer.");
        } finally {
            try {
                if (client != null) {
                    client.close();
                    client = null;
                }
                if (server != null) {
                    server.close();
                    server = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        isRunning = false;
    }

    public void initGroundStation() {
        // new ground station task
        SocketLogger.log(SocketLogger.LogPriority.info, CommandLoader.class, "## initDJIStatus. Init DJIGroundStationTask !!");
        mTask = new DJIGroundStationTask();
    }

    public static DJIGroundStationTask getGSTask() {
        return mTask;
    }
}
