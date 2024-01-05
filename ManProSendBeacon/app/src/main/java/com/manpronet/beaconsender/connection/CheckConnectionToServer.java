package com.manpronet.beaconsender.connection;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.manpronet.beaconsender.MainActivity;
import com.manpronet.beaconsender.R;
import com.manpronet.beaconsender.dati.Costanti;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckConnectionToServer {

    private static final String TAG = "CheckConnectionToServer";

    SharedPreferences preferences;

    public CheckConnectionToServer() {

        //dbHelper = new DatabaseHelperBeacon(MonitoringActivity.monitoringActivity);
        //db = dbHelper.getWritableDatabase();
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);

        String pref_stringa_connessione = preferences.getString("pref_stringa_connessione", "");
        Log.d(TAG,"- " + pref_stringa_connessione);
        String addsComplete = Costanti.host +  Costanti.attracco_android;
        Log.d(TAG,"addsComplete -> " + addsComplete);

        RequestQueue reQueue = Volley.newRequestQueue(MainActivity.mainActivity);

        StringRequest request=new StringRequest(com.android.volley.Request.Method.POST,
                addsComplete,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Cosa ricevo: "+response);

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            //for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                //Devo aggiungere i dati nel DB
                                //ContentValues record = new ContentValues();

                                Log.d(TAG,jsonObject.toString());

                                String indirizzoPortale = jsonObject.getString("indirizzoPortale");
                                String indirizzoPortaleEncoding = jsonObject.getString("indirizzoPortaleEncoding");
                                String NameOdbc = jsonObject.getString("NameOdbc");

                                Log.d(TAG,indirizzoPortale + " - " + indirizzoPortaleEncoding + " - " + NameOdbc);

                                //Devo Salvare Dominio di indirizzoPortale in pref_indirizzoInvio
                                String dominio = Costanti.extractDomain(indirizzoPortale);

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("pref_indirizzoInvio", dominio);
                                editor.putString("pref_db_string", NameOdbc);
                                editor.commit();

                                String indirizzoInvio = preferences.getString("pref_indirizzoInvio", dominio);
                                //Devo Salvare NameOdbc in
                                String db_connect = preferences.getString("pref_db_string", NameOdbc);

                                Log.d(TAG,indirizzoInvio  + " - " + indirizzoPortaleEncoding + " - " + db_connect);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(
                            VolleyError error) {
                        Log.e(TAG, ""+error);
                        error.printStackTrace();
                        Toast.makeText(MainActivity.mainActivity, MainActivity.mainActivity.getResources().getString(R.string.copy_err), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                //params.put("Content-Type", "application/json; charset=ansi");
                params.put( "Content-Type", "application/x-www-form-urlencoded");
                params.put( "charset", "utf-8");

                String StringaAccesso = preferences.getString("pref_stringa_connessione", "");

                params.put("StringaAccesso", ""+StringaAccesso);

                return params;
            }
        };
        try{ reQueue.add(request); }
        catch(Exception e){ Log.e(TAG, ""+e); }
    }
}
