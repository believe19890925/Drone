package com.autodesk.drone.iw.asdk.app;

import android.app.Application;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by dongjiawei on 12/23/14.
 */
public class DroneApplication extends Application {

    static {
        final LogConfigurator configurator = new LogConfigurator();
        File sdCardFolder = Environment.getExternalStorageDirectory();
        File folder = new File(sdCardFolder, "drone");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        configurator.setFileName(Environment.getExternalStorageDirectory() + File.separator + "drone" + File.separator + "drone_app.log");
        configurator.setRootLevel(Level.INFO);
        configurator.setMaxBackupSize(10);
        configurator.setMaxFileSize(5 * 1024 * 1024);

        configurator.configure();
    }

    private static DroneApplication application;

    public static synchronized DroneApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        Thread.setDefaultUncaughtExceptionHandler(new DefaultTreadHandler());
    }

    public void toastLongMessage(final String message) {
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(DroneApplication.this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
    }

    public void toastShortMessage(final String message) {
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(DroneApplication.this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
    }

    class DefaultTreadHandler implements Thread.UncaughtExceptionHandler {
        private Logger logger;
        private Thread.UncaughtExceptionHandler defaultUEH;

        public DefaultTreadHandler() {
            logger = Logger.getLogger(DroneApplication.class);
            defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            StackTraceElement[] arr = throwable.getStackTrace();
            StringBuilder report = new StringBuilder();
            report.append(throwable.toString()).append("\n");
            report.append("--------- Stack trace ---------\n");
            for (int i = 0; i < arr.length; i++) {
                report.append("    " + arr[i].toString() + "\n");
            }
            report.append("-------------------------------\n");

            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause
            report.append("--------- Cause ---------\n");
            Throwable cause = throwable.getCause();
            if (cause != null) {
                report.append(cause.toString() + "\n");
                arr = cause.getStackTrace();
                for (int i = 0; i < arr.length; i++) {
                    report.append("    " + arr[i].toString() + "\n");
                }
            }
            report.append("-------------------------------\n");

            logger.error("DefaultTreadHandler", throwable);

            defaultUEH.uncaughtException(thread, throwable);
        }
    }
}
