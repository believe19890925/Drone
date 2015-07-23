package com.autodesk.drone.iw.asdk.socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dongjiawei on 3/10/15.
 */
public class SocketUtils {

    public static String getMessage(String message) {
        return getMessage(message, null);
    }

    public static String getMessage(String message, String cmd) {
        return getMessage(message, cmd, false);
    }

    public static String getMessage(String message, String cmd, boolean isOK) {
        SocketLogger.log(SocketLogger.LogPriority.info, SocketUtils.class, " cmd >" + cmd + ", message = " + message);
        JSONObject response = new JSONObject();
        try {
            response.put("cmd", cmd == null ? "" : cmd);
            response.put("status", isOK ? "OK" : "NG");
            response.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public static double getJsonDouble(JSONObject jsonObject, String key, double defaultValue) {
        double returnValue = defaultValue;
        if (jsonObject.has(key)) {
            try {
                returnValue = jsonObject.getDouble(key);
            } catch (Exception ex) {
            }
        }
        return returnValue;
    }

    public static int getJsonInt(JSONObject jsonObject, String key, int defaultValue) {
        int returnValue = defaultValue;
        if (jsonObject.has(key)) {
            try {
                returnValue = jsonObject.getInt(key);
            } catch (Exception ex) {
            }
        }
        return returnValue;
    }

    public static boolean getJsonBoolean(JSONObject jsonObject, String key, boolean defaultValue) {
        boolean returnValue = defaultValue;
        if (jsonObject.has(key)) {
            try {
                returnValue = jsonObject.getBoolean(key);
            } catch (Exception ex) {
            }
        }
        return returnValue;
    }
}
