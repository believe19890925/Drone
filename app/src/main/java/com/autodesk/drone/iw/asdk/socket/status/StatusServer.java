package com.autodesk.drone.iw.asdk.socket.status;

import com.autodesk.drone.iw.asdk.app.DroneApplication;
import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dongjiawei on 3/10/15.
 */
public class StatusServer implements Runnable{
    private boolean isRunning;
    public static final int PORT = 9996;
    private ServerSocket server = null;

    public StatusServer() {
    }

    public int getPort() {
        return PORT;
    }

    public void stop() {
        if (server != null) {
            try {
                server.close();
                server = null;
                SocketLogger.log(SocketLogger.LogPriority.info, StatusServer.class, "## Status server stop! port: " + PORT);
            } catch (IOException e) {
                e.printStackTrace();
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
                String toastMsg = "## Open StatusSocket: " + client.getRemoteSocketAddress().toString() + "!";
                SocketLogger.log(SocketLogger.LogPriority.info, StatusServer.class, toastMsg);
                ConsoleManager.console(toastMsg);

                // Display a toast message to notice a command listener
                DroneApplication.getApplication().toastLongMessage(toastMsg);

                StatusCmdDispatcher.getInstance().registerSocket(client);
            } while (!server.isClosed());
        } catch (Exception e) {
            SocketLogger.log(SocketLogger.LogPriority.info, StatusServer.class, " Caution : Socket closed for StatusServer.");
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

}
