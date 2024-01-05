package com.manpronet.beaconsender;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;

import com.manpronet.beaconsender.connection.CheckConnectionToServer;
import com.manpronet.beaconsender.dati.Beacon;
import com.manpronet.beaconsender.servizio.SendBeaconService;

import java.util.List;

public class ImpostazioniActivity extends PreferenceActivity {

    ImpostazioniActivity impostazioniActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_impostazioni);

        impostazioniActivity = this;
        PreferenceManager pm = getPreferenceManager();

        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Preferenze", ""+e);
        }

        Preference editTextPref = pm.findPreference("versionNumberUser");
        assert editTextPref != null;
        editTextPref.setSummary(versionName);

/*
        final EditTextPreference etp0 = (EditTextPreference) pm.findPreference("pref_indirizzoInvio");
        etp0.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MainActivity.mainActivity.recreate();
                return true;
            }
        });
*/
/*
        final EditTextPreference etp1 = (EditTextPreference) pm.findPreference("pref_db_string");
        assert etp1 != null;
        etp1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //MainActivity ma = (MainActivity)getActivity();
                MainActivity.mainActivity.stopMyServiceBeacon();
                MainActivity.mainActivity.startMyServiceBeacon();
                MainActivity.mainActivity.recreate();
                return true;
            }
        });
*/

        final EditTextPreference etp1 = (EditTextPreference) pm.findPreference("pref_stringa_connessione");
        assert etp1 != null;
        etp1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        new CheckConnectionToServer();
                    }
                }, 100);
                //MainActivity ma = (MainActivity)getActivity();
                MainActivity.mainActivity.stopMyServiceBeacon();
                MainActivity.mainActivity.startMyServiceBeacon();
                MainActivity.mainActivity.recreate();
                return true;
            }
        });
/*
        final EditTextPreference etp2 = (EditTextPreference) pm.findPreference("pref_db_login");
        assert etp2 != null;
        etp2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //MainActivity ma = (MainActivity)getActivity();
                MainActivity.mainActivity.stopMyServiceBeacon();
                MainActivity.mainActivity.startMyServiceBeacon();
                MainActivity.mainActivity.recreate();
                return true;
            }
        });

        final EditTextPreference etp3 = (EditTextPreference) pm.findPreference("pref_db_password");
        assert etp3 != null;
        etp3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //MainActivity ma = (MainActivity)getActivity();
                MainActivity.mainActivity.stopMyServiceBeacon();
                MainActivity.mainActivity.startMyServiceBeacon();
                MainActivity.mainActivity.recreate();
                return true;
            }
        });
*/
        final EditTextPreference etp4 = (EditTextPreference) pm.findPreference("pref_time_send");
        assert etp4 != null;
        etp4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //MainActivity ma = (MainActivity)getActivity();
                MainActivity.mainActivity.stopMyServiceBeacon();
                MainActivity.mainActivity.startMyServiceBeacon();
                MainActivity.mainActivity.recreate();
                return true;
            }
        });

        Preference deleteDB = pm.findPreference("deleteDB");
        assert deleteDB != null;
        deleteDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
                //String Login = preferences.getString("pref_db_login", "");
                //String Password = preferences.getString("pref_db_password", "");
                //int ruolo = preferences.getInt("ruolo",0);
/*
                List<Beacon> list = SendBeaconService.databaseHelperBeacon.getBeacon();
                int size = list.size();
                Log.i("Impostazioni", "Numero record in DB: "+size);
*/
/*
                //Controllo i permessi
                if(
                        //(Login.equals("monand") && Password.equals("paolamen")) ||
                        //        (Login.equals("rossi") && Password.equals("1"))

                        ruolo == 1
                ){
*/
                    new AlertDialog.Builder(impostazioniActivity)
                        .setTitle("Database").setMessage("Sei sicuro di voler ripulire il Database")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SendBeaconService.databaseHelperBeacon.deleteAllDB();
                                    SendBeaconService.databaseHelper.deleteAllDB();

                                    SendBeaconService.databaseHelperBeacon.close();
                                    SendBeaconService.databaseHelper.close();
/*
                                    List<Beacon> list2 = SendBeaconService.databaseHelperBeacon.getBeacon();
                                    int size2 = list2.size();
                                    Log.i("Impostazioni", "Numero record in DB: "+size2);
*/
                                    MainActivity.mainActivity.stopMyServiceBeacon();
                                    MainActivity.mainActivity.startMyServiceBeacon();
                                    MainActivity.mainActivity.recreate();
                                }
                            })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_delete_db)
                        .create()
                        .show();
                /*}
                else{
                    //Messaggio: Non sei autorizzato
                    new AlertDialog.Builder(impostazioniActivity)
                            .setTitle("Database").setMessage("Non hai i permessi per svuotare il Database")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(R.drawable.ic_delete_db)
                            .create()
                            .show();
                }*/
                return true;
            }
        });

        Preference updateApp = pm.findPreference("updateApp");
        assert updateApp != null;
        updateApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("https://play.google.com/store/apps/details?id=com.manpronet.beaconsender"));
                startActivity(intent);
                return true;
            }
        });

        Preference sendData = pm.findPreference("sendData");
        assert sendData != null;
        sendData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MainActivity.mainActivity.sendFilesAndDB();
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
