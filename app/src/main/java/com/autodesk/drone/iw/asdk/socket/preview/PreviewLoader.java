package com.autodesk.drone.iw.asdk.socket.preview;

import android.os.Environment;

import com.autodesk.drone.iw.asdk.socket.SocketLogger;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dji.sdk.api.DJIDrone;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.natives.CamShow;

public class PreviewLoader {

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private static PreviewLoader instance = new PreviewLoader();

    private Vector<Socket> socketList = new Vector<Socket>();

    private PreviewLoader() {
    }

    public static PreviewLoader getInstance() {
        return instance;
    }

    public void registSocket(Socket socket) {
        if (socket == null) {
            return;
        }
        SocketLogger.log(SocketLogger.LogPriority.info, PreviewLoader.class, "## PreviewLoader new socket added!!");
        PreviewWriteTask task = new PreviewWriteTask(socket);
        mExecutorService.execute(task);

        // Reset preview size for test
        new Thread(new ResetPreviewSize()).start();

        socketList.add(socket);
    }

    class ResetPreviewSize implements Runnable {

        @Override
        public void run() {
            CamShow.native_pauseStream(true);
            // 0: Resolution_Type_320x240_15fps -> 1
            // 1: Resolution_Type_320x240_30fps -> 2
            // 2: Resolution_Type_640x480_15fps -> 4
            // 3: Resolution_Type_640x480_30fps -> 8
            CamShow.native_setType(4);
            CamShow.native_pauseStream(false);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class PreviewWriteTask implements Runnable {
        private Socket socket;
        private Queue<byte[]> dataQueue = new ConcurrentLinkedQueue<byte[]>();
        private File byteDatFile = null;

        public PreviewWriteTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardFolder = Environment.getExternalStorageDirectory();
                byteDatFile = new File(sdCardFolder, "drone_data/drone_native_" + System.currentTimeMillis() + ".dat");
            }

            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(new DJIReceivedVideoDataCallBack() {
                @Override
                public void onResult(byte[] videoBuffer, int size) {
                    onDataUpdate(videoBuffer, size);
                }
            });

            DataOutputStream outputStream = null;

            // keep long connection.
            while (true) {
                try {
                    if (socket == null) {
                        SocketLogger.log(SocketLogger.LogPriority.info, PreviewLoader.class, "## Socket terminated !!...");
                        break;
                    }

                    if (outputStream == null) {
                        outputStream = new DataOutputStream(socket.getOutputStream());
                    }

                    // write data if exists
                    if (!dataQueue.isEmpty()) {
                        // SocketLogger.log(SocketLogger.LogPriority.info, PreviewLoader.class, "## sendData...");

                        byte[] bufferData = dataQueue.poll();
                        byte[] header = ("Drone:" + bufferData.length + ":Drone").getBytes();
                        byte[] merged = new byte[header.length + bufferData.length];

                        System.arraycopy(header, 0, merged, 0, header.length);
                        System.arraycopy(bufferData, 0, merged, header.length, bufferData.length);

                        //outputStream.write(header, 0, header.length);                        
                        //outputStream.write(bufferData, 0, bufferData.length);
                        outputStream.write(merged, 0, merged.length);

                        outputStream.flush();

                        //Tools.appendDataToFile(byteDatFile, merged);
                    }
                } catch (Exception e) {
                    SocketLogger.log(SocketLogger.LogPriority.error, PreviewLoader.class, "PreviewWriteTask exception", e);
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            outputStream = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            SocketLogger.log(SocketLogger.LogPriority.info, PreviewLoader.class, "## Close PreviewSocket: %s !", socket.getRemoteSocketAddress().toString());
                            socketList.remove(socket);
                            socket.close();
                            socket = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }

        private void onDataUpdate(byte[] data, int size) {
            byte[] target = new byte[size];
            System.arraycopy(data, 0, target, 0, size);
            dataQueue.add(target);
        }
    }

}
