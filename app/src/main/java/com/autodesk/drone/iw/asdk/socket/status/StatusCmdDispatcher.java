package com.autodesk.drone.iw.asdk.socket.status;

import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.SocketUtils;
import com.autodesk.drone.iw.asdk.socket.cmd.CommandLoader;
import com.autodesk.drone.iw.asdk.socket.cmd.ICommandCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dongjiawei on 3/10/15.
 */
public class StatusCmdDispatcher {

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private static StatusCmdDispatcher instance = new StatusCmdDispatcher();

    private Vector<Socket> socketList = new Vector<Socket>();

    public enum CMD_TYPE {
        CMD_SHOW_DRONE_STATUS,
    }

    private StatusCmdDispatcher() {
    }

    public static StatusCmdDispatcher getInstance() {
        return instance;
    }

    public void registerSocket(Socket socket) {
        if (socket == null) {
            return;
        }
        SocketLogger.log(SocketLogger.LogPriority.info, StatusCmdDispatcher.class, "## StatusLoader new socket added!!");
        StatusCommandTask task = new StatusCommandTask(socket);
        mExecutorService.execute(task);

        socketList.add(socket);
    }

    class StatusCommandTask implements Runnable {
        private Socket socket;
        private PrintWriter writer = null;

        public StatusCommandTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (socket != null) {
                BufferedReader reader = null;
                try {
                    writer = new PrintWriter(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (socket != null) {
                        String message = reader.readLine();
                        if (message != null && message.trim().length() > 0 && !"null".equals(message.trim())) {

                            SocketLogger.log(SocketLogger.LogPriority.info, StatusCmdDispatcher.class, "## Command string = " + message);
                            ConsoleManager.console("##Receive > " + message);

                            JSONObject cmdObj = null;
                            CMD_TYPE cmdType = null;
                            try {
                                cmdObj = new JSONObject(message.trim());
                                cmdType = CMD_TYPE.valueOf(cmdObj.getString("cmd"));
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                                SocketLogger.log(SocketLogger.LogPriority.error, StatusCmdDispatcher.class, ex.getMessage(), ex);
                            }

                            if (cmdObj == null || cmdType == null) {
                                printResult(SocketUtils.getMessage("Invalid command : " + message));
                                continue;
                            }

                            switch (cmdType) {
                                case CMD_SHOW_DRONE_STATUS:
                                    int interval = SocketUtils.getJsonInt(cmdObj, "interval", 300);
                                    CommandLoader.getInstance().executeDroneStatus(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), interval);
                                    break;
                                default:
                                    printResult(SocketUtils.getMessage("Invalid command : " + message));
                            }
                        }
                    }
                } catch (Exception ex) {
                    SocketLogger.log(SocketLogger.LogPriority.error, StatusCmdDispatcher.class, ex.getMessage(), ex);
                    if (writer != null) {
                        writer.close();
                        writer = null;
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
                            SocketLogger.log(SocketLogger.LogPriority.info, StatusCmdDispatcher.class, "## Close CommandSocket: %s !", socket.getRemoteSocketAddress().toString());
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

        private void printResult(String result) {
            try {
                SocketLogger.log(SocketLogger.LogPriority.info, StatusCmdDispatcher.class, ">> CMD printResult > " + result);
                if (result.indexOf("NG") != -1) {
                    ConsoleManager.console(result);
                }
                writer.println(result);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (socket != null) {
                    try {
                        socketList.remove(socket);
                        socket.close();
                        socket = null;
                    } catch (Exception e1) {
                        SocketLogger.log(SocketLogger.LogPriority.error, StatusCmdDispatcher.class, "## Close CommandSocket: %s !", e1, socket.getRemoteSocketAddress().toString());
                    }
                }
            }
        }
    }

}
