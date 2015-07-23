package com.autodesk.drone.iw.asdk.console;

/**
 * Created by DJia on 2/15/15.
 */
public interface ConsoleLogMessageListener {
    public void notifyMessage(String message);
    public void notifyGSStatus(boolean hasHome, double lat, double lng);
}
