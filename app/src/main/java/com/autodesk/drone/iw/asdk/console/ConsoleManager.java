package com.autodesk.drone.iw.asdk.console;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DJia on 2/15/15.
 */
public class ConsoleManager {

    private static List<ConsoleLogMessageListener> listenerList = new ArrayList<ConsoleLogMessageListener>();
    public static void registerConsoleListener(ConsoleLogMessageListener listener) {
        if (listener != null && !listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }
    public static void removeConsoleListener(ConsoleLogMessageListener listener) {
        if (listener != null && listenerList.contains(listener)) {
            listenerList.remove(listener);
        }
    }
    public static void console(String message){
        for (ConsoleLogMessageListener listener : listenerList) {
            listener.notifyMessage(message);
        }
    }
    public static void gsStatus(boolean hasHome, double lat, double lng){
        for (ConsoleLogMessageListener listener : listenerList) {
            listener.notifyGSStatus(hasHome, lat, lng);
        }
    }
}
