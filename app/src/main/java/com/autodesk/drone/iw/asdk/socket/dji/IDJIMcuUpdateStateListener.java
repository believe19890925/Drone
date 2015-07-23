package com.autodesk.drone.iw.asdk.socket.dji;

import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef;

/**
 * Created by dongjiawei on 3/23/15.
 */
public interface IDJIMcuUpdateStateListener {
    public void notifyMcuUpdateState(DJIMainControllerSystemState state);
    public void notifyMcuErrorState(DJIMainControllerTypeDef.DJIMcErrorType error);
}
