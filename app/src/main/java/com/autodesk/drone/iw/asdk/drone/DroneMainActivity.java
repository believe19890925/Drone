package com.autodesk.drone.iw.asdk.drone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.autodesk.drone.iw.asdk.MainActivity;
import com.autodesk.drone.iw.asdk.R;
import com.autodesk.drone.iw.asdk.console.ConsoleLogMessageListener;
import com.autodesk.drone.iw.asdk.console.ConsoleManager;
import com.autodesk.drone.iw.asdk.console.ConsoleViewAdapter;
import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.SocketServerService;
import com.autodesk.drone.iw.asdk.socket.dji.DJIStateManager;
import com.autodesk.drone.iw.asdk.util.NetUtil;

/**
 * Created by dongjiawei on 5/19/15.
 * Implement drone main activity which will append our own logic when APP startup.
 */
public class DroneMainActivity extends MainActivity implements ConsoleLogMessageListener {
    private static final String TAG = "DroneMainActivity";

    private SocketServerService socketService = null;
    private TextView socketView = null;
    private ListView consoleListView = null;
    private ConsoleViewAdapter consoleViewAdapter = null;
    private TextView groundStationStatus = null;
    private Button ipAddressRefresh;
    private TextView ipAddressText;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "ServiceConnection.onServiceConnected!");
            socketService = ((SocketServerService.LocalBinder) binder).getService();
            String status = socketService.getStatus();
            Log.i(TAG, "Statis text = " + status);
            Toast.makeText(DroneMainActivity.this, status, Toast.LENGTH_LONG).show();

            socketView.setText(status);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "ServiceConnection.onServiceDisconnected!");
            socketService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        socketView = (TextView) findViewById(R.id.socketTextView);
        ipAddressRefresh = (Button) findViewById(R.id.ipAddressRefresh);
        ipAddressRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = NetUtil.getWifiLocalIpAddress(DroneMainActivity.this.getApplicationContext());
                if (ipAddress == null || "0.0.0.0".equals(ipAddress)) {
                    ipAddress = NetUtil.getHostIp();
                }

                String text = DroneMainActivity.this.getResources().getString(R.string.main_ip_text, ipAddress);
                ipAddressText.setText(text);
            }
        });

        ipAddressText = (TextView) findViewById(R.id.ipAddressText);

        consoleListView = (ListView) findViewById(R.id.consoleView);
        consoleViewAdapter = new ConsoleViewAdapter(this);
        consoleListView.setAdapter(consoleViewAdapter);

        groundStationStatus = (TextView) findViewById(R.id.groundStationStatus);

        onBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConsoleManager.registerConsoleListener(this);
        DJIStateManager.getInstance().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConsoleManager.removeConsoleListener(this);
        DJIStateManager.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onUnbindService();
    }

    private void onBindService() {
        Intent intent = new Intent(this, SocketServerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void onUnbindService() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public void notifyMessage(final String message) {
        if (message != null && message.trim().length() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    consoleViewAdapter.addMessageItem(message.trim());
                }
            });
        }
    }

    @Override
    public void notifyGSStatus(final boolean hasHome, final double lat, final double lng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = DroneMainActivity.this.getResources().getString(R.string.socket_gs_status, String.valueOf(hasHome), lat, lng);
                groundStationStatus.setText(text);
            }
        });
    }
}
