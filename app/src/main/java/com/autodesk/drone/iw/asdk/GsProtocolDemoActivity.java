
package com.autodesk.drone.iw.asdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.autodesk.drone.iw.asdk.CameraProtocolDemoActivity.pickerValueChangeListener;
import com.autodesk.drone.iw.asdk.socket.sync.SyncFileService;
import com.autodesk.drone.iw.asdk.widget.PopupNumberPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.Camera.DJICameraFileNamePushInfo;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.DJIError;
import dji.sdk.api.GroundStation.DJIGroundStationExecutionPushInfo;
import dji.sdk.api.GroundStation.DJIGroundStationFlyingInfo;
import dji.sdk.api.GroundStation.DJIGroundStationMissionPushInfo;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.DJIGroundStationFinishAction;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.DJIGroundStationMovingMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.DJIGroundStationPathMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationGoHomeResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHoverResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationOnWayPointAction;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResumeResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationTakeOffResult;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationWayPointExecutionState;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJICameraFileNameInfoCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationExecutionPushInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationFlyingInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationGoHomeCallBack;
import dji.sdk.interfaces.DJIGroundStationHoverCallBack;
import dji.sdk.interfaces.DJIGroundStationMissionPushInfoCallBack;
import dji.sdk.interfaces.DJIGroundStationResumeCallBack;
import dji.sdk.interfaces.DJIGroundStationTakeOffCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedFileDataCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 */
public class GsProtocolDemoActivity extends Activity implements OnClickListener{

    private static final String TAG = "GsProtocolDemoActivity";

    private static final int NAVI_MODE_ATTITUDE = 0;
    private static final int NAVI_MODE_WAYPOINT = 1;
    private static final int EXECUTION_STATUS_UPLOAD_FINISH = 0;
    private static final int EXECUTION_STATUS_FINISH = 1;
    private static final int EXECUTION_STATUS_REACH_POINT = 2;

    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    private DJIGroundStationFlyingInfoCallBack mGroundStationFlyingInfoCallBack = null;
    private DJIGroundStationMissionPushInfoCallBack mGroundStationMissionPushInfoCallBack = null;
    private DJIGroundStationExecutionPushInfoCallBack mGroundStationExecutionPushInfoCallBack = null;
    private DJICameraFileNameInfoCallBack mCameraFileNameInfoCallBack = null;

    private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;

    private Button mFacility_Onekeyfly;
    private Button mFacility_LoadPhotos;
    private Button mOpenGsButton;
    private Button mAddOneWaypointButton;
    private Button mRemoveWaypointButton;
    private Button mSetLoop;
    private Button mUploadWaypointButton;
    private Button mTakeOffButton;
    private Button mGohomeButton;
    private Button mCloseGsButton;
    private Button mPauseButton;
    private Button mResumeButton;

    private Button mYawLeftBtn;
    private Button mYawRightBtn;
    private Button mYawStopBtn;
    private Button mYawRotateTakePhotoBtn;
    private Button mDownloadResultBtn;

    private Button mPitchPlusBtn;
    private Button mPitchMinusBtn;
    private Button mPitchStopBtn;
    private Button mRollPlusBtn;
    private Button mRollMinusBtn;
    private Button mRollStopBtn;
    private Button mThottlePlusBtn;
    private Button mThottleMinusBtn;
    private Button mThottleStopBtn;

    private TextView mGroundStationTextView;
    private ScrollView mGroundStationInfoScrollView;

    private final int SHOWTOAST = 1;

    private double homeLocationLatitude = -1;
    private double homeLocationLongitude = -1;
    private boolean getHomePointFlag = false;
    private DJIGroundStationTask mTask;

    private TextView mConnectStateTextView;
    private TextView showTV;

    private Timer mTimer;
    private Timer mCheckYawTimer;

    private PopupNumberPicker mPopupNumberPicker = null;
    private static Context m_context;

    private View downloadResultView;
    private AlertDialog.Builder builder;

    private StringBuffer text1;
    private StringBuffer text2;

    private String GsInfoString = "";

    private Handler downloadHandler;

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }

    };
    private int checkYawTimes = 0;
    class CheckYawTask extends TimerTask {

        @Override
        public void run()
        {
            if(checkYawTimes >= 12){
                if(mCheckYawTimer != null){
                    Log.d(TAG ,"==========>mCheckYawTimer cancel!");
                    mCheckYawTimer.cancel();
                    mCheckYawTimer.purge();
                    mCheckYawTimer = null;

                    new Thread()
                    {
                        public void run()
                        {

                            DJIDrone.getDjiGroundStation().setAircraftYawSpeed(0, new DJIGroundStationExecuteCallBack(){

                                @Override
                                public void onResult(GroundStationResult result) {
                                    // TODO Auto-generated method stub

                                }

                            });
                        }
                    }.start();
                }
                return;
            }

            checkYawTimes++;
            Log.d(TAG ,"==========>mCheckYawTimer checkYawTimes="+checkYawTimes);

            new Thread()
            {
                public void run()
                {

//                    DJIDrone.getDjiGroundStation().setAircraftYawSpeed(300, new DJIGroundStationExecuteCallBack(){
//
//                        @Override
//                        public void onResult(GroundStationResult result) {
//                            // TODO Auto-generated method stub
//
//                        }
//
//                    });

                    DJIDrone.getDjiCamera().startTakePhoto(new DJIExecuteResultCallback(){

                        @Override
                        public void onResult(DJIError mErr)
                        {
                            // TODO Auto-generated method stub

                            Log.d(TAG, "Start Takephoto errorCode = "+ mErr.errorCode);
                            Log.d(TAG, "Start Takephoto errorDescription = "+ mErr.errorDescription);
                            String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));

                        }

                    });
                }
            }.start();


        }

    };


    private void checkConnectState(){

        GsProtocolDemoActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (DJIDrone.getDjiCamera() != null) {
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if (bConnectState) {
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                    } else {
                        mConnectStateTextView.setText(R.string.camera_connection_break);
                    }
                }
            }
        });

    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void setResultToToast(String result){
        Toast.makeText(GsProtocolDemoActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    private boolean checkGetHomePoint(){
        if(!getHomePointFlag){
            setResultToToast(getString(R.string.gs_not_get_home_point));
        }
        return getHomePointFlag;
    }

    public static Context GetContext()
    {
        return  m_context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gs_protocol_demo);

        text1 = new StringBuffer();
        text2 = new StringBuffer();

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_gs);
        mOpenGsButton = (Button)findViewById(R.id.OpenGsButton);
        mAddOneWaypointButton = (Button)findViewById(R.id.AddWaypointButton);
        mRemoveWaypointButton = (Button)findViewById(R.id.RemoveWaypointButton);
        mSetLoop = (Button)findViewById(R.id.GsSetLoop);
        mUploadWaypointButton = (Button)findViewById(R.id.UploadWaypointButton);
        mTakeOffButton = (Button)findViewById(R.id.TakeOffButton);
        mGohomeButton = (Button)findViewById(R.id.GohomeButton);
        mCloseGsButton = (Button)findViewById(R.id.CloseGsButton);
        mPauseButton = (Button)findViewById(R.id.PauseButton);
        mResumeButton = (Button)findViewById(R.id.ResumeButton);
        mYawLeftBtn = (Button)findViewById(R.id.GsYawLeftButton);
        mYawRightBtn = (Button)findViewById(R.id.GsYawRightButton);
        mYawStopBtn = (Button)findViewById(R.id.GsYawStopButton);
        mYawRotateTakePhotoBtn = (Button)findViewById(R.id.GsYawRotateTakePhotoButton);
        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateGsTextView);

        mPitchPlusBtn = (Button)findViewById(R.id.GsPitchPlusButton);
        mPitchMinusBtn = (Button)findViewById(R.id.GsPitchMinusButton);
        mPitchStopBtn = (Button)findViewById(R.id.GsPitchStopButton);
        mRollPlusBtn = (Button)findViewById(R.id.GsRollPlusButton);
        mRollMinusBtn = (Button)findViewById(R.id.GsRollMinusButton);
        mRollStopBtn = (Button)findViewById(R.id.GsRollStopButton);
        mThottlePlusBtn = (Button)findViewById(R.id.GsThrottlePlusButton);
        mThottleMinusBtn = (Button)findViewById(R.id.GsThrottleMinusButton);
        mThottleStopBtn = (Button)findViewById(R.id.GsThrottleStopButton);
        mDownloadResultBtn = (Button)findViewById(R.id.GroundStationDownloadResult);
        mFacility_Onekeyfly = (Button)findViewById(R.id.Facility_OnekeyFly);
        mFacility_LoadPhotos = (Button)findViewById(R.id.Facility_LoadPhotos);

        mFacility_Onekeyfly.setOnClickListener(this);
        mFacility_LoadPhotos.setOnClickListener(this);
        mOpenGsButton.setOnClickListener(this);
        mAddOneWaypointButton.setOnClickListener(this);
        mRemoveWaypointButton.setOnClickListener(this);
        mSetLoop.setOnClickListener(this);
        mUploadWaypointButton.setOnClickListener(this);
        mTakeOffButton.setOnClickListener(this);
        mGohomeButton.setOnClickListener(this);
        mCloseGsButton.setOnClickListener(this);
        mYawLeftBtn.setOnClickListener(this);
        mYawRightBtn.setOnClickListener(this);
        mYawStopBtn.setOnClickListener(this);
        mYawRotateTakePhotoBtn.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mResumeButton.setOnClickListener(this);

        mPitchPlusBtn.setOnClickListener(this);
        mPitchMinusBtn.setOnClickListener(this);
        mPitchStopBtn.setOnClickListener(this);
        mRollPlusBtn.setOnClickListener(this);
        mRollMinusBtn.setOnClickListener(this);
        mRollStopBtn.setOnClickListener(this);
        mThottlePlusBtn.setOnClickListener(this);
        mThottleMinusBtn.setOnClickListener(this);
        mThottleStopBtn.setOnClickListener(this);
        mDownloadResultBtn.setOnClickListener(this);

        mGroundStationTextView = (TextView)findViewById(R.id.GroundStationInfoTV);
        mGroundStationInfoScrollView = (ScrollView)findViewById(R.id.GroundStationInfoScrollView);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        mMcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack(){

            @Override
            public void onResult(DJIMainControllerSystemState state) {
                // TODO Auto-generated method stub
                homeLocationLatitude = state.droneLocationLatitude;
                homeLocationLongitude = state.droneLocationLongitude;

                if(homeLocationLatitude != -1 && homeLocationLongitude != -1 && homeLocationLatitude != 0 && homeLocationLongitude != 0){
                    getHomePointFlag = true;
                }
                else{
                    getHomePointFlag = false;
                }
            }

        };

        //file callback
        mCameraFileNameInfoCallBack = new DJICameraFileNameInfoCallBack() {
            @Override
            public void onResult(DJICameraFileNamePushInfo djiCameraFileNamePushInfo)
            {
                Log.v("Take photo",djiCameraFileNamePushInfo.fileName);
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, djiCameraFileNamePushInfo.fileName+"\n"+djiCameraFileNamePushInfo.filePath));
            }
        };

        DJIDrone.getDjiCamera().setDjiCameraFileNameInfoCallBack(mCameraFileNameInfoCallBack);
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);


        mGroundStationFlyingInfoCallBack = new DJIGroundStationFlyingInfoCallBack(){

            @Override
            public void onResult(DJIGroundStationFlyingInfo flyingInfo) {
                // TODO Auto-generated method stub
                //Log.e(TAG, "DJIGroundStationFlyingInfo homeLocationLatitude " +flyingInfo.homeLocationLatitude);
                //Log.e(TAG, "DJIGroundStationFlyingInfo homeLocationLongitude " +flyingInfo.homeLocationLongitude);

            }

        };

        DJIDrone.getDjiGroundStation().setGroundStationFlyingInfoCallBack(mGroundStationFlyingInfoCallBack);

        mGroundStationMissionPushInfoCallBack = new DJIGroundStationMissionPushInfoCallBack() {

            @Override
            public void onResult(DJIGroundStationMissionPushInfo info) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                switch(info.missionType.value()) {
                    case NAVI_MODE_WAYPOINT : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Target Index : " + info.targetWayPointIndex).append("\n");
                        sb.append("Mission Current Status : " + GroundStationWayPointExecutionState.find(info.currState).toString()).append("\n");
                        sb.append("Mission : " + info.targetWayPointIndex).append("\n");
                        break;
                    }

                    case NAVI_MODE_ATTITUDE : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Reserve : " + info.reserved).append("\n");
                        break;
                    }

                    default :
                        sb.append("Worng Selection").append("\n");
                }

                GsInfoString = sb.toString();

                GsProtocolDemoActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mGroundStationTextView.setText(GsInfoString);
                    }

                });
            }

        };

        DJIDrone.getDjiGroundStation().setGroundStationMissionPushInfoCallBack(mGroundStationMissionPushInfoCallBack);

        mGroundStationExecutionPushInfoCallBack = new DJIGroundStationExecutionPushInfoCallBack() {

            @Override
            public void onResult(DJIGroundStationExecutionPushInfo info) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                switch(info.eventType.value()) {
                    case EXECUTION_STATUS_UPLOAD_FINISH : {
                        sb.append("Execution Type : " + info.eventType.toString()).append("\n");
                        sb.append("Validation : " + (info.isMissionValid ? "true" : "false")).append("\n");
                        sb.append("Estimated run time : " + info.estimateRunTime).append("\n");
                        break;
                    }

                    case EXECUTION_STATUS_FINISH : {
                        sb.append("Execution Type : " + info.eventType.toString()).append("\n");
                        sb.append("Repeat : " + Integer.toString(info.isRepeat)).append("\n");
                        sb.append("Reserve: " + GroundStationWayPointExecutionState.find(info.reserved).toString()).append("\n");
                        break;
                    }

                    case EXECUTION_STATUS_REACH_POINT : {
                        sb.append("Execution Type : " + info.eventType.toString()).append("\n");
                        sb.append("WayPoint index : " + info.wayPointIndex).append("\n");
                        sb.append("Current State : " + GroundStationWayPointExecutionState.find(info.currentState).toString()).append("\n");
                        sb.append("Reserve : " + info.reserved).append("\n");
                        break;
                    }
                }

                GsInfoString = sb.toString();

                GsProtocolDemoActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mGroundStationTextView.setText(GsInfoString);
                    }

                });
            }

        };

        DJIDrone.getDjiGroundStation().setGroundStationExecutionPushInfoCallBack(mGroundStationExecutionPushInfoCallBack);

        mTask = new DJIGroundStationTask();

        m_context = this.getApplicationContext();

        if(DJIDrone.getDroneType() == DJIDroneType.DJIDrone_Vision){
            mGroundStationInfoScrollView.setVisibility(View.INVISIBLE);
            mDownloadResultBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        DJIDrone.getDjiMC().startUpdateTimer(1000);
        DJIDrone.getDjiGroundStation().startUpdateTimer(1000);

        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        DJIDrone.getDjiMC().stopUpdateTimer();
        DJIDrone.getDjiGroundStation().stopUpdateTimer();

        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub


        if(DJIDrone.getDjiCamera() != null)
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        super.onDestroy();
    }


    private boolean OnkeyFly()
    {
        if(!checkGetHomePoint())
            return false;
        Log.v("OneKeyfly", "Open GroundStation");

        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {

            @Override
            public void onResult(GroundStationResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.toString();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                if (GroundStationResult.GS_Result_Successed == result) {

                    if (!AddWaypoints())
                        return;

                    UploadWaypoints();


                }
            }

        });
        return true;
    }

    private boolean LoadPhotos()
    {

        //test
//       String strPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//       File sdCardDir = new File(strPath+"/Facility_photos") ;//??SDCard??

//       if (!sdCardDir.exists()){
//           sdCardDir.mkdirs();
//       }

//       File sdFile = new File(sdCardDir,"test.txt");

        Context context= this;
        DJIReceivedFileDataCallBack callBack = new DJIReceivedFileDataCallBack() {
            @Override
            public void onResult(byte[] bytes, int i, int i1, DJIError djiError) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    String strPath = Environment.getExternalStorageDirectory().getAbsolutePath();//??SDCard??
                    File sdCardDir = new File(strPath + "/Facility_photos");

                    if (!sdCardDir.exists()) {
                        sdCardDir.mkdirs();
                    }
                    File sdFile = new File(sdCardDir, "picture.JPG");
                    Toast.makeText(GsProtocolDemoActivity.this,"File has been created successfully",Toast.LENGTH_SHORT).show();

                    try {
                        FileOutputStream fos = new FileOutputStream(sdFile);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(bytes);// ??
                        fos.close(); // ?????
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //Toast.makeText(WebviewTencentActivity.this, "?????sd?", Toast.LENGTH_LONG).show();

                }
            }
        };

        SyncFileService.getInstance().executeSyncByDate(callBack,this);
        Toast.makeText(GsProtocolDemoActivity.this, "executeSyncByDate successfully", Toast.LENGTH_SHORT).show();
        return true;

    }

    private boolean AddWaypoints()
    {
        if(!checkGetHomePoint())
            return false;

        Log.v("OneKeyfly", "Add Waypoints");

        //Point_1
        DJIGroundStationWaypoint mWayPoint1 = new DJIGroundStationWaypoint(31.222092, 121.526354, 4, 1);
        mWayPoint1.altitude = 37.8647f;
        mWayPoint1.speed = 1; // slow 2
        mWayPoint1.heading = 0;
        mWayPoint1.maxReachTime = 999;
        mWayPoint1.stayTime = 999;
        mWayPoint1.hasAction = true;
        mWayPoint1.turnMode = 1;

        mWayPoint1.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, 158);
        mWayPoint1.addAction(GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, -45);
        mWayPoint1.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint1.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint1);

        //Point_2
        DJIGroundStationWaypoint mWayPoint2 = new DJIGroundStationWaypoint(31.221966, 121.526413, 4, 1);
        mWayPoint2.altitude = 42.7f;
        mWayPoint2.speed = 1; // slow 2
        mWayPoint2.heading = 0;
        mWayPoint2.maxReachTime = 999;
        mWayPoint2.stayTime = 999;
        mWayPoint2.turnMode = 1;
        mWayPoint2.hasAction = true;

        mWayPoint2.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -11);
        mWayPoint2.addAction(GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, -90);
        mWayPoint2.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint2.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint2);

        //Point_3
        DJIGroundStationWaypoint mWayPoint3 = new DJIGroundStationWaypoint(31.221844, 121.526299, 3, 1);
        mWayPoint3.altitude = 36.1f;
        mWayPoint3.speed = 1; // slow 2
        mWayPoint3.heading = 0;
        mWayPoint3.maxReachTime = 999;
        mWayPoint3.stayTime = 999;
        mWayPoint3.turnMode = 1;
        mWayPoint3.hasAction = true;

        mWayPoint3.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -133);
        mWayPoint3.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint3.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint3);

        //Point_4
        DJIGroundStationWaypoint mWayPoint4 = new DJIGroundStationWaypoint(31.221869, 121.526489, 3, 1);
        mWayPoint4.altitude = 42.6f;
        mWayPoint4.speed = 1; // slow 2
        mWayPoint4.heading = 0;
        mWayPoint4.maxReachTime = 999;
        mWayPoint4.stayTime = 999;
        mWayPoint4.turnMode = 1;
        mWayPoint4.hasAction = true;
        mWayPoint4.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -109);
        mWayPoint4.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint4.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint4);

        //Point_5
        DJIGroundStationWaypoint mWayPoint5 = new DJIGroundStationWaypoint(31.221905, 121.526665, 3, 1);
        mWayPoint5.altitude = 54f;
        mWayPoint5.speed = 1; // slow 2
        mWayPoint5.heading = 0;
        mWayPoint5.maxReachTime = 999;
        mWayPoint5.stayTime = 999;
        mWayPoint5.turnMode = 1;
        mWayPoint5.hasAction = true;
        mWayPoint5.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -13);
        mWayPoint5.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint5.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint5);

        //Point_6
        DJIGroundStationWaypoint mWayPoint6 = new DJIGroundStationWaypoint(31.221965, 121.5267274, 3, 1);
        mWayPoint6.altitude = 50.3f;
        mWayPoint6.speed = 1; // slow 2
        mWayPoint6.heading = 0;
        mWayPoint6.maxReachTime = 999;
        mWayPoint6.stayTime = 999;
        mWayPoint6.turnMode = 1;
        mWayPoint6.hasAction = true;
        mWayPoint6.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, 77);
        mWayPoint6.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint6.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint6);

        //Point_7
        DJIGroundStationWaypoint mWayPoint7 = new DJIGroundStationWaypoint(31.221991, 121.527164, 3, 1);
        mWayPoint7.altitude = 60.7f;
        mWayPoint7.speed = 1; // slow 2
        mWayPoint7.heading = 0;
        mWayPoint7.maxReachTime = 999;
        mWayPoint7.stayTime = 999;
        mWayPoint7.turnMode = 1;
        mWayPoint7.hasAction = true;
        mWayPoint7.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -13);
        mWayPoint7.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint7.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint7);

        //Point_8
        DJIGroundStationWaypoint mWayPoint8 = new DJIGroundStationWaypoint(31.222160, 121.527433, 3, 1);
        mWayPoint8.altitude = 41.4f;
        mWayPoint8.speed = 1; // slow 2
        mWayPoint8.heading = 0;
        mWayPoint8.maxReachTime = 999;
        mWayPoint8.stayTime = 999;
        mWayPoint8.turnMode = 1;
        mWayPoint8.hasAction = true;
        mWayPoint8.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -108);
        mWayPoint8.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint8.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint8);

        //Point_9
        DJIGroundStationWaypoint mWayPoint9 = new DJIGroundStationWaypoint(31.222183, 121.527526, 3, 1);
        mWayPoint9.altitude = 41.5f;
        mWayPoint9.speed = 1; // slow 2
        mWayPoint9.heading = 0;
        mWayPoint9.maxReachTime = 999;
        mWayPoint9.stayTime = 999;
        mWayPoint9.turnMode = 1;
        mWayPoint9.hasAction = true;
        mWayPoint9.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -107);
        mWayPoint9.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint9.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint9);

        //Point_10
        DJIGroundStationWaypoint mWayPoint10 = new DJIGroundStationWaypoint(31.2221099, 121.5275652, 3, 1);
        mWayPoint10.altitude = 39.1f;
        mWayPoint10.speed = 1; // slow 2
        mWayPoint10.heading = 0;
        mWayPoint10.maxReachTime = 999;
        mWayPoint10.stayTime = 999;
        mWayPoint10.turnMode = 1;
        mWayPoint10.hasAction = true;
        mWayPoint10.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -104);
        mWayPoint10.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint10.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint10);

        //Point_11
        DJIGroundStationWaypoint mWayPoint11 = new DJIGroundStationWaypoint(31.2221345, 121.5277288, 3, 1);
        mWayPoint11.altitude = 53.5f;
        mWayPoint11.speed = 1; // slow 2
        mWayPoint11.heading = 0;
        mWayPoint11.maxReachTime = 999;
        mWayPoint11.stayTime = 999;
        mWayPoint11.turnMode = 1;
        mWayPoint11.hasAction = true;
        mWayPoint11.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -8);
        mWayPoint11.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint11.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint11);

        //Point_12
        DJIGroundStationWaypoint mWayPoint12 = new DJIGroundStationWaypoint(31.2219236, 121.5278673, 4, 1);
        mWayPoint12.altitude = 36.1f;
        mWayPoint12.speed = 1; // slow 2
        mWayPoint12.heading = 0;
        mWayPoint12.maxReachTime = 999;
        mWayPoint12.stayTime = 999;
        mWayPoint12.turnMode = 1;
        mWayPoint12.hasAction = true;
        mWayPoint12.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -11);
        mWayPoint12.addAction(GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, -45);
        mWayPoint12.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint12.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint12);

        //Point_13
        DJIGroundStationWaypoint mWayPoint13 = new DJIGroundStationWaypoint(31.2221266, 121.5277845, 3, 1);
        mWayPoint13.altitude = 38.8f;
        mWayPoint13.speed = 1; // slow 2
        mWayPoint13.heading = 0;
        mWayPoint13.maxReachTime = 999;
        mWayPoint13.stayTime = 999;
        mWayPoint13.turnMode = 1;
        mWayPoint13.hasAction = true;
        mWayPoint13.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -107);
        mWayPoint13.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint13.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint13);

        //Point_14
        DJIGroundStationWaypoint mWayPoint14 = new DJIGroundStationWaypoint(31.2221906, 121.527781, 4, 1);
        mWayPoint14.altitude = 33.3f;
        mWayPoint14.speed = 1; // slow 2
        mWayPoint14.heading = 0;
        mWayPoint14.maxReachTime = 999;
        mWayPoint14.stayTime = 999;
        mWayPoint14.turnMode = 1;
        mWayPoint14.hasAction = true;
        mWayPoint14.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, 84);
        mWayPoint14.addAction(GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, -90);
        mWayPoint14.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint14.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint14);

        //Point_15
        DJIGroundStationWaypoint mWayPoint15 = new DJIGroundStationWaypoint(31.222210, 121.5279301, 4, 1);
        mWayPoint15.altitude = 35.1f;
        mWayPoint15.speed = 1; // slow 2
        mWayPoint15.heading = 0;
        mWayPoint15.maxReachTime = 999;
        mWayPoint15.stayTime = 999;
        mWayPoint15.turnMode = 1;
        mWayPoint15.hasAction = true;
        mWayPoint15.addAction(GroundStationOnWayPointAction.Way_Point_Action_Craft_Yaw, -109);
        mWayPoint15.addAction(GroundStationOnWayPointAction.Way_Point_Action_Gimbal_Pitch, -45);
        mWayPoint15.addAction(GroundStationOnWayPointAction.Way_Point_Action_Stay, 3);
        mWayPoint15.addAction(GroundStationOnWayPointAction.Way_Point_Action_Simple_Shot, 1);
        mTask.addWaypoint(mWayPoint15);

        //Point_End
        mTask.finishAction = DJIGroundStationFinishAction.Go_Home;
        mTask.movingMode = DJIGroundStationMovingMode.GSHeadingUsingWaypointHeading;
        mTask.pathMode = DJIGroundStationPathMode.Point_To_Point;
        return true;
    }

    private boolean UploadWaypoints()
    {
        if(!checkGetHomePoint())
            return false;
        Log.v("OneKeyfly", "UpLoad?Waypoints");
        DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask, new DJIGroundStationExecuteCallBack(){

            @Override
            public void onResult(GroundStationResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.toString();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                if (GroundStationResult.GS_Result_Successed == result)
                {
                    TakeOff();
                }
            }

        });

        return  true;
    }

    private boolean TakeOff()
    {
        if(!checkGetHomePoint())
            return false;
        Log.v("OneKeyfly", "Take off!");
        DJIDrone.getDjiGroundStation().startGroundStationTask(new DJIGroundStationTakeOffCallBack(){

            @Override
            public void onResult(GroundStationTakeOffResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.toString();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                Log.v("OneKeyfly", "Take off successfully!");
            }
        });
        return true;
    }

    @Override
    public void onClick(View v) {
        List<String> strlist = null;
        int TotalStringCnt = 0;
        String[] mSettingStrs = null;
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.Facility_OnekeyFly:
                if(!checkGetHomePoint()) return;
                OnkeyFly();
                break;
            case R.id.Facility_LoadPhotos:
                //if (checkGetHomePoint()) return;
                LoadPhotos();
                break;
            case R.id.OpenGsButton:
                if(!checkGetHomePoint()) return;
                Log.v("Inspire", "ButtonOnClick");

                DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });

                break;

            case R.id.AddWaypointButton:
                if(!checkGetHomePoint()) return;

                AddWaypoints();

                break;

            case R.id.RemoveWaypointButton:
                if (mPopupNumberPicker != null)
                    mPopupNumberPicker.dismiss();
                strlist = new ArrayList<String>();
                TotalStringCnt = getResources().getStringArray(R.array.photosizeValues).length;
                mSettingStrs = new String[TotalStringCnt];
                mSettingStrs = getResources().getStringArray(R.array.photosizeValues);

                int size = mTask.mWaypointsList.size();
                if (size != 0) {
                    for (int i = 0; i < size; i++){
                        strlist.add(i+"");
                    }
                    mPopupNumberPicker = new PopupNumberPicker(m_context,
                            strlist,
                            new pickerValueChangeListener(){

                                @Override
                                public void onValueChange(int pos1, int pos2) {
                                    //Log.d(TAG,"pos1 = "+ pos1 +", pos2 = "+pos2);
                                    mPopupNumberPicker.dismiss();
                                    mPopupNumberPicker = null;

                                    //Log.d(TAG,"CameraActionWhenBreak.values()[pos1].toString() = "+CameraActionWhenBreak.values()[pos1].toString());

                                    mTask.removeWaypointAtIndex(pos1);

                                    Log.v("setloop", pos1+"");

                                }}, 250,
                            200, 0);
                    mPopupNumberPicker.showAtLocation(findViewById(R.id.my_content_view),
                            Gravity.CENTER, 0, 0);
                } else {
                    String ResultsString = "There is no way point";
                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                }
                break;

            case R.id.GsSetLoop:
                if (mPopupNumberPicker != null)
                    mPopupNumberPicker.dismiss();

                strlist = new ArrayList<String>();
                TotalStringCnt = getResources().getStringArray(R.array.photosizeValues).length;
                mSettingStrs = new String[TotalStringCnt];
                mSettingStrs = getResources().getStringArray(R.array.photosizeValues);

                strlist.add("True");
                strlist.add("False");

                mPopupNumberPicker = new PopupNumberPicker(m_context,
                        strlist,
                        new pickerValueChangeListener(){

                            @Override
                            public void onValueChange(int pos1, int pos2) {
                                //Log.d(TAG,"pos1 = "+ pos1 +", pos2 = "+pos2);
                                mPopupNumberPicker.dismiss();
                                mPopupNumberPicker = null;

                                //Log.d(TAG,"CameraActionWhenBreak.values()[pos1].toString() = "+CameraActionWhenBreak.values()[pos1].toString());
                                if (0 == pos1) {
                                    mTask.setLoop(true);
                                    mTask.repeatNum = 2;
                                } else {
                                    mTask.setLoop(false);
                                    mTask.repeatNum = 0;
                                }

                                Log.v("setloop", pos1+"");

                            }}, 250,
                        200, 0);
                mPopupNumberPicker.showAtLocation(findViewById(R.id.my_content_view),
                        Gravity.CENTER, 0, 0);
                break;
            case R.id.UploadWaypointButton:
                if(!checkGetHomePoint()) return;

                DJIDrone.getDjiGroundStation().uploadGroundStationTask(mTask, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;

            case R.id.TakeOffButton:
                if(!checkGetHomePoint()) return;
                DJIDrone.getDjiGroundStation().startGroundStationTask(new DJIGroundStationTakeOffCallBack(){

                    @Override
                    public void onResult(GroundStationTakeOffResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                });
                break;

            case R.id.GohomeButton:
                if(!checkGetHomePoint()) return;
                DJIDrone.getDjiGroundStation().goHome(new DJIGroundStationGoHomeCallBack(){

                    @Override
                    public void onResult(GroundStationGoHomeResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;

            case R.id.CloseGsButton:
                if(!checkGetHomePoint()) return;

                DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;

            case R.id.PauseButton:
                if(!checkGetHomePoint()) return;
                DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationHoverCallBack(){

                    @Override
                    public void onResult(GroundStationHoverResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                });
                break;

            case R.id.ResumeButton:
                if(!checkGetHomePoint()) return;

                DJIDrone.getDjiGroundStation().continueGroundStationTask(new DJIGroundStationResumeCallBack(){

                    @Override
                    public void onResult(GroundStationResumeResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;

            case R.id.GsYawLeftButton:
                new Thread()
                {
                    public void run()
                    {

                        DJIDrone.getDjiGroundStation().setAircraftYawSpeed(-100, new DJIGroundStationExecuteCallBack(){

                            @Override
                            public void onResult(GroundStationResult result) {
                                // TODO Auto-generated method stub

                            }

                        });
                    }
                }.start();

                break;

            case R.id.GsYawRightButton:
                new Thread()
                {
                    public void run()
                    {

                        DJIDrone.getDjiGroundStation().setAircraftYawSpeed(100, new DJIGroundStationExecuteCallBack(){

                            @Override
                            public void onResult(GroundStationResult result) {
                                // TODO Auto-generated method stub

                            }

                        });
                    }
                }.start();

                break;

            case R.id.GsYawStopButton:
                new Thread()
                {
                    public void run()
                    {

                        DJIDrone.getDjiGroundStation().setAircraftYawSpeed(0, new DJIGroundStationExecuteCallBack(){

                            @Override
                            public void onResult(GroundStationResult result) {
                                // TODO Auto-generated method stub
                                String ResultsString = "return code =" + result.toString();
                                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                            }

                        });
                    }
                }.start();

                break;

            case R.id.GsYawRotateTakePhotoButton:
//                if(mCheckYawTimer != null) return;
//
//                checkYawTimes = 0;
//                mCheckYawTimer = new Timer();
//                CheckYawTask mCheckYawTask = new CheckYawTask();
//                mCheckYawTimer.schedule(mCheckYawTask, 100, 3000);
//
//                new Thread()
//                {
//                    public void run()
//                    {
//
//                        DJIDrone.getDjiGroundStation().setAircraftYawSpeed(100, new DJIGroundStationExecuteCallBack(){
//
//                            @Override
//                            public void onResult(GroundStationResult result) {
//                                // TODO Auto-generated method stub
//
//                            }
//
//                        });
//                    }
//                }.start();

                DJIDrone.getDjiGroundStation().downloadGroundStationTask(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });

                break;

            case R.id.GsPitchPlusButton:
                DJIDrone.getDjiGroundStation().setAircraftPitchSpeed(200, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;

            case R.id.GsPitchMinusButton:
                DJIDrone.getDjiGroundStation().setAircraftPitchSpeed(-200, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;

            case R.id.GsPitchStopButton:
                DJIDrone.getDjiGroundStation().setAircraftPitchSpeed(0, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;

            case R.id.GsRollPlusButton:

                DJIDrone.getDjiGroundStation().setAircraftRollSpeed(200, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });

                break;

            case R.id.GsRollMinusButton:
                DJIDrone.getDjiGroundStation().setAircraftRollSpeed(-200, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;

            case R.id.GsRollStopButton:
                DJIDrone.getDjiGroundStation().setAircraftRollSpeed(0, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;

            case R.id.GsThrottlePlusButton:
                DJIDrone.getDjiGroundStation().setAircraftThrottle(1, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });

                break;

            case R.id.GsThrottleMinusButton:
                DJIDrone.getDjiGroundStation().setAircraftThrottle(2, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });

                break;

            case R.id.GsThrottleStopButton:{
                DJIDrone.getDjiGroundStation().setAircraftThrottle(0, new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub

                    }

                });
                break;
            }

            case R.id.GroundStationDownloadResult : {
                LayoutInflater inflater = LayoutInflater.from(this);
                downloadResultView = inflater.inflate(R.layout.show_view, null);
                builder = new AlertDialog.Builder(GsProtocolDemoActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Download Result");
                builder.setView(downloadResultView);
                DJIDrone.getDjiGroundStation().downloadGroundStationTask(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        if (result == GroundStationResult.GS_Result_Successed) {
                            final DJIGroundStationTask task = DJIDrone.getDjiGroundStation().getDJIGroundStationTask();
                            showTV = (TextView)downloadResultView.findViewById(R.id.ShowTextView);
                            StringBuffer str = new StringBuffer();
                            Log.d(TAG, "taskwaypointcount "+task.wayPointCount);
                            for (int i = 0; i < task.wayPointCount; i++){
                                str.append(i + " latitude : " + task.mWaypointsList.get(i).latitude+"\n");
                                str.append(i + " longitude : " + task.mWaypointsList.get(i).lontitude+"\n");
                                str.append(i + " hasAction : " + (task.mWaypointsList.get(i).hasAction ? "True" : "False") + "\n");
                                str.append(i + " maxReachTime : " + task.mWaypointsList.get(i).maxReachTime + "\n\n");
                            }
                            showTV.setText(str);
                            Looper.prepare();
                            builder.create().show();
                            Looper.loop();
                        }
                    }

                });
                break;
            }

            default:
                break;
        }
    }

    /**
     * @Description : RETURN BTN RESPONSE FUNCTION
     * @author      : andy.zhao
     * @param view
     * @return      : void
     */
    public void onReturn(View view){
        Log.d(TAG ,"onReturn");
        this.finish();
    }

}
