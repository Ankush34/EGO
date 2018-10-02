package com.ak.ego.bookShareCarRideModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class bookingShareCabServiceBroadcaseReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("HandlerBroadCastReceive"," Broadcasted");
    }
}
