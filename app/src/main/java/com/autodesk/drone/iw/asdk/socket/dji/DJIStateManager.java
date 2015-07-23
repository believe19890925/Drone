package com.autodesk.drone.iw.asdk.socket.dji;

import com.autodesk.drone.iw.asdk.socket.SocketLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.Gimbal.DJIGimbal;
import dji.sdk.api.Gimbal.DJIGimbalAttitude;
import dji.sdk.api.MainController.DJIMainController;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef;
import dji.sdk.interfaces.DJIGimbalErrorCallBack;
import dji.sdk.interfaces.DJIGimbalUpdateAttitudeCallBack;
import dji.sdk.interfaces.DJIMcuErrorCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;

/**
 * Created by dongjiawei on 3/23/15.
 */
public class DJIStateManager {
    private List<IDJIMcuUpdateStateListener> mcuUpdateStateListenerList = Collections.synchronizedList(new ArrayList<IDJIMcuUpdateStateListener>());
    private List<IDJIGimbalAttitudeListener> gimbalAttitudeListenerList = Collections.synchronizedList(new ArrayList<IDJIGimbalAttitudeListener>());
    private List<IDJICameraConnectListener> cameraConnectListenerList = Collections.synchronizedList(new ArrayList<IDJICameraConnectListener>());

    private static DJIStateManager manager = new DJIStateManager();

    private double homeLocationLatitude = -1;
    private double homeLocationLongitude = -1;
    private boolean hasHomePoint = false;
    private double cameraPitch = -1.0;
    private double gimbalYaw = 0;

    private int currentInterval = 1;
    private Timer mTimer;
    private boolean isStateRunning = false;

    private DJIStateManager() {
    }

    public static DJIStateManager getInstance() {
        return manager;
    }

    public void start() {
        SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJIStateManager start!!");
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);
    }

    public void stop() {
        SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJIStateManager stop!!");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        stopDJITimer();
        currentInterval = 1;
        cameraPitch = -1.0;
        hasHomePoint = false;
        homeLocationLatitude = -1;
        homeLocationLongitude = -1;
    }

    public void updateTimer(int interval) {
        interval = interval < 1 ? 1 : interval;
        if (currentInterval == interval) {
            return;
        }
        currentInterval = interval;
        stopDJITimer();
        starDJITimer();
    }

    public void addMcuUpdateStateListener(IDJIMcuUpdateStateListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (mcuUpdateStateListenerList) {
            // If too many instances added, clear the cache list.
            if (mcuUpdateStateListenerList.size() > 10) {
                mcuUpdateStateListenerList.clear();
            }
            if (!mcuUpdateStateListenerList.contains(listener)) {
                mcuUpdateStateListenerList.add(listener);
            }
        }
    }

    public void addCameraStatusListener(IDJICameraConnectListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (cameraConnectListenerList) {

            if (!cameraConnectListenerList.contains(listener)) {
                cameraConnectListenerList.add(listener);
            }
        }
    }

    public void addGimbalAttitudeListener(IDJIGimbalAttitudeListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (gimbalAttitudeListenerList) {
            // If too many instances added, clear the cache list.
            if (gimbalAttitudeListenerList.size() > 10) {
                gimbalAttitudeListenerList.clear();
            }
            if (!gimbalAttitudeListenerList.contains(listener)) {
                gimbalAttitudeListenerList.add(listener);
            }
        }
    }

    private void removeCameraStateListener(IDJICameraConnectListener listener) {
        if (listener == null) {
            return;
        }
        if (cameraConnectListenerList.contains(listener)) {
            cameraConnectListenerList.remove(listener);
        }
    }

    private void removeGimbalAttitudeListener(IDJIGimbalAttitudeListener listener) {
        if (listener == null) {
            return;
        }
        if (gimbalAttitudeListenerList.contains(listener)) {
            gimbalAttitudeListenerList.remove(listener);
        }
    }

    private void removeMcuUpdateStateListener(IDJIMcuUpdateStateListener listener) {
        if (listener == null) {
            return;
        }
        if (mcuUpdateStateListenerList.contains(listener)) {
            mcuUpdateStateListenerList.remove(listener);
        }
    }

    private void stopDJITimer() {
        isStateRunning = false;

        DJIMainController controller = DJIDrone.getDjiMC();
        if (controller != null) {
            controller.stopUpdateTimer();
        }
        DJIGimbal djiGimbal = DJIDrone.getDjiGimbal();
        if (djiGimbal != null) {
            djiGimbal.stopUpdateTimer();
        }
    }

    private void starDJITimer() {
        isStateRunning = true;

        initMcuUpdateState(currentInterval);
        initGimbalUpdateAttitude(currentInterval);
    }

    private void initMcuUpdateState(int interval) {
        SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJIStateManager McuUpdateState start!! interval = " + interval);
        DJIDrone.getDjiMC().startUpdateTimer(interval);

        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {
            @Override
            public void onResult(DJIMainControllerSystemState state) {
                homeLocationLatitude = state.homeLocationLatitude;
                homeLocationLongitude = state.homeLocationLongitude;

                if (homeLocationLatitude != -1 && homeLocationLongitude != -1
                        && homeLocationLatitude != 0 && homeLocationLongitude != 0) {
                    hasHomePoint = true;
                } else {
                    hasHomePoint = false;
                }
                SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJI hasHome = " + hasHomePoint + ", [" + homeLocationLatitude + "," + homeLocationLongitude + "], Camera pitch = " + cameraPitch);

                synchronized (mcuUpdateStateListenerList) {
                    for (IDJIMcuUpdateStateListener listener : mcuUpdateStateListenerList) {
                        try {
                            if (listener != null) {
                                listener.notifyMcuUpdateState(state);
                            }
                        } catch (Exception ex) {
                            removeMcuUpdateStateListener(listener);
                            SocketLogger.log(SocketLogger.LogPriority.error, DJIStateManager.class, "## Error in notifyMcuUpdateState! Remove it. ");
                        }
                    }
                }
            }
        });

        DJIDrone.getDjiMC().setMcuErrorCallBack(new DJIMcuErrorCallBack() {
            @Override
            public void onError(DJIMainControllerTypeDef.DJIMcErrorType error) {
                synchronized (mcuUpdateStateListenerList) {
                    for (IDJIMcuUpdateStateListener listener : mcuUpdateStateListenerList) {
                        try {
                            if (listener != null) {
                                listener.notifyMcuErrorState(error);
                            }
                        } catch (Exception ex) {
                            removeMcuUpdateStateListener(listener);
                            SocketLogger.log(SocketLogger.LogPriority.error, DJIStateManager.class, "## Error in notifyMcuErrorState! Remove the listener. ");
                        }
                    }
                }
            }
        });
    }

    private void initGimbalUpdateAttitude(int interval) {
        SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJIStateManager GimbalUpdateAttitude start!! interval = " + interval);
        DJIDrone.getDjiGimbal().startUpdateTimer(interval);
        DJIDrone.getDjiGimbal().setGimbalUpdateAttitudeCallBack(new DJIGimbalUpdateAttitudeCallBack() {
            @Override
            public void onResult(DJIGimbalAttitude djiGimbalAttitude) {
                cameraPitch = djiGimbalAttitude != null ? djiGimbalAttitude.pitch : -1.0;
                gimbalYaw = djiGimbalAttitude != null ? djiGimbalAttitude.yaw : 0;
                synchronized (gimbalAttitudeListenerList) {
                    for (IDJIGimbalAttitudeListener listener : gimbalAttitudeListenerList) {
                        try {
                            if (listener != null) {
                                listener.notifyGimbalAttitude(djiGimbalAttitude);
                            }
                        } catch (Exception ex) {
                            removeGimbalAttitudeListener(listener);
                            SocketLogger.log(SocketLogger.LogPriority.warn, DJIStateManager.class, "## Error in notifyGimbalAttitude! Remove the listener. ");
                        }
                    }
                }
            }
        });
        DJIDrone.getDjiGimbal().setGimbalErrorCallBack(new DJIGimbalErrorCallBack() {
            @Override
            public void onError(int i) {

            }
        });
    }

    public double getCameraPitch() {
        return cameraPitch;
    }

    public double getGimbalYaw() {
        return gimbalYaw;
    }

    public boolean hasHomePoint() {
        return hasHomePoint;
    }

    class Task extends TimerTask {
        @Override
        public void run() {
            boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
            if (bConnectState && !isStateRunning) {
                SocketLogger.log(SocketLogger.LogPriority.info, DJIStateManager.class, "## DJIStateManager >> Start status listeners for Camera connected!!");
                starDJITimer(); // Start timer with currentInterval
            }

            if (!bConnectState && isStateRunning) {
                stopDJITimer(); // Stop timer if camera not connected
            }

            synchronized (cameraConnectListenerList) {
                for (IDJICameraConnectListener listener : cameraConnectListenerList) {
                    try {
                        if (listener != null) {
                            listener.onCameraState(bConnectState);
                        }
                    } catch (Exception ex) {
                        removeCameraStateListener(listener);
                        SocketLogger.log(SocketLogger.LogPriority.warn, DJIStateManager.class, "## Error in camera status listener, remove it! ");
                    }
                }
            }
        }
    }
}
