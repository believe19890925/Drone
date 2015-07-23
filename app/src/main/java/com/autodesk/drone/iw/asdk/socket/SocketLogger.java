package com.autodesk.drone.iw.asdk.socket;

import org.apache.log4j.Logger;

/**
 * Created by dongjia on 12/18/2014.
 */
public class SocketLogger {
    public enum LogPriority {
        error,
        warn,
        info,
        debug
    }

    public static void log(LogPriority p, Class<?> clazz, String msg) {
        log(clazz, msg, null, p);
    }

    public static void log(LogPriority p, Class<?> clazz, String msg, Throwable t) {
        log(clazz, msg, t, p);
    }

    public static void log(LogPriority p, Class<?> clazz, String msg, Object... msgArgs) {
        log(clazz, msg, null, p, msgArgs);
    }

    public static void log(LogPriority p, Class<?> clazz, String msg, Throwable t, Object... msgArgs) {
        log(clazz, msg, t, p, msgArgs);
    }

    private static void log(Class<?> clazz, String msg, Throwable t, LogPriority priority, Object... msgArgs) {
        try {
            if (msg != null && msgArgs != null && msgArgs.length > 0) {
                msg = String.format(msg, msgArgs);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Logger logger = Logger.getLogger(clazz);

        switch (priority) {
            case error:
                logger.error(msg, t);
                break;
            case warn:
                logger.warn(msg, t);
                break;
            case info:
                logger.info(msg, t);
                break;
            case debug:
                logger.info(msg, t);
                break;
        }
    }
}
