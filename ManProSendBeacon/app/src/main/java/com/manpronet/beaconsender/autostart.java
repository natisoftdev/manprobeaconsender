package com.manpronet.beaconsender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.manpronet.beaconsender.servizio.SendBeaconService;

public class autostart extends BroadcastReceiver
{
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0, SendBeaconService.class);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}