package com.autodesk.drone.iw.asdk.socket.dji;

import dji.sdk.api.Gimbal.DJIGimbalAttitude;

/**
 * Created by dongjiawei on 3/23/15.
 */
public interface IDJIGimbalAttitudeListener {
    public void notifyGimbalAttitude(DJIGimbalAttitude djiGimbalAttitude);
}
