package com.ak.ego.share_vehicle_module;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.time.chrono.MinguoEra;

public class LocationServiceHandler extends Handler {

    public LocationServiceHandler(Looper looper)
    {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.d("HANDLER",""+msg.arg1);
        Bundle data_received =msg.getData();
        Intent received_intent = (Intent)msg.obj;
        Messenger messenger = (Messenger)received_intent.getExtras().get("massenger");
        Message message = new Message();
        message.obj = "i am sending";
        message.setData(data_received);
        try {
            messenger.send(message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
