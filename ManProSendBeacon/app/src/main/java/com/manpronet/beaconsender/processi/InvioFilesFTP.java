package com.manpronet.beaconsender.processi;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.manpronet.beaconsender.dati.Costanti;
import com.manpronet.beaconsender.servizio.SendBeaconService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class InvioFilesFTP extends Thread {
    //private static final String TAG = "InvioFilesFTP";
    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public InvioFilesFTP() {
        // cancel if already existed
        if(mTimer != null) { mTimer.cancel(); }
        else { mTimer = new Timer(); }
    }

    public void start(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SendBeaconService.sendBeaconService);
        String timeSendSec = preferences.getString("pref_time_send","");// 5 Minuti di base
        int secondi;

        if(timeSendSec != ""){ secondi = Integer.parseInt(timeSendSec) * 60; }
        else { secondi = Costanti.timeSendSec; }

        mTimer.scheduleAtFixedRate(new InvioFilesFTP.TimeDisplayTimerTask(), 30*1000, secondi * 1000);
    }

    class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if(SendBeaconService.sendBeaconService != null){ execute(); }
                }
            });
        }
    }

    private void execute(){
        //Controllo se devo inviare reportBLocchi e reportConnettività via FTP
        SendBeaconService.checkReportistica(getDateNow());
    }

    private String getDateNow(){
        String currentTime;
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        currentTime = sdf.format(dt);
        return  currentTime;
    }
}