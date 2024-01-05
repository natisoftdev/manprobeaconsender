package com.manpronet.beaconsender.volley;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.manpronet.beaconsender.R;
import com.manpronet.beaconsender.dati.Beacon;
import com.manpronet.beaconsender.dati.Costanti;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VolleyBeacon {
/*
    private RequestQueue reQueue;
    private static final String TAG = "CopyInDatabase";
*/
    public VolleyBeacon(final Context context, final Beacon beacon)/*(final Beacon oneBeacon */{
/*
        String url = Costanti.getIndirizzoPortale(context);
        String addsComplete;

        addsComplete = Costanti.checkPortalAddress(url,context) + Costanti.ricezione_beacon_utilizzatori;

        reQueue = Volley.newRequestQueue(context);
        StringRequest request=new StringRequest(com.android.volley.Request.Method.POST,addsComplete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, ""+response);
                        JSONObject json;
                        try {
                            json = new JSONObject(response);
                            int val  = json.getInt("result");
                            Log.d(TAG, "valore json bottone copia MAC beacon: "+val);
                            String msg;
                            if(val==1) msg = "OK";
                            else msg = String.valueOf(R.string.errore);
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) { Log.e(TAG, ""+e); }
                    }
                },
                new Response.ErrorListener() { @Override public void onErrorResponse(VolleyError error) { Log.e(TAG, ""+error); } })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=ansi");

                Date dt = new Date();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String currentTime = sdf.format(dt);
                params.put("DataInvio", ""+  currentTime);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String Login = preferences.getString("pref_db_login", "");
                String Password = preferences.getString("pref_db_password", "");
                String db_connect = preferences.getString("pref_db_string", "");

                params.put("Login", ""+Login);
                params.put("Password", ""+Password);
                params.put("db_connect", ""+db_connect);

                params.put("Model", ""+beacon.getModel());
                params.put("dataCamp", ""+beacon.getDatacampionamento());
                params.put("Android_id", ""+beacon.getAndroid_id());
                params.put("NumeroVersioneApp", ""+beacon.getNumeroversione());
                params.put("LivelloBatteria", ""+beacon.getLivelloBatteria());
                params.put("TempoCampionamento", ""+beacon.getTempocampionamento());
                params.put("Mac", ""+beacon.getMac());
                params.put("Distance", ""+beacon.getDistance());
                params.put("TxPower", ""+beacon.getTxpower());
                params.put("Rssi", ""+beacon.getRssi());

                return params;
            }
        };

        try{ reQueue.add(request); }
        catch(Exception e){ Log.e(TAG, ""+e); }

 */
    }
}
