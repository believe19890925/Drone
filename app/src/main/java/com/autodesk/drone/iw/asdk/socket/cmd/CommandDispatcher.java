package com.autodesk.drone.iw.asdk.socket.cmd;

import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.SocketUtils;
import com.autodesk.drone.iw.asdk.socket.sync.ISyncFileCallback;
import com.autodesk.drone.iw.asdk.socket.sync.SyncFileService;

import org.json.JSONArray;
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

import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;

public class CommandDispatcher {

    private ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private static CommandDispatcher instance = new CommandDispatcher();
    private Vector<Socket> socketList = new Vector<Socket>();

    private CommandDispatcher() {
    }

    public static CommandDispatcher getInstance() {
        return instance;
    }

    public enum CMD_TYPE {
        CMD_START_TAKE_PHOTO,
        CMD_STOP_TAKE_PHOTO,
        CMD_START_RECORD_VIDEO,
        CMD_STOP_RECORD_VIDEO,
        CMD_LIST_FILE,
        CMD_SHOW_DRONE_STATUS,
        CMD_PITCH_UP,
        CMD_PITCH_DOWN,
        CMD_PITCH_VALUE,
        CMD_YAW_VALUE,
        CMD_FORMAT_SD_CARD,

        CMD_GS_OPEN_GROUND_STATION,
        CMD_GS_CLOSE_GROUND_STATION,

        CMD_GS_CLEAR_WAY_POINT,
        CMD_GS_ADD_WAY_POINT,
        CMD_GS_UPLOAD_WAY_POINTS,
        CMD_GS_TAKE_OFF,
        CMD_GS_GO_HOME,
        CMD_GS_PAUSE,
        CMD_GS_RESUME,

        CMD_GS_YAW_LEFT,
        CMD_GS_YAW_RIGHT,
        CMD_GS_YAW_STOP,
        CMD_GS_YAW_ROTATE_PHOTO,
        CMD_GS_PITCH_PLUS,
        CMD_GS_PITCH_MINUS,
        CMD_GS_PITCH_STOP,
        CMD_GS_ROLL_PLUS,
        CMD_GS_ROLL_STOP,
        CMD_GS_ROLL_MINUS,
        CMD_GS_THROTTLE_PLUS,
        CMD_GS_THROTTLE_MINUS,
        CMD_GS_THROTTLE_STOP,
    }

    public enum ACTION_TYPE {
        stay,
        simple_shot,
        video_start,
        video_stop,
        craft_yaw,
        gimbal_yaw,
        gimbal_pitch
    }

    public void registSocket(Socket socket) {
        if (socket == null) {
            return;
        }
        SocketLogger.log(SocketLogger.LogPriority.info, CommandDispatcher.class, "## CommandDispatcher new socket added!!");

        CommandTask task = new CommandTask(socket);
        mExecutorService.execute(task);

        socketList.add(socket);
    }

    class CommandTask implements Runnable {
        private Socket socket;
        private PrintWriter writer = null;

        public CommandTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (socket != null) {
                BufferedReader reader = null;
                try {
                    writer = new PrintWriter(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (socket != null && reader != null) {
                        String message = reader.readLine();
                        if (message != null && message.trim().length() > 0 && !"null".equals(message.trim())) {

                            SocketLogger.log(SocketLogger.LogPriority.info, CommandDispatcher.class, "## Command string = " + message);
                            ConsoleManager.console("##Receive > " + message);

                            JSONObject cmdObj = null;
                            CMD_TYPE cmdType = null;
                            try {
                                cmdObj = new JSONObject(message.trim());
                                cmdType = CMD_TYPE.valueOf(cmdObj.getString("cmd"));
                            } catch (Exception ex) {
                                SocketLogger.log(SocketLogger.LogPriority.error, CommandDispatcher.class, ex.getMessage(), ex);
                            }

                            if (cmdObj == null || cmdType == null) {
                                printResult(SocketUtils.getMessage("Invalid command : " + message));
                                continue;
                            }

                            switch (cmdType) {
                                case CMD_LIST_FILE:
                                    SyncFileService.getInstance().listFile(new ISyncFileCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_START_RECORD_VIDEO:
                                    CommandLoader.getInstance().executeStartVideo(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_STOP_RECORD_VIDEO:
                                    CommandLoader.getInstance().executeStopVideo(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_START_TAKE_PHOTO:
                                    CommandLoader.getInstance().executeStartPhoto(new ICommandCallback() {

                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }

                                    }, cmdType.name());
                                    break;
                                case CMD_STOP_TAKE_PHOTO:
                                    CommandLoader.getInstance().executeStopPhoto(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_SHOW_DRONE_STATUS:
                                    int interval = SocketUtils.getJsonInt(cmdObj, "interval", 300);
                                    CommandLoader.getInstance().executeDroneStatus(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result, true);
                                        }
                                    }, cmdType.name(), interval);
                                    break;
                                case CMD_PITCH_UP:
                                    CommandLoader.getInstance().executePitchUp(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_PITCH_DOWN:
                                    CommandLoader.getInstance().executePitchDown(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_PITCH_VALUE:
                                    int pitchValue = SocketUtils.getJsonInt(cmdObj, "pitchValue", -1);
                                    CommandLoader.getInstance().executePitch(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), pitchValue);
                                    break;
                                case CMD_YAW_VALUE:
                                    int leftAngle = SocketUtils.getJsonInt(cmdObj, "angle", 60);
                                    CommandLoader.getInstance().executeYAWValue(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), leftAngle);
                                    break;
                                case CMD_FORMAT_SD_CARD:
                                    CommandLoader.getInstance().executeFormatSDCard(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_OPEN_GROUND_STATION:
                                    CommandLoader.getInstance().executeOpenGS(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_CLEAR_WAY_POINT:
                                    CommandLoader.getInstance().executeClearWayPoint(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_ADD_WAY_POINT:
                                    double lat = SocketUtils.getJsonDouble(cmdObj, "lat", -1);
                                    double lng = SocketUtils.getJsonDouble(cmdObj, "lng", -1);

                                    if (lat == -1 || lng == -1) {
                                        printResult(SocketUtils.getMessage("Invalid point way point [" + lat + ", " + lng + "] !", cmdType.name()));
                                    } else {
                                        boolean hasAction = SocketUtils.getJsonInt(cmdObj, "hasAction", 0) == 1;
                                        int actionSize = 0;
                                        DJIGroundStationWaypoint wayPoint = null;

                                        if (!hasAction) {
                                            wayPoint = new DJIGroundStationWaypoint(lat, lng, 0, 0);
                                        } else {
                                            actionSize = cmdObj.has("actionList") ? cmdObj.getJSONArray("actionList").length() : 0;
                                            int repeatNum = SocketUtils.getJsonInt(cmdObj, "actionRepeatNumber", 0);
                                            wayPoint = new DJIGroundStationWaypoint(lat, lng, actionSize, repeatNum);
                                        }

                                        wayPoint.altitude = (float) SocketUtils.getJsonDouble(cmdObj, "altitude", 30);
                                        wayPoint.speed = (float) SocketUtils.getJsonDouble(cmdObj, "speed", 2);
                                        wayPoint.heading = (short) SocketUtils.getJsonDouble(cmdObj, "heading", 360);
                                        wayPoint.maxReachTime = (short) SocketUtils.getJsonInt(cmdObj, "maxReachTime", 999);// maximum time which the drone could spend on the way point.
                                        wayPoint.stayTime = (short) SocketUtils.getJsonInt(cmdObj, "stayTime", 3);
                                        wayPoint.turnMode = SocketUtils.getJsonInt(cmdObj, "turnMode", 0);
                                        wayPoint.hasAction = hasAction;

                                        if (hasAction && actionSize > 0) {
                                            JSONArray actionList = cmdObj.getJSONArray("actionList");
                                            for (int i = 0; i < actionList.length(); i++) {
                                                JSONObject jsonObject = actionList.getJSONObject(i);
                                                String actionTpe = jsonObject.getString("action_type");
                                                int actionValue = SocketUtils.getJsonInt(jsonObject, "action_value", 0);

                                                if (ACTION_TYPE.stay.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Stay, actionValue);
                                                } else if (ACTION_TYPE.craft_yaw.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, actionValue);
                                                } else if (ACTION_TYPE.gimbal_pitch.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, actionValue);
                                                } else if (ACTION_TYPE.gimbal_yaw.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Yaw, actionValue);
                                                } else if (ACTION_TYPE.video_start.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Video_Start, actionValue);
                                                } else if (ACTION_TYPE.video_stop.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Video_Stop, actionValue);
                                                } else if (ACTION_TYPE.simple_shot.name().equalsIgnoreCase(actionTpe)) {
                                                    wayPoint.addAction(DJIGroundStationTypeDef.GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, actionValue);
                                                }
                                            }
                                        }

                                        int index = SocketUtils.getJsonInt(cmdObj, "index", -1);
                                        CommandLoader.getInstance().executeAddWayPoint(new ICommandCallback() {
                                            @Override
                                            public void notifyResult(String result) {
                                                printResult(result);
                                            }
                                        }, cmdType.name(), wayPoint, index);
                                    }
                                    break;
                                case CMD_GS_UPLOAD_WAY_POINTS:
                                    CommandLoader.getInstance().executeUploadWayPoints(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_TAKE_OFF:
                                    CommandLoader.getInstance().executeTakeOff(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_CLOSE_GROUND_STATION:
                                    CommandLoader.getInstance().executeCloseGS(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_PAUSE:
                                    CommandLoader.getInstance().executeDronePause(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_RESUME:
                                    CommandLoader.getInstance().executeDroneResume(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_GO_HOME:
                                    CommandLoader.getInstance().executeGoHome(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_YAW_LEFT:
                                    CommandLoader.getInstance().executeGsYAW(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), -100);
                                    break;
                                case CMD_GS_YAW_RIGHT:
                                    CommandLoader.getInstance().executeGsYAW(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 100);
                                    break;
                                case CMD_GS_YAW_STOP:
                                    CommandLoader.getInstance().executeGsYAW(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 0);
                                    break;
                                case CMD_GS_YAW_ROTATE_PHOTO:
                                    CommandLoader.getInstance().executeGsYAWRotateTakePhoto(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name());
                                    break;
                                case CMD_GS_PITCH_PLUS:
                                    CommandLoader.getInstance().executeGsPitch(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 200);
                                    break;
                                case CMD_GS_PITCH_MINUS:
                                    CommandLoader.getInstance().executeGsPitch(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), -200);
                                    break;
                                case CMD_GS_PITCH_STOP:
                                    CommandLoader.getInstance().executeGsPitch(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 0);
                                    break;
                                case CMD_GS_ROLL_PLUS:
                                    CommandLoader.getInstance().executeGsRoll(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 200);
                                    break;
                                case CMD_GS_ROLL_STOP:
                                    CommandLoader.getInstance().executeGsRoll(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), -200);
                                    break;
                                case CMD_GS_ROLL_MINUS:
                                    CommandLoader.getInstance().executeGsRoll(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 0);
                                    break;
                                case CMD_GS_THROTTLE_PLUS:
                                    CommandLoader.getInstance().executeGsThrottle(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 1);
                                    break;
                                case CMD_GS_THROTTLE_MINUS:
                                    CommandLoader.getInstance().executeGsThrottle(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 2);
                                    break;
                                case CMD_GS_THROTTLE_STOP:
                                    CommandLoader.getInstance().executeGsThrottle(new ICommandCallback() {
                                        @Override
                                        public void notifyResult(String result) {
                                            printResult(result);
                                        }
                                    }, cmdType.name(), 0);
                                    break;
                                default:
                                    printResult(SocketUtils.getMessage("Invalid command : " + message));
                            }
                        } else if (message == null) {
                            SocketLogger.log(SocketLogger.LogPriority.warn, CommandDispatcher.class, "## Dispatcher terminated for empty message!");
                            break;
                        }
                    }
                } catch (Exception ex) {
                    SocketLogger.log(SocketLogger.LogPriority.error, CommandDispatcher.class, ex.getMessage(), ex);
                } finally {
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
                            SocketLogger.log(SocketLogger.LogPriority.info, CommandDispatcher.class, "## Close CommandSocket: %s !", socket.getRemoteSocketAddress().toString());
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
            printResult(result, false);
        }

        private void printResult(String result, boolean ignoreConsole) {
            try {
                SocketLogger.log(SocketLogger.LogPriority.info, CommandDispatcher.class, ">> CMD printResult > " + result);
                if (!ignoreConsole) {
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
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

}
