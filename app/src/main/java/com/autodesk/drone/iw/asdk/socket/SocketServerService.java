package com.autodesk.drone.iw.asdk.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.autodesk.drone.iw.asdk.socket.cmd.CommandServer;
import com.autodesk.drone.iw.asdk.socket.download.DownloadServer;
import com.autodesk.drone.iw.asdk.socket.preview.PreviewServer;
import com.autodesk.drone.iw.asdk.socket.status.StatusServer;

public class SocketServerService extends Service {

    private IBinder mBinder = null;
    private CommandServer commandServer = null;
    private PreviewServer previewServer = null;
    private DownloadServer downloadServer = null;
    private StatusServer statusServer = null;

    @Override
    public IBinder onBind(Intent intent) {
        SocketLogger.log(SocketLogger.LogPriority.info, SocketServerService.class, "SocketServerService. Service onBind!");
        startServer();
        return mBinder;
    }

    private void startServer() {
        if (commandServer == null) {
            commandServer = new CommandServer();
        }

        if (!commandServer.isRunning()) {
            commandServer.start();
        }

        if (previewServer == null) {
            previewServer = new PreviewServer();
        }

        if (!previewServer.isRunning()) {
            previewServer.start();
        }
        if (downloadServer == null) {
            downloadServer = new DownloadServer();
        }

        if (!downloadServer.isRunning()) {
            downloadServer.start();
        }

        if (statusServer == null) {
            statusServer = new StatusServer();
        }
        if (!statusServer.isRunning()) {
            statusServer.start();
        }
    }

    @Override
    public void onCreate() {
        SocketLogger.log(SocketLogger.LogPriority.info, SocketServerService.class, "SocketServerService. onCreate!");
        super.onCreate();
        mBinder = new LocalBinder();
    }

    @Override
    public void onDestroy() {
        SocketLogger.log(SocketLogger.LogPriority.info, SocketServerService.class, "SocketServerService. onDestroy!");
        super.onDestroy();
        if (commandServer != null) {
            commandServer.stop();
            commandServer = null;
        }

        if (previewServer != null) {
            previewServer.stop();
            previewServer = null;
        }
        if (downloadServer != null) {
            downloadServer.stop();
            downloadServer = null;
        }
        if (statusServer != null) {
            statusServer.stop();
            statusServer = null;
        }
    }

    public String getStatus() {
        return "Command  Socket on : " + commandServer.getPort() + ".\n"
                + "Preview  Socket on : " + previewServer.getPort() + ".\n"
                + "Download Socket on : " + downloadServer.getPort() + ".\n"
                + "Status  Socket on : " + statusServer.getPort();
    }

    public class LocalBinder extends Binder {
        public SocketServerService getService() {
            return SocketServerService.this;
        }

    }
}
