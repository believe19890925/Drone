package com.autodesk.drone.iw.asdk.console;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by DJia on 2/15/15.
 */
public class ConsoleItem {
    private String message;
    private String time;

    public ConsoleItem(String message) {
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;

        // Setup time string
        Calendar date = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        this.time = format.format(date.getTime());
    }

    public String getTime() {
        return time;
    }
}
