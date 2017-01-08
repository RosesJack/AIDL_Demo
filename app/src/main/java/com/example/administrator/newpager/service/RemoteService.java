package com.example.administrator.newpager.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * auther：wzy
 * date：2017/1/8 19 :52
 * desc:
 */

public class RemoteService extends Service {
    private static final String TAG = "RemoteService";
    private RemoteCallbackList<IMyAidlCallBack> mRemoteCallbackList = new RemoteCallbackList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }

    public void doInBackGround() {
        new Thread() {
            @Override
            public void run() {
                long result = this.getId();
                int count = 0;
                Log.i(TAG, "mRemoteCallbackList: " + mRemoteCallbackList+",mRemoteCallbackList.mCallBack:"+mRemoteCallbackList);
                count = mRemoteCallbackList.beginBroadcast();
                if (count == 0) {
                    return;
                }
                try {
                    for (int i = 0; i < count; i++) {
                        mRemoteCallbackList.getBroadcastItem(i).callBack((int) result);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    mRemoteCallbackList.finishBroadcast();
                }
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    class LocalBinder extends IRemoteService.Stub {

        @Override
        public void refreshFromNet() throws RemoteException {

        }

        @Override
        public void registerListener(IMyAidlCallBack callBack) throws RemoteException {
            Log.i(TAG, "callBack:" + callBack);
            Log.i(TAG, "mRemoteCallbackList:" + mRemoteCallbackList);
            if (mRemoteCallbackList == null) {
                Toast.makeText(getApplicationContext(), "mRemoteCallbackList==null", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "mRemoteCallbackList==null");
                return;
            }
            mRemoteCallbackList.register(callBack);
        }

        @Override
        public void unRegisterListener(IMyAidlCallBack callBack) throws RemoteException {
            mRemoteCallbackList.unregister(callBack);
        }

        @Override
        public void doInBackGround() throws RemoteException {
            RemoteService.this.doInBackGround();
        }
    }

}
