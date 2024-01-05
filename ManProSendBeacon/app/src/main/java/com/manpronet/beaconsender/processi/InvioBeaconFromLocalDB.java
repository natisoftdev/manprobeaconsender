package com.manpronet.beaconsender.processi;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.manpronet.beaconsender.MainActivity;
import com.manpronet.beaconsender.R;
import com.manpronet.beaconsender.database.DatabaseHelperBeacon;
import com.manpronet.beaconsender.dati.Beacon;
import com.manpronet.beaconsender.dati.Costanti;
import com.manpronet.beaconsender.servizio.SendBeaconService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import static com.manpronet.beaconsender.MainActivity.mainActivity;

public class InvioBeaconFromLocalDB extends Thread{

    private static final String TAG = "InvioBeaconFromLocalDB";
    public static final String AUTHORITY = "com.manpro.beaconscanner.provider.BeaconProvider";
    public static final String TABLE_NAME = "BeaconRilevati";

    private RequestQueue reQueue;

    private static Socket socket;
    private static PrintWriter printWriter;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    public Timer mTimer = null;
    ContentResolver crT = null;

    private static boolean flag;
    private static String dataRecFinto = "";

    public InvioBeaconFromLocalDB() {
        crT = SendBeaconService.sendBeaconService.getContentResolver();
        reQueue = Volley.newRequestQueue(SendBeaconService.sendBeaconService);
        // cancel if already existed
        if(mTimer != null) { mTimer.cancel(); }
        else {
            // recreate new
            mTimer = new Timer();
        }
    }

    public void start(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SendBeaconService.sendBeaconService);
        String timeSendSec = preferences.getString("pref_time_send","");// 5 Minuti
        int secondi;
        if(timeSendSec != ""){
            secondi = Integer.parseInt(timeSendSec) * 60;
        }
        else {
            secondi = Costanti.timeSendSec;
        }

        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 30*1000, secondi * 1000);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                        Log.i(TAG + " - CICLO 5 MINUTI","ESECUZIONE PROCESSO");
                        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
                        execute(crT,uri);
                }
            });
        }
    }

    private synchronized void execute(ContentResolver cr, Uri uri){
        String data = getDateNow();
        Log.d(TAG," - data -> " + data);
        //Costruisco file reportCicliScanner.lm
        SendBeaconService.createReportCicliScanner(data,"");
        implementazioneDB(cr,uri);
        //Controllo stato della Connettività
        SendBeaconService.createReportConnettivita(data);
        //Controllo se devo inviare reportBLocchi e reportConnettività via FTP
        SendBeaconService.checkReportistica(data);
        //Invio dei dati del Beacon in formato XML al Socket
        sendBeaconRilevatiSocket();
    }

    private synchronized void implementazioneDB(ContentResolver cr, Uri uri){

        String [] sqlSelect = {
                DatabaseHelperBeacon.COLUMN_ID,
                DatabaseHelperBeacon.COLUMN_DB_CONNECT,
                DatabaseHelperBeacon.COLUMN_UTENTE,
                DatabaseHelperBeacon.COLUMN_PASSWORD,
                DatabaseHelperBeacon.COLUMN_MODEL,
                DatabaseHelperBeacon.COLUMN_DATACAMPIONAMENTO,
                DatabaseHelperBeacon.COLUMN_ANDROID,
                DatabaseHelperBeacon.COLUMN_LIVELLOBATTERIA,
                DatabaseHelperBeacon.COLUMN_TEMPOCAMPIONAMENTO,
                DatabaseHelperBeacon.COLUMN_NUMEROVERSIONE,
                DatabaseHelperBeacon.COLUMN_BEACON_MAC,
                DatabaseHelperBeacon.COLUMN_BEACON_DISTANCE,
                DatabaseHelperBeacon.COLUMN_BEACON_TXPOWER,
                DatabaseHelperBeacon.COLUMN_BEACON_RSSI,
                DatabaseHelperBeacon.COLUMN_BIT,
                DatabaseHelperBeacon.COLUMN_ANDROID_ID,
                DatabaseHelperBeacon.COLUMN_ANDROID_ID
        };

        Cursor c = null;
        //Ottengo il numero di record presenti nel DB
        //int count = databaseHelperBeacon.getBeaconCount();
        long r = 0;
        //Log.i("ANDROIDOS",android.os.Build.VERSION.SDK_INT+"");
        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O )
            { //Log.i("ANDROIDOS",">= Oreo");
                c = cr.query(uri,sqlSelect,null,null); }
        else
            { //Log.i("ANDROIDOS","< Oreo");
                c = cr.query(uri, sqlSelect, null, null, null); }
/*
        if (c == null) {
            Log.d(TAG, "Cursor c == null.");
            return;
        }
*/
//        Log.i(TAG + " - implementazioneDB","Ho preso i dati col Cursor");
//        Log.i(TAG + " - implementazioneDB","Size del Cursor -> " + c.getCount());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SendBeaconService.sendBeaconService);

        int sizePrimaApp = 0;
        try {
            sizePrimaApp = c.getCount();
            if(sizePrimaApp > 0) {
                //for (int i = 0; i < 100; i++) {
                int i = 0;

                String dataNow = getDateNow();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("pref_lastDataSend", dataNow);
                editor.commit();

                //Log.d("CICLOINVIO", "Ciclo -> " + i);
                while (c.moveToNext() && (i < 100)) {
                    //SE RECORD FINTO DI FINE CICLATA SCANNERbEACON != XXXXXXXXXXXXXXXXX
                    //Log.i("CICLISCANNER","MAC: -> " + c.getString(10) );
                    Log.i(TAG + " - implementazioneDB","MAC Stato: -> " + c.getString(10).equals("XXXXXXXXXXXXXXXXX") );
                    Log.i(TAG + " - CICLOINVIO","-> " + c.getString(0));
                    Log.i(TAG + " - CICLOINVIO","Ciclo -> " + i);
                    //Faccio le solite cose
                    if (!c.getString(10).equals("XXXXXXXXXXXXXXXXX")) {
                        Log.i(TAG + " - CICLOINVIO","Dato Reale");
                        //Inizializzazione dell'oggetto
                        Beacon beacon = new Beacon();
                        //Settaggio dei campi
                        beacon.setId(Integer.parseInt(c.getString(0)));
                        beacon.setDb_connect(c.getString(1));
                        //beacon.setDb_connect(preferences.getString("pref_db_string", ""));
                        Log.i(TAG + " - CICLOINVIO_db","c.getString(1) -> " + c.getString(1));
                        beacon.setUtente(c.getString(2));
                        //beacon.setUtente(preferences.getString("pref_db_login", ""));
                        Log.i(TAG + " - CICLOINVIO_ut","c.getString(2) -> " + c.getString(2));
                        beacon.setPassword(c.getString(3));
                        //beacon.setPassword(preferences.getString("pref_db_password", ""));
                        Log.i(TAG + " - CICLOINVIO_pw","c.getString(3) -> " + c.getString(3));
                        beacon.setModel(c.getString(4));
                        beacon.setDatacampionamento(c.getString(5));
                        beacon.setAndroid(Integer.parseInt(c.getString(6)));
                        beacon.setLivelloBatteria(Integer.parseInt(c.getString(7)));
                        beacon.setTempocampionamento(Integer.parseInt(c.getString(8)));
                        beacon.setNumeroversione(Costanti.versionCode);
                        beacon.setMac(c.getString(10));
                        beacon.setDistance(c.getString(11));
                        beacon.setTxpower(Integer.parseInt(c.getString(12)));
                        beacon.setRssi(Integer.parseInt(c.getString(13)));
                        beacon.setBit(Integer.parseInt(c.getString(14)));
                        beacon.setAndroid_id(Costanti.android_id);

                        Log.i(TAG + " - BeaconPreInsert",beacon.toString());
                        //Inserisco record in DB locale
                        r = SendBeaconService.databaseHelperBeacon.insertRecord(beacon);
                        //Cancello record da DB dell'altra APP
                        cr.delete(uri, "ID = ?", new String[]{c.getString(0)});
                    }
                    else {
                        //Log.i("CICLOINVIO","Dato Fittizzio");
                        //String id = c.getString(0);
                        String dt = c.getString(5);
                        String nBeacon = c.getString(12);
                        //Log.i("CICLISCANNER","ID -> " + id + " | Data -> " + dt + " | N.Beacons -> " + nBeacon);
                        //Log.i("CICLISCANNER","Data PRECEDENTE -> " + dataRecFinto );
                        //Log.i("CICLISCANNER","Confronto -> " + dataRecFinto + " = " + dt );
                        //Log.i("CICLISCANNER","Confronto -> " + ( (dataRecFinto.equals(dt)) ) );

                        if (!(dataRecFinto.equals(dt))) {
                            dataRecFinto = dt;
                            //Log.i("CICLISCANNER","Inserisco valore in FILE");
                            //Log.i("CICLISCANNER","Cosa inserisco -> " + dt + " - " + nBeacon);
                            //Inserisco la data nel file reportCicliScanner
                            SendBeaconService.createReportCicliScanner(dataRecFinto, nBeacon);
                        }
                        cr.delete(uri, "ID = ?", new String[]{c.getString(0)});
                    }

                    i++;
                }
                //}
            }
            c.close();

        }catch ( NullPointerException exception ) {
            sendAlertMessage("Nessun dato da inviare");
            Log.i(TAG + " - Ciclo invio Dati ", "Nessun dato da inviare");
        }
    }

    private String getDateNow(){
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentTime = sdf.format(dt);
        return  currentTime;
    }

    private synchronized void sendBeaconRilevatiSocket(){
        Log.d(TAG,"sendBeaconRilevatiSocket -> DENTRO");
        List<Beacon> list = SendBeaconService.databaseHelperBeacon.getBeacon();
        int size = list.size();
        Log.i(TAG + " - sendBeaconRilSocket", "Numero record in DB: "+size);
        //Toast.makeText(MyServiceBeacon.serviceB, "Numero record in DB: "+size, Toast.LENGTH_LONG).show();
        int max = 50;
        if(size > max){
            size = max;
        }
        Log.d(TAG,"size -> " + size);
        for(int i = 0; i < size; i++ ){
            /*
            String s = "";

            Log.i("sendBeaconRilSocket","DB -> "+SendBeaconService.db_connect);
            Log.i("sendBeaconRilSocket","Utente -> "+SendBeaconService.Login);
            Log.i("sendBeaconRilSocket","Password -> "+SendBeaconService.Password);

            if( (SendBeaconService.db_connect.length() > 0) && (SendBeaconService.Login.length() > 0) && (SendBeaconService.Password.length() > 0)){
                //Invio dei dati tramite SOCKET

                s = createStringFormatXML(list.get(i));
                Log.d("sendBeaconRilSocket","CERCO DI INVIARE");
                //invioDati(s);
                //Log.i("sendBeaconRilSocket",s);

                //Invio dei dati tramite VOLLEY (PHP)
                //flag =
                //Log.i("sendBeaconRilSocket","Provo a mandare i dati via PHP");
                sendDatiWithPHP(list.get(i));
                //Log.i("sendBeaconRilSocket","Flag -> " + flag);
                //VolleyBeacon v = new VolleyBeacon(SendBeaconService.sendBeaconService,list.get(i));
            }
            else{
                //TODO: Implementare messaggio di errore
            }
            */

            if( (list.get(i).getDb_connect().length() > 0) && (list.get(i).getUtente().length() > 0) && (list.get(i).getPassword().length() > 0)){
                //Codice funzionante da quando ho tolto le credenziali dalle impostazioni
                Log.d(TAG + " - sendBeaconRilSocket","CERCO DI INVIARE");
                sendDatiWithPHP(list.get(i));
            }
            else{
                //TODO: Implementare messaggio di errore
            }

            //Log.i("sendBeaconRilSocket","Flag -> "+flag);
            if(flag){
                //A questo punto cancello i dati dal DB
                //per test
                //databaseHelperBeacon.updateRecord(BeaconRilevati.COLUMN_BIT,1,new String[]{BeaconRilevati.COLUMN_ID, String.valueOf(oneBeacon.getId())});
                //codice reale
                Log.d(TAG + " - sendBeaconRilSocket","Elimino record");
                SendBeaconService.databaseHelperBeacon.deleteRecord(DatabaseHelperBeacon.COLUMN_ID, list.get(i).getId());
            }
            else{
                //TODO: Implementare messaggio di errore
            }
        }
    }

    private synchronized void sendDatiWithPHP(final Beacon beacon) {
        //boolean status = false;
        String dominio = Costanti.getIndirizzoPortale(SendBeaconService.sendBeaconService);
        //if (dominio.length() > 0){}
        //else dominio = Costanti.host_sviluppo;
        String addsComplete = Costanti.checkPortalAddress(dominio,SendBeaconService.sendBeaconService) +
               /* ":8089" + */Costanti.ricezione_beacon_utilizzatori;

        Log.i(TAG + " - sendDatiWithPHP",addsComplete);

        if (reQueue == null) {
            reQueue = Volley.newRequestQueue(SendBeaconService.sendBeaconService);
            Log.i(TAG + " - sendDatiWithPHP","Setting a new request queue");
        }

        StringRequest request = new StringRequest(Request.Method.POST, addsComplete,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG + " - sendDatiWithPHP", "onResponse -> " + response);

                        //ArrayList<String> result = new ArrayList<String>();
                        //if(response.contains("result")){
                            //Log.i("sendDatiWithPHP", "Risultato -> " + response.split(":")[1].charAt(0));

                            try{

                                JSONObject jsonObjectResponse = new JSONObject(response);
                                String RuoloJson = jsonObjectResponse.getString("RuoloJson");
                                String CredenzialiJson = jsonObjectResponse.getString("CredenzialiJson");
                                Log.d(TAG, TAG + " - Cosa ricevo -> RuoloJson: "+RuoloJson);
                                Log.d(TAG, TAG + " - Cosa ricevo -> BeaconAmbientiJson: "+CredenzialiJson);

                                JSONObject jsonObjRuolo = new JSONObject(RuoloJson);
                                JSONObject jsonObjCredenzialiJson = new JSONObject(CredenzialiJson);
                                //JSONArray jsonArrayBeaconAmbienti = new JSONArray(BeaconAmbientiJson);

                                int ruolo = jsonObjRuolo.getInt("ruolo");
                                Log.d(TAG, TAG + " - Devo salvare nelle preferenze alla voce ruolo -> "+ruolo);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("ruolo", ruolo);
                                editor.commit();

                                int value = Integer.parseInt(String.valueOf(CredenzialiJson.split(":")[1].charAt(0)));
                                //Log.i("sendDatiWithPHP", "onResponse value -> " + value);
                                if( ! ( value == 1 ) ){
                                    //notifico il messaggio tramite Notifica su device
                                    Log.d(TAG,TAG + " - sendAlertMessage -> 1");
                                    sendAlertMessage("Pacchetto "+ getDateNow() +" non inviato");
                                }
                            }
                            catch (Exception e){
                                Log.e(TAG + " - sendDatiWithPHP", "Errore -> " + e);
                                sendAlertMessage("Pacchetto "+ getDateNow() +" non inviato");
                                Log.e(TAG,"sendAlertMessage -> 2");
                            }

                        //}
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //notifico il messaggio tramite Notifica su device
                        sendAlertMessage("Pacchetto "+ getDateNow() +" non inviato");
                        Log.e(TAG,"sendAlertMessage -> 3");
                        Log.e("sendDatiWithPHP","onErrorResponse -> " + error);
                        flag = false;
                    }
                })
        {
            /*
            @Override
            public String getBodyContentType() {
                return "text/plain";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                //TODO if you want to use the status code for any other purpose like to handle 401, 403, 404
                String statusCode = String.valueOf(response.statusCode);
                //Handling logic
                return super.parseNetworkResponse(response);
            }
            */

            @Override
            protected Map<String, String> getParams() {

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json; charset=ansi");

                params.put("Login", ""+beacon.getUtente());
                params.put("Password", ""+beacon.getPassword());
                params.put("db_connect", ""+beacon.getDb_connect());

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

                //Log.d("sendDatiWithPHP","Costanti.android_id -> " + Costanti.android_id);
                Log.d(TAG + " - sendDatiWithPHP","Cosa mando -> "+params.toString());
                return params;
            }
        };

        try {
            request.setRetryPolicy(new DefaultRetryPolicy(1000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            reQueue.add(request);
            flag = true;
        }
        catch (Exception e){
            Log.e(TAG + " - sendDatiWithPHP","-> " + e);
            flag = false;
        }

        //return  status;
    }

    public void invioDati(String doc){
        SendingData sendingData = new SendingData(doc);
        sendingData.execute();
    }

    class SendingData extends AsyncTask<Void, Void, Void> {
        private String fileContent;

        /**
         * Costruttore parametrico
         * @param stringa dati da inviare
         */
        public SendingData(String stringa){
            fileContent = stringa;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket();   //https://manpronet.com:8082
                //socket.setSoTimeout(5000);

                Log.i(TAG + " - SendingData",Costanti.getIndirizzoPortale(SendBeaconService.sendBeaconService));
                InetAddress address = InetAddress.getByName(new URL(Costanti.getIndirizzoPortale(SendBeaconService.sendBeaconService)).getHost());
                String ip = address.getHostAddress();
                socket.connect(new InetSocketAddress(ip,Costanti.portSocket),5000);

                Log.i(TAG + " - SendingData", "Stato Socket ->  "+socket.isConnected());

                printWriter = new PrintWriter(socket.getOutputStream());
                //printWriter.write(getDateNow()+"_"+Login+"_"+android.os.Build.MODEL+"_reportArrayBeacon.xml&&");//Nome del File -> 15-01-2019 12:13:23_barone_G8342_reportArrayBeacon.xml (esempio)
                //Log.i("SendingData", "STRINGA -> "+fileContent);
                printWriter.write(fileContent);//Contenuto
                printWriter.flush();
                printWriter.close();
                socket.close();

                flag = true;
                Log.i(TAG + " - SendingData", "Stato Socket Flag ->  "+flag);

            } catch (ConnectException c) {
                Log.e(TAG + " - SendingData", "Stato Socket ->  "+socket.isConnected());
                //Log.e("SendingData", ""+MyServiceBeacon.getDataNow());
                Log.e(TAG + " - SendingData", "Server non raggiungibile "+ getDateNow());
                //notifico il messaggio tramite Notifica su device
                sendAlertMessage("Pacchetto "+ getDateNow() +" non inviato.\n Socket non raggiungibile");
                Log.d(TAG,"sendAlertMessage -> 4");
                c.printStackTrace();
                flag = false;
                Log.e(TAG + " - SendingData", "Stato Socket Flag ->  "+flag);
            } catch (Exception e) {
                Log.e(TAG + " - SendingData", "Errore "+e);
                e.printStackTrace();
                flag = false;
                Log.e("SendingData", "Stato Socket Flag ->  "+flag);
            }
            return null;
        }
    }

    private void sendAlertMessage(String message){
        NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(mainActivity,"AlertMessage")
                .setSmallIcon(R.drawable.ic_send_not)
                .setContentTitle("Messaggio di avviso")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canale2";
            String description = "Desc Canale2";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("AlertMessage", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);
        notificationManager.notify(232, mBuilder2.build());
    }

    public static String createStringFormatXML(Beacon beacon){
        //Log.i("createStringFormatXML","STO COSTRUENDO STRINGA");
        String fileXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>";
        fileXML += "<invio>";

            fileXML += "<db_connect>"+beacon.getDb_connect()+"</db_connect>";
            fileXML += "<utente>"+beacon.getUtente()+"</utente>";
            fileXML += "<password>"+beacon.getPassword()+"</password>";

            fileXML += "<model>"+beacon.getModel()+"</model>";
            fileXML += "<datacampionamento>"+beacon.getDatacampionamento()+"</datacampionamento>";
            fileXML += "<androidid>"+beacon.getAndroid()+"</androidid>";
            fileXML += "<batterialivello>"+beacon.getLivelloBatteria()+"</batterialivello>";
            fileXML += "<tempocampionamento>"+beacon.getTempocampionamento()+"</tempocampionamento>";
            fileXML += "<numeroVersione>"+beacon.getNumeroversione()+"</numeroVersione>";
            fileXML += "<mac>"+beacon.getMac()+"</mac>";
            fileXML += "<distance>"+beacon.getDistance()+"</distance>";
            fileXML += "<txpower>"+beacon.getTxpower()+"</txpower>";
            fileXML += "<rssi>"+beacon.getRssi()+"</rssi>";
            fileXML += "<android_id>"+beacon.getAndroid_id()+"</android_id>";

        fileXML += "</invio>";

        //restituisco i dati
        //Log.i("createStringFormatXML",fileXML);
        return fileXML;
    }
}