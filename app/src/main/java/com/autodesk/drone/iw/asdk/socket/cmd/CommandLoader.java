package com.autodesk.drone.iw.asdk.socket.cmd;

import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.SocketUtils;
import com.autodesk.drone.iw.asdk.socket.dji.DJIStateManager;
import com.autodesk.drone.iw.asdk.socket.dji.IDJIGimbalAttitudeListener;
import com.autodesk.drone.iw.asdk.socket.dji.IDJIMcuUpdateStateListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraVisionType;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.Gimbal.DJIGimbalAttitude;
import dji.sdk.api.Gimbal.DJIGimbalRotation;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef.DJIMcErrorType;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationGoHomeCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationResumeCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;

public class CommandLoader {

    private Timer mCheckYawTimer;
    private int checkYawTimes;
    private DroneStatus droneStatus = new DroneStatus();

    private CommandLoader() {
    }

    private static class SingletonFactory {
        private static CommandLoader instance = new CommandLoader();

    }

    public static CommandLoader getInstance() {
        return SingletonFactory.instance;
    }


    public void executeStartVideo(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {

            DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback() {
                @Override
                public void onResult(DJIError mErr) {
                    String result = "Code =" + mErr.errorCode + ", " + "Description =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                    String resultMsg = SocketUtils.getMessage(result, cmd, mErr.errorCode == DJIError.RESULT_OK);
                    callback.notifyResult(resultMsg);
                }
            });
        }
    }

    public void executeStopVideo(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {
            DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback() {

                @Override
                public void onResult(DJIError mErr) {
                    String result = "Code =" + mErr.errorCode + ", " + "Description =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                    String resultMsg = SocketUtils.getMessage(result, cmd, mErr.errorCode == DJIError.RESULT_OK);
                    callback.notifyResult(resultMsg);
                }

            });
        }
    }

    public void executeStartPhoto(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {

            DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback() {

                @Override
                public void onResult(DJIError mErr) {
                    String result = "Code =" + mErr.errorCode + ", " + "Description =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                    String resultMsg = SocketUtils.getMessage(result, cmd, mErr.errorCode == DJIError.RESULT_OK);
                    callback.notifyResult(resultMsg);
                }
            });
        }
    }

    public void executeStopPhoto(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {
            DJIDrone.getDjiCamera().stopTakePhoto(new DJIExecuteResultCallback() {

                @Override
                public void onResult(DJIError mErr) {
                    String result = "Code =" + mErr.errorCode + ", " + "Description =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                    String resultMsg = SocketUtils.getMessage(result, cmd, mErr.errorCode == DJIError.RESULT_OK);
                    callback.notifyResult(resultMsg);
                }
            });
        }
    }

    public void executePitchUp(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {
            new Thread() {
                public void run() {
                    DJIGimbalRotation mPitch = null;
                    if (DJIDrone.getDjiCamera().getVisionType() == CameraVisionType.Camera_Type_Plus) {
                        mPitch = new DJIGimbalRotation(true, true, false, 150);
                    } else {
                        mPitch = new DJIGimbalRotation(true, true, false, 20);
                    }
                    DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false, false, 0);
                    DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch, null, null);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop, null, null);

                    String resultMsg = SocketUtils.getMessage("", cmd, true);
                    callback.notifyResult(resultMsg);
                }
            }.start();
        }
    }

    public void executePitchDown(final ICommandCallback callback, final String cmd) {
        if (checkCameraStatus(callback, cmd)) {
            new Thread() {
                public void run() {
                    DJIGimbalRotation mPitch = null;
                    if (DJIDrone.getDjiCamera().getVisionType() == CameraVisionType.Camera_Type_Plus) {
                        mPitch = new DJIGimbalRotation(true, false, false, 150);
                    } else {
                        mPitch = new DJIGimbalRotation(true, false, false, 20);
                    }

                    DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false, false, 0);

                    DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch, null, null);

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop, null, null);

                    String resultMsg = SocketUtils.getMessage("", cmd, true);
                    callback.notifyResult(resultMsg);
                }
            }.start();
        }

    }

    public void executePitch(final ICommandCallback callback, final String cmd, final int pitchValue) {
        if (checkCameraStatus(callback, cmd)) {
            int minValue = DJIDrone.getDjiGimbal().getGimbalPitchMinAngle();
            int maxValue = DJIDrone.getDjiGimbal().getGimbalPitchMaxAngle();
            if (pitchValue < minValue || pitchValue > maxValue) {
                String resultMsg = SocketUtils.getMessage("Pitch value should between " + minValue + " and " + maxValue + "!", cmd);
                callback.notifyResult(resultMsg);
                return;
            }

            // Check if is pitch up
            final boolean isPitchUp = DJIStateManager.getInstance().getCameraPitch() > pitchValue;

            new Thread() {
                public void run() {
                    DJIGimbalRotation mPitch = null;
                    int pitchStep;
                    if (DJIDrone.getDjiCamera().getVisionType() == CameraVisionType.Camera_Type_Plus) {
                        pitchStep = 50;
                        mPitch = new DJIGimbalRotation(true, isPitchUp, false, pitchStep); // 150
                    } else {
                        pitchStep = 20;
                        mPitch = new DJIGimbalRotation(true, isPitchUp, false, pitchStep);
                    }

                    DJIGimbalRotation mPitch_stop = new DJIGimbalRotation(false, false, false, 0);
                    // Firstly, pitch special steps to a pitch value which is close to target value
                    int pitchCnt = (int) (Math.abs(DJIStateManager.getInstance().getCameraPitch() - pitchValue) / pitchStep);
                    for(int i = 0; i < pitchCnt; i ++) {
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch, null, null);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    while (true) {
                        double deltaValue = Math.abs(DJIStateManager.getInstance().getCameraPitch() - pitchValue);
                        if (deltaValue <= pitchStep
                                || isPitchUp && DJIStateManager.getInstance().getCameraPitch() <= pitchValue
                                || !isPitchUp && DJIStateManager.getInstance().getCameraPitch() >= pitchValue) {
                            break;
                        }
                        DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch, null, null);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                    DJIDrone.getDjiGimbal().updateGimbalAttitude(mPitch_stop, null, null);

                    String resultMsg = SocketUtils.getMessage("", cmd, true);
                    callback.notifyResult(resultMsg);
                }
            }.start();
        }
    }

    public void executeYAWValue(final ICommandCallback callback, final String cmd, final int angle) {
        if (checkCameraStatus(callback, cmd)) {
            new Thread() {
                boolean yawLeft = angle < 0;
                int yawValue = Math.abs(angle);

                DJIGimbalRotation mYaw = new DJIGimbalRotation(true, yawLeft, false, yawValue);
                DJIGimbalRotation mYaw_stop = new DJIGimbalRotation(true, false, false, 0);

                public void run() {
                    DJIDrone.getDjiGimbal().updateGimbalAttitude(null, null, mYaw);

                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DJIDrone.getDjiGimbal().updateGimbalAttitude(null, null, mYaw_stop);

                    String resultMsg = SocketUtils.getMessage("", cmd, true);
                    callback.notifyResult(resultMsg);
                }
            }.start();
        }
    }

    public void executeFormatSDCard(final ICommandCallback callback, final String cmd) {

        DJIDrone.getDjiCamera().formatSDCard(new DJIExecuteResultCallback() {

            @Override
            public void onResult(DJIError mErr) {
                String resultMsg = null;
                if (mErr.errorCode == DJIError.RESULT_OK) {
                    resultMsg = SocketUtils.getMessage(mErr.errorDescription, cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage(mErr.errorDescription, cmd);
                }
                callback.notifyResult(resultMsg);
            }

        });
    }

    public void executeOpenGS(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult groundStationResult) {
                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(groundStationResult)) {
                    resultMsg = SocketUtils.getMessage("OpenGroundStation successfully! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("OpenGroundStation failed! " + groundStationResult.name(), cmd);
                }

                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeAddWayPoint(final ICommandCallback callback, final String cmd, final DJIGroundStationWaypoint wayPoint, final int index) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }

        if (index >= 0 && index < CommandServer.getGSTask().getAllWaypoint().size()) {
            CommandServer.getGSTask().insertWaypoint(wayPoint, index);
        } else {
            CommandServer.getGSTask().addWaypoint(wayPoint);
        }

        String resultMsg = SocketUtils.getMessage("Cached way point into Task!" + getWayPointStr(), cmd, true);
        callback.notifyResult(resultMsg);
    }


    public void executeClearWayPoint(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        CommandServer.getGSTask().RemoveAllWaypoint();
        String resultMsg = SocketUtils.getMessage("Removed all cached way points from Task!" + getWayPointStr(), cmd, true);
        callback.notifyResult(resultMsg);
    }

    public void executeUploadWayPoints(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        List<DJIGroundStationWaypoint> wayPointList = CommandServer.getGSTask().getAllWaypoint();

        if (!wayPointList.isEmpty()) {
            SocketLogger.log(SocketLogger.LogPriority.debug, CommandLoader.class, "Upload way points %s", getWayPointStr());
        } else {
            String resultMsg = SocketUtils.getMessage("No valid way points is set!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }

        DJIDrone.getDjiGroundStation().uploadGroundStationTask(CommandServer.getGSTask(), new DJIGroundStationExecuteCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                    resultMsg = SocketUtils.getMessage("Upload way points to drone successfully! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("Upload way points to drone failed! >" + result.name(), cmd);
                }
                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeTakeOff(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().startGroundStationTask(new DJIGroundStationTakeOffCallBack() {

            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationTakeOffResult result) {
                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationTakeOffResult.GS_Takeoff_Successed.equals(result)) {
                    resultMsg = SocketUtils.getMessage(result.name(), cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage(result.name(), cmd);
                }
                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeCloseGS(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResult groundStationResult) {
                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(groundStationResult)) {
                    resultMsg = SocketUtils.getMessage("CloseGroundStation successfully! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("CloseGroundStation failed! " + groundStationResult.name(), cmd);
                }

                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeDronePause(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationHoverResult groundStationHoverResult) {

                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationHoverResult.GS_Hover_Successed.equals(groundStationHoverResult)) {
                    resultMsg = SocketUtils.getMessage("PauseGroundStationTask successfully! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("PauseGroundStationTask failed! " + groundStationHoverResult.name(), cmd);
                }
                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeDroneResume(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().continueGroundStationTask(new DJIGroundStationResumeCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationResumeResult groundStationResumeResult) {

                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationResumeResult.GS_Resume_Successed.equals(groundStationResumeResult)) {
                    resultMsg = SocketUtils.getMessage("ResumeGroundStationTask successfully! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("ResumeGroundStationTask failed! " + groundStationResumeResult.name(), cmd);
                }
                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeGoHome(final ICommandCallback callback, final String cmd) {
        if (!DJIStateManager.getInstance().hasHomePoint()) {
            String resultMsg = SocketUtils.getMessage("Can not get home point!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        DJIDrone.getDjiGroundStation().goHome(new DJIGroundStationGoHomeCallBack() {
            @Override
            public void onResult(DJIGroundStationTypeDef.GroundStationGoHomeResult groundStationGoHomeResult) {

                String resultMsg = null;
                if (DJIGroundStationTypeDef.GroundStationGoHomeResult.GS_GoHome_Successed.equals(groundStationGoHomeResult)) {
                    resultMsg = SocketUtils.getMessage("GoHome successfully! ", cmd, true);
                } else if (DJIGroundStationTypeDef.GroundStationGoHomeResult.GS_GoHome_Began.equals(groundStationGoHomeResult)) {
                    resultMsg = SocketUtils.getMessage("GoHome start! ", cmd, true);
                } else {
                    resultMsg = SocketUtils.getMessage("GoHome : " + groundStationGoHomeResult.name(), cmd);
                }
                callback.notifyResult(resultMsg);
            }
        });
    }

    public void executeGsYAW(final ICommandCallback callback, final String cmd, final int speed) {

        new Thread() {
            public void run() {
                DJIDrone.getDjiGroundStation().setAircraftYawSpeed(speed, new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        String resultMsg = null;
                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                            resultMsg = SocketUtils.getMessage("SetAircraftYawSpeed to " + speed + " successfully! ", cmd, true);
                        } else {
                            resultMsg = SocketUtils.getMessage("SetAircraftYawSpeed to " + speed + result.name(), cmd);
                        }
                        callback.notifyResult(resultMsg);
                    }
                });
            }
        }.start();
    }

    public void executeGsPitch(final ICommandCallback callback, final String cmd, final int speed) {

        new Thread() {
            public void run() {
                DJIDrone.getDjiGroundStation().setAircraftPitchSpeed(speed, new DJIGroundStationExecuteCallBack() {
                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        String resultMsg = null;
                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                            resultMsg = SocketUtils.getMessage("SetAircraftPitchSpeed to " + speed + "  successfully! ", cmd, true);
                        } else {
                            resultMsg = SocketUtils.getMessage("SetAircraftPitchSpeed to " + speed + result.name(), cmd);
                        }
                        callback.notifyResult(resultMsg);
                    }
                });
            }
        }.start();
    }

    public void executeGsRoll(final ICommandCallback callback, final String cmd, final int speed) {

        new Thread() {
            public void run() {
                DJIDrone.getDjiGroundStation().setAircraftRollSpeed(speed, new DJIGroundStationExecuteCallBack() {
                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        String resultMsg = null;
                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                            resultMsg = SocketUtils.getMessage("SetAircraftRollSpeed to " + speed + "  successfully! ", cmd, true);
                        } else {
                            resultMsg = SocketUtils.getMessage("SetAircraftRollSpeed to " + speed + result.name(), cmd);
                        }
                        callback.notifyResult(resultMsg);
                    }
                });
            }
        }.start();
    }

    public void executeGsThrottle(final ICommandCallback callback, final String cmd, final int throttle) {

        new Thread() {
            public void run() {
                DJIDrone.getDjiGroundStation().setAircraftThrottle(throttle, new DJIGroundStationExecuteCallBack() {
                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        String resultMsg = null;
                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                            resultMsg = SocketUtils.getMessage("SetAircraftThrottle to " + throttle + " successfully! ", cmd, true);
                        } else {
                            resultMsg = SocketUtils.getMessage("SetAircraftThrottle to " + throttle + result.name(), cmd);
                        }
                        callback.notifyResult(resultMsg);
                    }
                });
            }
        }.start();
    }

    public void executeGsYAWRotateTakePhoto(final ICommandCallback callback, final String cmd) {
        if (mCheckYawTimer != null) {
            String resultMsg = SocketUtils.getMessage("Can not take YAW rotate phone now!", cmd);
            callback.notifyResult(resultMsg);
            return;
        }
        checkYawTimes = 0;
        mCheckYawTimer = new Timer();

        CheckYawTask mCheckYawTask = new CheckYawTask(callback);
        mCheckYawTimer.schedule(mCheckYawTask, 100, 3000);

        new Thread() {
            public void run() {
                DJIDrone.getDjiGroundStation().setAircraftYawSpeed(100, new DJIGroundStationExecuteCallBack() {
                    @Override
                    public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                        String resultMsg = null;
                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                            resultMsg = SocketUtils.getMessage("RotateTakePhoto SetAircraftYawSpeed to 100 successfully! ", cmd, true);
                        } else {
                            resultMsg = SocketUtils.getMessage("RotateTakePhoto SetAircraftYawSpeed to 100 : " + result.name(), cmd);
                        }
                        callback.notifyResult(resultMsg);
                    }
                });
            }
        }.start();
    }

    public void executeDroneStatus(final ICommandCallback callback, final String cmd, final int interval) {
        if (checkCameraStatus(callback, cmd)) {
            DJIStateManager.getInstance().updateTimer(interval);

            DJIStateManager.getInstance().addMcuUpdateStateListener(new IDJIMcuUpdateStateListener() {
                @Override
                public void notifyMcuUpdateState(DJIMainControllerSystemState state) {
                    if (droneStatus.isFilled()) {
                        String resultMsg = SocketUtils.getMessage(droneStatus.toString(), cmd, true);
                        callback.notifyResult(resultMsg);
                    }
                    droneStatus.setDroneState(state);
                }

                @Override
                public void notifyMcuErrorState(DJIMcErrorType error) {
                    String resultMsg = SocketUtils.getMessage(error.name(), cmd);
                    callback.notifyResult(resultMsg);
                }
            });
            DJIStateManager.getInstance().addGimbalAttitudeListener(new IDJIGimbalAttitudeListener() {
                @Override
                public void notifyGimbalAttitude(DJIGimbalAttitude djiGimbalAttitude) {
                    droneStatus.setCameraState(djiGimbalAttitude);
                }
            });
        }
    }

    private String getWayPointStr() {
        StringBuilder builder = new StringBuilder();
        for (DJIGroundStationWaypoint point : CommandServer.getGSTask().getAllWaypoint()) {
            builder.append("[").append(point.latitude).append(",").append(point.lontitude).append("],");
        }
        return builder.toString();
    }

    private boolean checkCameraStatus(ICommandCallback callback, String cmd) {
        boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
        if (!bConnectState) {
            String resultMsg = SocketUtils.getMessage("Camera not connected.", cmd);
            SocketLogger.log(SocketLogger.LogPriority.debug, CheckYawTask.class, resultMsg);
            callback.notifyResult(resultMsg);
            return false;
        }
        return true;
    }

    class CheckYawTask extends TimerTask {
        private ICommandCallback callback;

        public CheckYawTask(ICommandCallback cb) {
            this.callback = cb;
        }

        @Override
        public void run() {
            if (checkYawTimes >= 12) {
                if (mCheckYawTimer != null) {
                    SocketLogger.log(SocketLogger.LogPriority.debug, CheckYawTask.class, "==========>mCheckYawTimer cancel!");
                    mCheckYawTimer.cancel();
                    mCheckYawTimer.purge();
                    mCheckYawTimer = null;

                    new Thread() {
                        public void run() {

                            DJIDrone.getDjiGroundStation().setAircraftYawSpeed(0, new DJIGroundStationExecuteCallBack() {

                                @Override
                                public void onResult(DJIGroundStationTypeDef.GroundStationResult result) {
                                    if (callback != null) {
                                        String resultMsg = null;
                                        if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(result)) {
                                            resultMsg = SocketUtils.getMessage("TakeRotatePhoto SetAircraftYawSpeed to 0 successfully! ");
                                        } else {
                                            resultMsg = SocketUtils.getMessage("TakeRotatePhoto SetAircraftYawSpeed to 0 : " + result.name());
                                        }
                                        callback.notifyResult(resultMsg);
                                    }
                                }

                            });
                        }
                    }.start();
                }
                return;
            }

            checkYawTimes++;
            SocketLogger.log(SocketLogger.LogPriority.debug, CheckYawTask.class, "==========>mCheckYawTimer checkYawTimes=" + checkYawTimes);

            new Thread() {
                public void run() {
                    DJIDrone.getDjiGroundStation().setAircraftYawSpeed(300, new DJIGroundStationExecuteCallBack() {
                        @Override
                        public void onResult(DJIGroundStationTypeDef.GroundStationResult groundStationResult) {
                            if (callback != null) {
                                String resultMsg = null;
                                if (DJIGroundStationTypeDef.GroundStationResult.GS_Result_Successed.equals(groundStationResult)) {
                                    resultMsg = SocketUtils.getMessage("TakeRotatePhoto SetAircraftYawSpeed to 300 successfully! ");
                                } else {
                                    resultMsg = SocketUtils.getMessage("TakeRotatePhoto SetAircraftYawSpeed to 300 : " + groundStationResult.name());
                                }
                                callback.notifyResult(resultMsg);
                            }
                        }
                    });

                    DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback() {
                        @Override
                        public void onResult(DJIError mErr) {
                            SocketLogger.log(SocketLogger.LogPriority.debug, CheckYawTask.class, "Start take photo errorCode = " + mErr.errorCode);
                            SocketLogger.log(SocketLogger.LogPriority.debug, CheckYawTask.class, "Start take photo errorDescription = " + mErr.errorDescription);

                            String result = SocketUtils.getMessage("TakeRotatePhoto errorCode =" + mErr.errorCode + "\n" + "errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode));
                            if (callback != null) {
                                callback.notifyResult(result);
                            }
                        }

                    });
                }
            }.start();
        }
    }

    class DroneStatus {
        private DJIMainControllerSystemState state;
        private DJIGimbalAttitude djiGimbalAttitude;

        public void setDroneState(DJIMainControllerSystemState state) {
            this.state = state;
        }

        public void setCameraState(DJIGimbalAttitude djiGimbalAttitude) {
            this.djiGimbalAttitude = djiGimbalAttitude;
        }

        public synchronized boolean isFilled() {
            return state != null && djiGimbalAttitude != null;
        }

        public synchronized String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append(state.droneLocationLatitude).append(",");
            builder.append(state.droneLocationLongitude).append(",");
            builder.append(state.altitude).append(",");
            builder.append(state.speed).append(",");
            builder.append(state.homeLocationLatitude).append(",");
            builder.append(state.homeLocationLongitude).append(",");
            builder.append(state.satelliteCount).append(",");
            builder.append(state.velocityX).append(",");
            builder.append(state.velocityY).append(",");
            builder.append(state.velocityZ).append(",");
            builder.append(state.pitch).append(",");
            builder.append(state.roll).append(",");
            builder.append(state.yaw).append(",");
            builder.append(state.remainPower).append(",");
            builder.append(state.remainFlyTime).append(",");
            builder.append(state.powerLevel).append(",");
            builder.append(state.isFlying).append(",");
            builder.append(state.noFlyStatus).append(",");
            builder.append(state.noFlyZoneCenterLatitude).append(",");
            builder.append(state.noFlyZoneCenterLongitude).append(",");
            builder.append(state.noFlyZoneRadius).append(",");
            builder.append(djiGimbalAttitude.pitch).append(",");
            builder.append(djiGimbalAttitude.roll).append(",");
            builder.append(djiGimbalAttitude.yaw).append(",");
            builder.append(DJIStateManager.getInstance().hasHomePoint() ? 1 : 0);
            return builder.toString();
        }
    }
}
