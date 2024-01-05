package com.manpronet.beaconsender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.manpronet.beaconsender.dati.Costanti;
import com.manpronet.beaconsender.javascript.MyJavaScriptInterface;
import com.manpronet.beaconsender.servizio.SendBeaconService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static MainActivity mainActivity = null;

    public TextView lastDataSend;
    public TextView title_lastDataSend;

    private static File myDir;
    public static File fileReportC;
    public static File fileReportCS;
    public static final String nameFileConnettivita = "reportConnettivita.lm"; //nome del file contenente i dati di reportistica
    public static final String nameFileCicliScanner = "reportCicliScanner.lm";

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public SharedPreferences preferences;
    //WebView webView;
    //WebView webView2;

    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = new String[]{
            //Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            //Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
    };

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Costanti.android_id  = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //Log.d("sendDatiWithPHP","Costanti.android_id -> " + Costanti.android_id);
        Costanti.versionCode = BuildConfig.VERSION_CODE;
        //Log.d("sendDatiWithPHP","Costanti.versionCode -> " + Costanti.versionCode);
        Costanti.versionName = BuildConfig.VERSION_NAME;
        //Log.d("sendDatiWithPHP","Costanti.versionName -> " + Costanti.versionName);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.richiestaPosizione));
                builder.setMessage(getString(R.string.mexRichiestaPosizione));
                builder.setPositiveButton(getString(android.R.string.ok), null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mainActivity = this;

        init();

        //inzio del servizio
        startMyServiceBeacon();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastDataSend.setText(preferences.getString("pref_lastDataSend", ""));
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void init(){

        //Creo i file
        String root = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString();
        myDir = new File(root + "/log_beacon");
        //Log.i("myDir",myDir.toString());
        if (!myDir.exists()) { myDir.mkdirs(); }
        createFile(getDateNow());

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Entro nelle impostazioni
        Button btn = findViewById(R.id.btnImpostazioni);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImpostazioni();
            }
        });
        //Controllo che ci sia Internet
        Button btnInternet = findViewById(R.id.btnInternet);
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet();
            }
        });
        //Controllo che il Bluetooth sia attivo
        Button btnBluetooth = findViewById(R.id.btnBluetooth);
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBluetooth();
            }
        });

        title_lastDataSend = findViewById(R.id.title_lastDataSend);
        title_lastDataSend.setText("Ultimo Invio:");

        lastDataSend = findViewById(R.id.lastDataSend);
        lastDataSend.setText(preferences.getString("pref_lastDataSend", ""));

        Button btnLastScanData = findViewById(R.id.btnLastScanData);
        btnLastScanData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("MainActivity","-> " + preferences.getString("pref_lastDataSend", ""));
                lastDataSend.setText(preferences.getString("pref_lastDataSend", ""));
            }
        });

        //Controllo che le Preferenze sia corrette
        //Button btnPref = findViewById(R.id.btnPref);
        /*btnPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPref();
            }
        });*/

    /*
        webView = findViewById(R.id.webViewPref);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("setWebViewClient", "inside onPageStarted");
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                Log.d("setWebViewClient", "inside onPageFinished");
                //String input = "value='true'";
                //view.findAllAsync(input);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
                Log.d("setWebViewClient", "onReceivedError" );
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("setWebViewClient", "shouldOverrideUrlLoading" );
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                } else if (url.startsWith("mailto:")) {
                    String body = "Enter your Question, Enquiry or Feedback below:\n\n";
                    Intent mail = new Intent(Intent.ACTION_SEND);
                    mail.setType("application/octet-stream");
                    mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"email address"});
                    mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    mail.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(mail);
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                Log.d("setWebViewClient", "onReceivedError" );
            }

        });

        webView.clearSslPreferences();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(new MyJavaScriptInterface(this),"HTMLViewer");
    */

        String Login = preferences.getString("pref_db_login", "");
        String Password = preferences.getString("pref_db_password", "");
        String db_connect = preferences.getString("pref_db_string", "");
        Log.d("LINGUA", Locale.getDefault().getLanguage());

        //String dominio = Costanti.getIndirizzoPortale(this);
        //String addsComplete;

        //addsComplete = Costanti.checkPortalAddress(dominio,this);

        //String url = addsComplete + Costanti.ricezione_beacon_login_lingue+"?Login="+Login+"&Password="+Password+"&db_connect="+db_connect+"&Lingua="+Locale.getDefault().getLanguage();

        //Log.d("LINGUA", url);
        //webView.loadUrl(url);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) { if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { return false;} }
        }
        return true;
    }

    public void startMyServiceBeacon() {
        Intent myService = new Intent(this, SendBeaconService.class);
        startService(myService);
    }

    public void stopMyServiceBeacon(){
        Intent myService = new Intent(this, SendBeaconService.class);
        stopService(myService);
    }

    private void openImpostazioni(){
        Intent intent = new Intent(MainActivity.this,ImpostazioniActivity.class);
        startActivity(intent);
    }

    private void checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast toast = Toast.makeText(this, R.string.bluetoothAcceso, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, R.string.bluetoothSpento, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void checkInternet(){
        if (checkInternetApp(getApplicationContext())) {
            // Internet available, do something
            Toast toast = Toast.makeText(this, R.string.connessioneAccesa, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            // Internet not available
            Toast toast = Toast.makeText(this, R.string.connessioneSpenta, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void checkPref(){
        /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Costanti.getIndirizzoPortale(getApplicationContext()).length() > 0 &&
            preferences.getString("pref_db_string", "").length() > 0 &&
            preferences.getString("pref_db_login", "").length() > 0 &&
            preferences.getString("pref_db_password", "").length() > 0
            )
        {
            // Internet available, do something
            Toast toast = Toast.makeText(this, "Le preferenze risultano corrette", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            // Internet not available
            Toast toast = Toast.makeText(this, "Le preferenze NON risultano corrette", Toast.LENGTH_SHORT);
            toast.show();
        }*/
    }

    private static boolean checkInternetApp(Context context){
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void sendFilesAndDB(){
        File t = new File(getApplicationInfo().dataDir+"/databases/BeaconRilevamenti.db");
        Log.i("Percorso", t.getAbsolutePath() + " -> " + t.exists());

        File dest = new File(SendBeaconService.getMyDir()+"/BeaconRilevamenti.db");
        try {
            copy(t,dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Ottengo i percorsi dei due file
        ArrayList<File> filesRep = trovaFiles(myDir);
        //Devo cercare nella cartella i file reportBlocchi.lm e reportConnettivita.lm con la data di oggi

        Log.d("sendFilesAndDB"," -> " + filesRep.toString());

        File fileB = filesRep.get(2);
        File fileC = filesRep.get(1);
        File fileCS = filesRep.get(0);
        File fileDB = new File(SendBeaconService.getMyDir()+"/BeaconRilevamenti.db");
        //readFile(new File(getApplicationInfo().dataDir+"/databases/BeaconRilevamenti.db"));

        //File fileDB = new File(getApplicationInfo().dataDir+"/databases/BeaconRilevamenti.db");
        //File fileDB = new File("//data/data/com.manpronet.manprosendbeacon/databases/BeaconRilevamenti.db");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String Login = preferences.getString("pref_db_login", "");

        if(
                (fileB != null && fileB.exists()) &&
                (fileC != null && fileC.exists()) /*&& (fileDB != null && fileDB.exists()) */ && (fileCS != null && fileCS.exists()) ){ //se i files sono presenti

            Log.i("sendFilesAndDB", "Compilo email");
            //
            ArrayList<Uri> files = new ArrayList();
            files.add(FileProvider.getUriForFile(MainActivity.mainActivity, getApplicationContext().getPackageName() + ".my.package.name.provider", fileB));//reportBlocchi
            files.add(FileProvider.getUriForFile(MainActivity.mainActivity, getApplicationContext().getPackageName() + ".my.package.name.provider", fileC));//reportConnettività
            files.add(FileProvider.getUriForFile(MainActivity.mainActivity, getApplicationContext().getPackageName() + ".my.package.name.provider", fileDB));//DB
            files.add(FileProvider.getUriForFile(MainActivity.mainActivity, getApplicationContext().getPackageName() + ".my.package.name.provider", fileCS));//reportConnettività

            //files.add(FileProvider.getUriForFile(MainActivity.mainActivity,"",fileDB));//DB
            //files.add(Uri.parse(fileDB.toString()));//DB
            //
            Intent email = new Intent();
            email.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            email.setAction(Intent.ACTION_SEND_MULTIPLE);
            email.setType("*/*");
            //email.setType("text/plain");                                   //natisoft.sviluppo@gmail.com | malferrari.natisoft@gmail.com
            //email.putExtra(Intent.EXTRA_MIME_TYPES,"/");
            email.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] {"natisoft.sviluppo@gmail.com"}); // a chi
            email.putExtra(Intent.EXTRA_SUBJECT,"Invio Report Debug - "+android.os.Build.MODEL+" - "+Login);// titolo email
            email.putExtra(Intent.EXTRA_TEXT,"Invio in allegato il file di Report Debug\n App version: " + Costanti.versionName);//eventuale testo
            email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            //
            startActivityForResult(Intent.createChooser(email, getString(R.string.chooseApplication)),1);
        }
        else{
            //Magari mettere un messaggio TOAST che dice che uno dei file non sono stati ancora caricati
            Log.i("File", "File non trovati");
        }
    }

    private static ArrayList<File> trovaFiles(File listaFile){
        ArrayList<File> returnFiles = new ArrayList<>();
        //Log.i("readListFiles","Leggo CARTELLA");
        File[] listOfFiles = listaFile.listFiles();
        if(listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (
                        listOfFile.getName().equals(getDateString() + "_reportConnettivita.lm") ||
                        listOfFile.getName().equals(getDateString() + "_reportCicliScanner.lm"))
                {
                    //Log.i("readListFiles", "Nome FIlE -> " + listOfFile.getName());
                    returnFiles.add(listOfFile);
                }
            }
            File reportBlocchi = new File("/storage/emulated/0/Android/data/com.manpro.beaconscanner/files/Documents/log_beacon/"+ getDateString() + "_reportBlocchi.lm");
            returnFiles.add(reportBlocchi);
        }
        return returnFiles;
    }

    private static String getDateString() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(new Date());
    }
/*
    //Metodo per il debug
    private static void readFile(File file){
        File[] listOfFiles = file.listFiles();
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                Log.i("readFile", "Nome Path -> " + listOfFiles[i].getAbsolutePath());
                Log.i("readFile", "Nome FIlE -> " + listOfFiles[i].getName());
            }
        }
    }
*/
    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    private String getDateNow(){
        String currentTime;
        Date dt = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        currentTime = sdf.format(dt);
        return currentTime;
    }

    public static synchronized void createFile(String dateN){
        String today = dateN.split(" ")[0];
        fileReportC = new File( myDir, today + "_" + nameFileConnettivita );
        fileReportCS = new File( myDir, today + "_" + nameFileCicliScanner );
        if ( !fileReportC.exists() ) {//If file è vuoto || non esistente
            try {
                OutputStream os = new FileOutputStream(fileReportC);
                String text = dateN + "\n";
                Log.d("fileReportC", "Testo inserito -> " + text);
                byte[] data = text.getBytes();
                os.write(data);
                os.close();
                Log.d("fileReportC", "File Path= " + fileReportC.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ( !fileReportCS.exists() ) {//If file è vuoto || non esistente
            try {
                OutputStream os = new FileOutputStream(fileReportCS);
                String text = dateN + "\n";
                Log.d("fileReportCS", "Testo inserito -> " + text);
                byte[] data = text.getBytes();
                os.write(data);
                os.close();
                Log.d("fileReportCS", "File Path= " + fileReportCS.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}