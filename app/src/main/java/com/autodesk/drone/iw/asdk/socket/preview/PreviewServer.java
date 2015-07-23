package com.autodesk.drone.iw.asdk.socket.preview;

import android.content.Context;
import android.widget.Toast;

import com.autodesk.drone.iw.asdk.app.DroneApplication;
import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PreviewServer implements Runnable {
    private boolean isRunning;
    public static final int PORT = 9998;
    private ServerSocket server = null;

    public PreviewServer() {
    }

    public int getPort() {
        return PORT;
    }

    public void stop() {
        if (server != null) {
            try {
                server.close();
                server = null;
                SocketLogger.log(SocketLogger.LogPriority.info, PreviewServer.class, "## Socket server stop! port: " + PORT);
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
                String toastMsg = "## Open PreviewSocket: " + client.getRemoteSocketAddress().toString() + "!";
                SocketLogger.log(SocketLogger.LogPriority.info, PreviewServer.class, toastMsg);
                ConsoleManager.console(toastMsg);

                // Display a toast message to notice a command listener
                DroneApplication.getApplication().toastLongMessage(toastMsg);

                PreviewLoader.getInstance().registSocket(client);
            } while (!server.isClosed());
        } catch (Exception e) {
            SocketLogger.log(SocketLogger.LogPriority.info, PreviewServer.class, "Caution : Socket closed for PreviewServer.");
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
