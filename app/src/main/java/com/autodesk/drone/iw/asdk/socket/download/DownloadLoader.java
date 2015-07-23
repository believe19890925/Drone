package com.autodesk.drone.iw.asdk.socket.download;

import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.sync.SyncFileService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadLoader {

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private static DownloadLoader instance = new DownloadLoader();

    private Vector<Socket> socketList = new Vector<Socket>();

    private DownloadLoader() {
    }

    public static DownloadLoader getInstance() {
        return instance;
    }

    public void registSocket(Socket socket) {
        if (socket == null) {
            return;
        }

        SocketLogger.log(SocketLogger.LogPriority.info, DownloadLoader.class, "## DownloadLoader new socket added!!");

        FileWriteTask task = new FileWriteTask(socket);
        mExecutorService.execute(task);
        socketList.add(socket);
    }

    class FileWriteTask implements Runnable {
        private Socket socket;
        DataOutputStream outputStream = null;
        private boolean isCancel = false;

        private Queue<byte[]> dataQueue = new ConcurrentLinkedQueue<byte[]>();

        public FileWriteTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (socket != null) {
                BufferedReader reader = null;

                // keep long connection.
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    String fileName = reader.readLine();
                    SocketLogger.log(SocketLogger.LogPriority.info, DownloadLoader.class, "## Command string = %s", fileName);
                    ConsoleManager.console("##Receive > Download file: " + fileName);

                    if (fileName == null || fileName.trim().length() <= 0) {
                        outputStream.writeUTF("Invalid command : empty file name! > " + fileName);
                        outputStream.flush();
                        return;
                    }

                    SyncFileService.getInstance().executeSync(fileName.trim(), new IDownloadCalback() {
                        @Override
                        public void notifyData(byte[] data, int size) {
                            onDataUpdate(data, size);
                        }

                        @Override
                        public void finished() {
                            isCancel = true;
                        }

                        @Override
                        public void error(String message) {
                            try {
                                outputStream.writeUTF(message);
                                outputStream.flush();
                            } catch (IOException e) {
                                SocketLogger.log(SocketLogger.LogPriority.error, DownloadLoader.class, "FileWriteTask executeSync Exception ", e);
                            }
                            isCancel = true;
                        }

                        @Override
                        public void notifyFileInfo(String fileName, long fileSize, String createTime) {
                            try {
                                String notifyMessage = "OK," + fileName + "," + fileSize + "," + createTime;
                                outputStream.writeUTF(notifyMessage);
                                outputStream.flush();
                                ConsoleManager.console(notifyMessage);
                            } catch (IOException e) {
                                SocketLogger.log(SocketLogger.LogPriority.error, DownloadLoader.class, "FileWriteTask notifyFileInfo Exception ", e);
                            }
                        }
                    });

                    while (!isCancel || !dataQueue.isEmpty()) {
                        if (!dataQueue.isEmpty() && dataQueue.size() > 0) {
                            byte[] bufferData = dataQueue.poll();
                            outputStream.write(bufferData, 0, bufferData.length);
                        }
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    SocketLogger.log(SocketLogger.LogPriority.error, DownloadLoader.class, "FileWriteTask Exception ", e);
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            outputStream = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                            reader = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            SocketLogger.log(SocketLogger.LogPriority.info, DownloadLoader.class, "## Close DownloadSocket: %s !", socket.getRemoteSocketAddress().toString());
                            socketList.remove(socket);
                            socket.close();
                            socket = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }

        public void onDataUpdate(byte[] data, int size) {
            if (data != null) {
                dataQueue.add(getByteArray(data, size));
            }
        }

        private byte[] getByteArray(byte[] data, int size) {
            byte[] target = new byte[size];
            System.arraycopy(data, 0, target, 0, size);
            return target;
        }
    }
}
