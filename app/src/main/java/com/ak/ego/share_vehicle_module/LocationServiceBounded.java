package com.ak.ego.share_vehicle_module;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Random;

public class LocationServiceBounded extends Service {

    public class LocationServiceBoundedHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Messenger messenger = (Messenger)msg.obj;
            Log.d("BOUNDED","handling message");
            Message message = new Message();
            message.arg1 = 1;
            try
            {
                messenger.send(message);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }

    public class LocationServiceBinder extends Binder{
        LocationServiceBounded getService(){
            return LocationServiceBounded.this;
        }
    }
    private final Messenger messenger = new Messenger(new LocationServiceBoundedHandler());
    private IBinder binder = new LocationServiceBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       // return binder;
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public void  getMyServiceDone()
    {
        Log.d("BOUNDEDSERVICE"," u called me");
    }

}
