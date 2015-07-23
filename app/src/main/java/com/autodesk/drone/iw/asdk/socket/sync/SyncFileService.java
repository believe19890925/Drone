package com.autodesk.drone.iw.asdk.socket.sync;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.autodesk.drone.iw.asdk.socket.SocketLogger;
import com.autodesk.drone.iw.asdk.socket.download.IDownloadCalback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMode;
import dji.sdk.api.Camera.DJIPhantom3AdvancedCamera;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.media.DJIMedia;
import dji.sdk.api.media.DJIMediaFile;
import dji.sdk.api.media.DJIP3AMediaDirInfo;
import dji.sdk.api.media.DJIP3AMediaInfo;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIMediaFetchCallBack;
import dji.sdk.interfaces.DJIP3ADownloadListener;
import dji.sdk.interfaces.DJIReceivedFileDataCallBack;

public class SyncFileService {
    private static SyncFileService instance = new SyncFileService();
    public static final List<DJIMedia> M_DJI_MEDIA_LIST = new ArrayList<DJIMedia>();

    private SyncFileService() {

    }

    public static SyncFileService getInstance() {
        return instance;
    }

    public void listFile(final ISyncFileCallback callback, final String command) {
        DJIDrone.getDjiCamera().startUpdateTimer(1000);
        DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack() {
            @Override
            public void onResult(List<DJIMedia> mList, DJIError mError) {

                JSONObject response = new JSONObject();
                try {
                    if (command != null) {
                        response.put("cmd", command);
                    }
                    if (mError.errorCode == DJIError.RESULT_OK) {
                        response.put("status", "OK");

                        List<DJIMedia> mDJIMediaList = new ArrayList<DJIMedia>();
                        mDJIMediaList.addAll(mList);

                        JSONArray array = new JSONArray();
                        for (int i = 0; i < mDJIMediaList.size(); i++) {
                            DJIMedia media = mDJIMediaList.get(i);
                            JSONObject mediaObj = new JSONObject();
                            mediaObj.put("createTime", media.createTime);
                            mediaObj.put("fileSize", media.fileSize);
                            mediaObj.put("fileName", media.fileName);
                            mediaObj.put("mediaType", media.mediaType.name());
                            mediaObj.put("mediaURL", media.mediaURL);
                            array.put(mediaObj);
                        }
                        response.put("fileList", array);
                    } else {
                        response.put("status", "NG");
                        response.put("message", "fetchMediaList failed,errorCode = " + mError.errorCode);
                    }
                } catch (Exception ex) {
                    SocketLogger.log(SocketLogger.LogPriority.error, SyncFileService.class, "DJI fetchMediaList exception", ex);
                }

                resetCameraStatus();

                if (callback != null) {
                    callback.notifyResult(response.toString());
                }
            }
        });
    }

    /**
     * Write file to desktop as byte stream
     *
     * @param callback
     * @param fileName
     */
    public void executeSync(final String fileName, final IDownloadCalback callback) {
        DJIDrone.getDjiCamera().startUpdateTimer(1000);
        DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack() {
            private DJIMedia mMedia = null;

            @Override
            public void onResult(List<DJIMedia> mList, DJIError mError) {

                //Toast.makeText(,"",Toast.LENGTH_SHORT).show();
                if (mError.errorCode == DJIError.RESULT_OK) {
                    List<DJIMedia> mDJIMediaList = new ArrayList<DJIMedia>();
                    mDJIMediaList.addAll(mList);

                    for (DJIMedia media : mDJIMediaList) {
                        if (fileName.equalsIgnoreCase("LATEST")) {
                            // return latest file
                            mMedia = media;
                            break;
                        } else if (fileName.equalsIgnoreCase(media.fileName)) {
                            mMedia = media;
                            break;
                        }
                    }

                    if (mMedia == null) {
                        resetCameraStatus();
                        callback.error("Can not find file " + fileName + ", please call listFile to refresh!");
                    } else {
                        DJIDrone.getDjiCamera().fetchMediaData(mMedia, new DJIReceivedFileDataCallBack() {
                            private boolean hasNotifiedFile = false;

                            @Override
                            public void onResult(byte[] buffer, int size, int progress, DJIError mErr) {
                                if (mErr.errorCode == DJIError.RESULT_OK) {
                                    SocketLogger.log(SocketLogger.LogPriority.debug, SyncFileService.class, ">> onDataUpdate size > " + size + ", progress = " + progress);
                                    if (!hasNotifiedFile) {
                                        callback.notifyFileInfo(mMedia.fileName, mMedia.fileSize, mMedia.createTime);
                                        hasNotifiedFile = true;
                                    }
                                    callback.notifyData(buffer, size);
                                    if (progress == 100) {
                                        resetCameraStatus();
                                        callback.finished();
                                    }
                                } else {
                                    resetCameraStatus();
                                    callback.error("DJIReceivedFileDataCallBack failed!");
                                }
                            }
                        });
                    }
                } else {
                    resetCameraStatus();
                    callback.error("List file error : " + mError.errorDescription);
                }
            }
        });
    }

    public void executeSyncByDate(final DJIReceivedFileDataCallBack callBack, final Activity activity)
    {
        Log.v(SyncFileService.this.toString(),"FetchMediaList begin");
        DJIDrone.getDjiCamera().startUpdateTimer(1000);

        ((DJIPhantom3AdvancedCamera)DJIDrone.getDjiCamera()).fetchMediaList(new DJIP3ADownloadListener<DJIP3AMediaDirInfo>() {

            @Override
            public void onFailure(DJIError mError)
            {
                // TODO Auto-generated method stub
                //handler.sendMessage(handler.obtainMessage(SHOWTOAST, DJIError.getErrorDescriptionByErrcode(mError.errorCode)));
            }

            @Override
            public void onProgress(long arg0, long arg1)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRateUpdate(long arg0, long arg1, long arg2)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStart()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(DJIP3AMediaDirInfo info)
            {
                /*
                handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                // TODO Auto-generated method stub
                if(mDJIMediaInfoList != null){
                    mDJIMediaInfoList.clear();
                }
                mDJIMediaDirInfo = info;
                mDJIMediaInfoList.addAll(mDJIMediaDirInfo.fileInfoList);
//                            DJILogHelper.getInstance().LOGD("P3A", ""+mDJIMediaInfoList.size(),true,true);
                handler.sendMessage(handler.obtainMessage(NEED_REFRESH_FILE_LIST, null));
                */
                List<DJIP3AMediaInfo> mDJIMediaInfoList = null;
                DJIP3AMediaDirInfo mDJIMediaDirInfo = null;

                if(mDJIMediaInfoList != null){
                    mDJIMediaInfoList.clear();
                }
                mDJIMediaDirInfo = info;
                mDJIMediaInfoList.addAll(mDJIMediaDirInfo.fileInfoList);

                ((DJIPhantom3AdvancedCamera) DJIDrone.getDjiCamera()).fetchMediaThumbnail(mDJIMediaInfoList.get(0), new DJIP3ADownloadListener<DJIMediaFile>() {

                    @Override
                    public void onFailure(DJIError arg0) {
                        // TODO Auto-generated method stub
                        //handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                        //currentProgress = -1;
                    }

                    @Override
                    public void onProgress(long total, long current) {
                        //   // TODO Auto-generated method stub
                        //   int tmpProgress = (int)(1.0*current/total) * 100;
                        //   if (tmpProgress != currentProgress) {
                        //       mDownloadDialog.setProgress(tmpProgress);
                        //       currentProgress = tmpProgress;
                        //   }
                    }

                    @Override
                    public void onRateUpdate(long arg0, long arg1, long arg2) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onStart() {
                        // TODO Auto-generated method stub
                        //   handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
                        //   currentProgress = -1;
                    }

                    @Override
                    public void onSuccess(DJIMediaFile data) {
                        // TODO Auto-generated method stub
                        //    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                        //    handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.sync_success)));
                        //    currentProgress = -1;
                        String strPath = Environment.getExternalStorageDirectory().getAbsolutePath();//??SDCard??
                        File sdCardDir = new File(strPath + "/Facility_photos");

                        if (!sdCardDir.exists()) {
                            sdCardDir.mkdirs();
                        }
                        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                        int Quality = 100;
                        OutputStream stream = null;
                        String strFileName = sdCardDir +"/picture.JPG";
                        try {
                            stream = new FileOutputStream(strFileName);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        data.bitmap.compress(format, Quality, stream);
                    }

                });

            }

        });

        /*
        DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack() {
            private DJIMedia mMedia = null;

            @Override
            public void onResult(List<DJIMedia> mList, DJIError mError) {
                //Toast.makeText(activity,"FetchMediaList Onresult",Toast.LENGTH_SHORT);
                //Toast.makeText(activity, "FetchMediaList Onresult", Toast.LENGTH_SHORT);

                String strError = "FetchMediaList Onresult = "+ mError.errorCode;
                //Log.v(SyncFileService.this.toString(),strError);
                SocketLogger.log(SocketLogger.LogPriority.info, DJIReceivedFileDataCallBack.class, strError);
                mMedia = mList.get(1);
                DJIDrone.getDjiCamera().fetchMediaData(mMedia, callBack);

            //   if (mError.errorCode == DJIError.RESULT_OK){
            //       mMedia = mList.get(1);

            //       if (mMedia == null) {
            //           resetCameraStatus();

            //       }
            //       else
            //       {
            //           DJIDrone.getDjiCamera().fetchMediaData(mMedia,callBack);
            //       }
            //   }else{
            //       resetCameraStatus();
            //       //callback.error("List file error : " + mError.errorDescription);
            //   }
            }
        });
        */
    }

    private void resetCameraStatus() {
        DJIDrone.getDjiCamera().stopUpdateTimer();
        // Reset Camera status
        DJIDrone.getDjiCamera().setCameraMode(CameraMode.Camera_Camera_Mode, new DJIExecuteResultCallback() {
            @Override
            public void onResult(DJIError mErr) {
            }
        });
    }
}
