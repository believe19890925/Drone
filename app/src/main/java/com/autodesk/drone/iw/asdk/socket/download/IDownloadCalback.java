package com.autodesk.drone.iw.asdk.socket.download;

public interface IDownloadCalback {
    public void notifyData(byte[] data, int size);

    public void finished();

    public void error(String message);

    public void notifyFileInfo(String fileName, long fileSize, String createTime);
}
