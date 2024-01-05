package com.manpronet.beaconsender.servizio;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.manpronet.beaconsender.MainActivity;
import com.manpronet.beaconsender.R;
import com.manpronet.beaconsender.database.DatabaseHelper;
import com.manpronet.beaconsender.database.DatabaseHelperBeacon;
import com.manpronet.beaconsender.dati.Costanti;
import com.manpronet.beaconsender.dati.Reportistica;
import com.manpronet.beaconsender.ftp.FtpServer;
import com.manpronet.beaconsender.processi.InvioBeaconFromLocalDB;
import com.manpronet.beaconsender.processi.InvioFilesFTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SendBeaconService extends Service {

    private static final String TAG = "SendBeaconService";

    public static SendBeaconService sendBeaconService;
    public static InvioBeaconFromLocalDB invioBeaconFromLocalDB;
    public static InvioFilesFTP invioFilesFTP;
    public static DatabaseHelper databaseHelper; // DatabaseHelper Object
    public static DatabaseHelperBeacon databaseHelperBeacon; // DatabaseHelper Object

    public static final String nameFileConnettivita = "reportConnettivita.lm"; //nome del file contenente i dati di reportistica
    public static final String nameFileCicliScanner = "reportCicliScanner.lm";
    //private static Socket socket;
    //private static PrintWriter printWriter;
    //public static File fileReportB;
    public static File fileReportC;
    public static File fileReportCS;

    private static File myDir;

    //public static String Login;
    //public static String Password;
    //public static String db_connect;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Salvo il contesto della classe
        sendBeaconService = this;
        //Implementazione del percorso con cartella dove verranno salvati i file
        String root = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString();
        myDir = new File(root + "/log_beacon");
        if (!myDir.exists()) { myDir.mkdirs(); }

        //readListFiles(myDir);

        //Inizializzazione dei due DB
        databaseHelper = new DatabaseHelper(this);
        databaseHelperBeacon = new DatabaseHelperBeacon(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Login = preferences.getString("pref_db_login", "");
        //Password = preferences.getString("pref_db_password", "");
        //db_connect = preferences.getString("pref_db_string", "");

        //Log.i("SharedPreferences",Login + " - " + Password + " - " + db_connect);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_send_not);
        builder.setContentTitle("ManProSendBeacon in esecuzione");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ManProSendBeaconChannel",
                    "My Notification ManProSendBeaconName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification ManProSendBeaconChannel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
            startForeground(21,builder.build());
        }

        //Esecuzione del ciclo dei 5 minuti

        //Il compito è:
        // - Prendere i dati dall'altra applicazione;
        // - Inviare i dati in formato XML al Socket;
        // - Se operazione positiva cancella il record dal DB
        invioBeaconFromLocalDB = new InvioBeaconFromLocalDB();
        invioBeaconFromLocalDB.start();

        invioFilesFTP = new InvioFilesFTP();
        invioFilesFTP.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand -> START_STICKY");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy -> Servizio distrutto");
        super.onDestroy();
    }

    public static synchronized void createReportConnettivita(String dataN){
        //Log.i("createReportCon", "Dentro");
        String today = dataN.split(" ")[0]; //prendo la prima parte della stringa ovvero dd-mm-yyyy

        //File filesDir = sendBeaconService.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        //fileReportB = new File( myDir, today + "_" + nameFileBlocchi );
        fileReportC = new File(myDir, today + "_" + nameFileConnettivita);

        //Log.i("createReportCon", "fileReportC.length() -> "+fileReportC.length());
        if (!fileReportC.exists()){//If file è vuoto || non esistente
            //Log.i("createReportBlocchi","File vuoto o non esistente inserisco il valore -> "+dataN);
            //Creo file e aggiungo dateNow
            try { fileReportC.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
            writeToFile(fileReportC,dataN+"\n");
        }

        if(! checkInternet(sendBeaconService.getApplicationContext())){ //se false == errore
            Log.e("createReportCon", "No internet");
            //Data attuale
            String dateNow = dataN+"\n";
            //If file vuoto o non esistente
            if (fileReportC.length() == 0 || !fileReportC.exists()){
                //Creo file e aggiungo dateNow
                writeToFile(fileReportC,dateNow);
            }
            //Else
            else{
                long maxSize = ( Costanti.maxSizeFile * ( 1024L * 1024L ) ); //corrisponde a 22 MB
                if(fileReportC.length() > maxSize){//devo cancellare il file
                    //Cancello
                    boolean success = fileReportC.delete();
                    // Se si è verificato un errore...
                    if (!success) {
                        Log.e("createReportCon", "Cancellazione fallita");
                        throw new IllegalArgumentException("Cancellazione fallita");
                    }
                    else {
                        //Ricreo il file e aggiungo dateNow
                        writeToFile(fileReportC,dateNow);
                        //Log.i("createReportCon",""+dateNow);
                    }
                }
                else{
                    //Aggiungo dateNow
                    writeToFile(fileReportC,dateNow);
                    //Log.i("createReportCon",""+dateNow);
                }
            }
        }
    }

    public static synchronized void createReportCicliScanner(String dataN, String nBeacons) {
        //Log.i("createRepCicliSca", "Dentro");
        String today = dataN.split(" ")[0]; //prendo la prima parte della stringa ovvero dd-mm-yyyy

        //File filesDir = sendBeaconService.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        //fileReportB = new File( myDir, today + "_" + nameFileBlocchi );
        fileReportCS = new File(myDir, today + "_" + nameFileCicliScanner);

        //Log.i("createRepCicliSca", "fileReportC.length() -> "+fileReportCS.length());

        //Se file non essite lo creo
        if (!fileReportCS.exists()){//If file è vuoto || non esistente
            //Log.i("createRepCicliSca","File vuoto o non esistente inserisco il valore -> "+dataN);
            //Creo file e aggiungo dateNow
            try { fileReportCS.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
            writeToFile(fileReportCS,dataN + " - " + nBeacons +"\n");
            //Log.i("createRepCicliSca", "Scrivo in " + nameFileCicliScanner + " -> "+dataN);
        }

        else{//Aggiungo la data
            writeToFile(fileReportCS,dataN + " - " + nBeacons +"\n");
            Log.i("createRepCicliSca", "Scrivo in " + nameFileCicliScanner + " -> "+dataN);
        }

    }

    private static synchronized void writeToFile(File fileName, String message){
        //Log.i("createRepCicliSca", "Scrivo -> " + fileName.getAbsolutePath() + " -> "+message);
        try {
            OutputStream os = new FileOutputStream(fileName);
            String text = message+"\n";
            Log.d("createRepCicliSca", "Testo inserito -> " + text);
            byte[] data = text.getBytes();
            os.write(data);
            os.close();
        } catch (Exception e) {
            Log.e("writeToFile","File "+fileName+" non creato");
            e.printStackTrace();
        }
    }

    private static boolean checkInternet(Context context){
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

    public static void checkReportistica(String dataNow) {
        //Invio file del giorno prima
        checkFilesYesterday(dataNow);
        //Cancellazione dei file del mese precedente
        checkDeleteFilesLastMonth();
        //Cancellazione dei record dal DB della Reportistica
        //checkDeleteRecordFromReportisticaDB();//serviceiBeaconOne.db
    }

    private static void checkFilesYesterday(String dataNow){
        String yesterday = getYesterdayDateString();//Ottengo la data di ieri
        //Log.i("checkReportistica", "Data di ieri -> "+yesterday);
        //Log.i("checkReportistica", "Data di oggi -> "+dataNow);
        if(databaseHelper == null){
            databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
        }
        boolean f = databaseHelper.getReportistica(yesterday, Reportistica.COLUMN_DATA); //true se è presente || false se non è presente
        //Log.i("checkReportistica", "Stato presenza della Reportistica -> "+f);
        if(!f) {
            if(databaseHelper == null){
                databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
            }
            //Log.i("checkReportistica", "Inserisco il valore in DB-> "+yesterday);
            //Inserisco il dato e lo setto a Data = yesterday e i due bit a 0
            databaseHelper.insertRecord("0","0","0",yesterday);
            databaseHelper.close();
        }
        //Log.i("checkReportistica", "Oggi è il giorno successivo a ieri -> "+(checkNextDay(dataNow,databaseHelper.getLastReportistica().getData())));
        //Se oggi è data diversa a ultima data messa su db
        if(checkNextDay(dataNow,databaseHelper.getLastReportistica().getData())){
            if(databaseHelper == null){
                databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
            }
            //REPORT CONNETTIVITA
            //Log.i("checkReportistica", "Se il BIT CONNETTIVITA == 0 va inviato il file ->"+((databaseHelper.getReportistica(yesterday).getConnettivita()) == 0));
            //Log.i("checkReportistica", "Nome del file che dovrei inviare -> "+yesterday+"_reportConnettivita.lm");
            //Log.i("checkReportistica", "NUOVO nome del file che dovrei inviare ->"+getYesterdayDateString()+"_"+Login+"_"+android.os.Build.MODEL+"_"+fileReportC.getName().split("_")[1]);
            if((databaseHelper.getReportistica(yesterday).getConnettivita()) == 0) {//Se data di ieri su DB ha i bit a 0, devo inviare il file
                File file = new File("");
                File[] listOfFiles = myDir.listFiles();
                if (listOfFiles.length > 0){

                    for (int i = 0; i < listOfFiles.length; i++) {
                        //Log.i("checkDFLM","Nome del file -> "+listOfFiles[i].getName());
                        if (listOfFiles[i].getName().indexOf(yesterday + "_reportConnettivita.lm") != -1) {
                            //Log.i("checkReportistica","Nome del file che devo mandare -> "+listOfFiles[i].getName());
                            file = listOfFiles[i];
                        }
                    }

                    //Se file esiste eseguo operazioni
                    if (file.exists()) {
                        String fileName = getYesterdayDateString() + /*"_" + Login +*/ "_" + android.os.Build.MODEL + "_" + file.getName().split("_")[1];
                        //Log.i("checkReportistica", "Nome del file che DEVO inviare PER CONNETTIVITA ->"+fileName);
                        //Log.i("checkReportistica", "Contenuto del FILE ->"+fileName);
                        //readFile(new File(fileName));
                        //Mando i file via FTP
                        boolean flagC = sendFileWithFTP(fileName, file); //Invio 04-02-2019_lghezzi_G8342_reportConnettivita.lm
                        //Log.i("DBSQL_", "Stato Connettivita -> "+flagC);
                        if (flagC) {// Se file inviato
                            if(databaseHelper == null) {
                                databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
                                databaseHelper.getWritableDatabase();
                            }
                            //Aggiorno su DB il bit di colonna Connettivita dove data = yesterday
                            databaseHelper.updateReportistica(Reportistica.COLUMN_CONNETTIVIA, 1, new String[]{Reportistica.COLUMN_DATA, yesterday});
                            databaseHelper.close();
                            //Log.i("DBSQL_", "Aggiornato il valore CONNETIVITA nel record -> "+i);
                        } else {
                            Log.e("checkReportistica", "Attenzione!! File CONNETIVITA non inviato ");
                        }
                    }
                }
            }else{Log.e("checkReportistica", "File CONNETIVITA già inviato ");}

            //REPORT BLOCCHI
            //Log.i("checkReportistica", "Se il BIT BLOCCHI == 0 va inviato il file ->"+((databaseHelper.getReportistica(yesterday).getBlocco()) == 0));
            //Log.i("checkReportistica", "Nome del file che dovrei inviare -> "+yesterday+"_reportBlocchi.lm");
            //Log.i("checkReportistica", "NUOVO nome del file che dovrei inviare ->"+getYesterdayDateString()+"_"+Login+"_"+android.os.Build.MODEL+"_reportBlocchi.lm");
            //Se data di ieri su DB ha i bit a 0, devo inviare il file
            if((databaseHelper.getReportistica(yesterday).getBlocco()) == 0){
                File file2 = new File("");
                File[] listOfFiles2 = myDir.listFiles();
                for (int i = 0; i < listOfFiles2.length; i++) {
                    //Log.i("checkDFLM","Nome del file -> "+listOfFiles2[i].getName());
                    if(listOfFiles2[i].getName().indexOf(yesterday+"_reportBlocchi.lm") != -1){
                        //Log.i("checkReportistica","Nome del file che devo mandare -> "+listOfFiles2[i].getName());
                        file2 = listOfFiles2[i];
                    }
                }
                //Se file esiste eseguo operazioni
                if(file2.exists()){
                    String fileName2 = getYesterdayDateString()+ /*"_"+Login+*/"_"+android.os.Build.MODEL+"_"+file2.getName().split("_")[1];
                    //Log.i("checkReportistica", "Nome del file che DEVO inviare PER BLOCCHI ->"+fileName2);
                    //Log.i("checkReportistica", "Contenuto del FILE ->"+fileName2);
                    //readFile(new File(fileName2));
                    //Mando i file via FTP
                    boolean flagB = sendFileWithFTP(fileName2,file2); //Invio reportBlocco.lm
                    //Log.i("DBSQL_", "Stato Blocco -> "+flagB);
                    if(flagB){// Se file inviato
                        if(databaseHelper == null){
                            databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
                        }
                        //Aggiorno su DB il bit di colonna Blocco
                        //Aggiorno dato della colonna Blocco nel record che data = yesterday
                        databaseHelper.updateReportistica(Reportistica.COLUMN_BLOCCO,1,new String[]{Reportistica.COLUMN_DATA,yesterday});
                        databaseHelper.close();
                        //Log.i("DBSQL_", "Aggiornato il valore BLOCCO nel record -> "+i);
                    }else{Log.e("checkReportistica", "Attenzione!! File BLOCCO non inviato ");}
                }
            }else{Log.e("checkReportistica", "File BLOCCO già inviato ");}

            //REPORT CICLISCANNER
            //Log.i("checkReportistica", "Se il BIT CICLISCANNER == 0 va inviato il file ->"+((databaseHelper.getReportistica(yesterday).getCicloScanner()) == 0));
            //Log.i("checkReportistica", "Nome del file che dovrei inviare -> "+yesterday+"_reportCicliScanner.lm");
            //Log.i("checkReportistica", "NUOVO nome del file che dovrei inviare ->"+getYesterdayDateString()+"_"+Login+"_"+android.os.Build.MODEL+"_"+fileReportCS.getName().split("_")[1]);
            if((databaseHelper.getReportistica(yesterday).getCicloScanner()) == 0){
                File file3 = new File("");
                File[] listOfFiles3 = myDir.listFiles();
                for (int i = 0; i < listOfFiles3.length; i++) {
                    //Log.i("checkDFLM","Nome del file -> "+listOfFiles[i].getName());
                    if(listOfFiles3[i].getName().indexOf(yesterday+"_reportCicliScanner.lm") != -1){
                        //Log.i("checkReportistica","Nome del file che devo mandare -> "+listOfFiles3[i].getName());
                        file3 = listOfFiles3[i];
                    }
                }
                //Se file esiste eseguo operazioni
                if(file3.exists()){
                    String fileName3 = getYesterdayDateString() + /*"_" + Login + */ "_"+android.os.Build.MODEL+"_"+file3.getName().split("_")[1];
                    //Log.i("checkReportistica", "Nome del file che DEVO inviare PER CICLISCANNER ->"+fileName3);
                    //Log.i("checkReportistica", "Contenuto del FILE ->"+fileName3);
                    //readFile(new File(fileName3));
                    //Mando i file via FTP
                    boolean flagB = sendFileWithFTP(fileName3,file3); //Invio reportCicliScanner.lm
                    //Log.i("DBSQL_", "Stato CicloScanner -> "+flagB);
                    if(flagB){// Se file inviato
                        if(databaseHelper == null){
                            databaseHelper = new DatabaseHelper(SendBeaconService.sendBeaconService);
                        }
                        //Aggiorno su DB il bit di colonna CicloScanner
                        //Aggiorno dato della colonna CicloScanner nel record che data = yesterday
                        databaseHelper.updateReportistica(Reportistica.COLUMN_CICLOSCANNER,1,new String[]{Reportistica.COLUMN_DATA,yesterday});
                        databaseHelper.close();
                        //Log.i("DBSQL_", "Aggiornato il valore CICLISCANNER nel record -> "+i);
                    }else{Log.e("checkReportistica", "Attenzione!! File CICLISCANNER non inviato ");}
                }
            }else{Log.e("checkReportistica", "File CICLISCANNER già inviato ");}
        }
    }

    private static void checkDeleteFilesLastMonth(){
        String lastMonth = getLastMonth();//Ottengo la data di ieri
        Log.i("checkDFLM","Mese precedente -> "+lastMonth);
        //Leggo la cartella dove sono presenti i file
        File[] listOfFiles = myDir.listFiles();
        for (File listOfFile : listOfFiles) {
            //Log.i("checkDFLM","Lista File -> ");
            Log.i("checkDFLM", "File -> " + listOfFile.getName());
            if (listOfFile.isFile()) {
                Log.i("checkDFLM", "File presente in cartella -> " + listOfFile.getName());

                //Codice di prova
                Log.i("checkDFLM", "Size del File -> " + Integer.parseInt(String.valueOf(listOfFile.length())) + " Byte");

                //File sdcard = Environment.getExternalStorageDirectory();
                //File file = new File(sdcard,"file.txt");
                //StringBuilder text = new StringBuilder();
/*
                try {
                    BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
                    String line;

                    while ((line = br.readLine()) != null) {
                        //text.append(line);
                        //text.append('\n');
                        Log.i("checkDFLM","Leggo Riga -> "+line);
                    }
                    br.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }
*/

                Log.i("checkDFLM", "indexOf(lastMonth) -> " + listOfFile.getName().indexOf(lastMonth));
                //SE valore = -1 non è presente SE valore != -1
                if (listOfFile.getName().indexOf(lastMonth) != -1) { //SE valore != -1
                    Log.i("checkDFLM", "Cancello il File Blocchi -> " + listOfFile.getName());
                    listOfFile.delete();
                } else {//SE valore = -1
                    //File da non cancellare
                    Log.i("checkDFLM", "Non Cancello File -> " + listOfFile.getName());
                }
            }
            /*else if (listOfFiles[i].isDirectory()) {
                //Log.i("checkDFLM","Directory -> "+listOfFiles[i].getName());
            }*/
        }
    }

    private static String getLastMonth(){
        final Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) +1; //Mese attuale
        int year = cal.get(Calendar.YEAR);
        //Log.i("getLastMonth",month+"-"+year);
        if(month == 1){month = 12;year -= 1;}
        else {month -= 1;}
        return month+"-"+year;
    }

    private static boolean checkNextDay(String dataN, String dataY){
        boolean flag = false;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date d1 = sdf.parse(dataN);
            //Log.i("DBSQL_checkNextDay","Data Now "+d1);
            Date d2 = sdf.parse(dataY);
            //Log.i("DBSQL_checkNextDay","Data Last "+d2);
            // Calculates the difference in milliseconds.
            long days = (Math.abs(d1.getTime() - d2.getTime()) / (24 * 3600 * 1000));
            //Log.i("DBSQL_checkNextDay","Differenza di giorni: "+days);
            if(days > 0){flag = true;}
        } catch (ParseException e) {
            Log.e("DBSQL_checkNextDay","Impossibile fare la conversione "+e);
            e.printStackTrace();
        }
        return flag;
    }

    private static Date getYesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    private static String getYesterdayDateString() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(getYesterday());
    }

    public static boolean sendFileWithFTP(String fileName, File file){
        boolean flag = false;
        //Log.i("DBSQL_sendFileWithFTP", "Sono dentro al metodo sendFileWithFTP()");
        //Log.i("DBSQL_sendFileWithFTP","Nome del file che voglio inviare è -> "+fileName);
        flag = FtpServer.uploadFile(fileName,file);
        //Log.i("DBSQL_sendFileWithFTP", "Stato del File inviato -> "+flag);
        return flag;
    }
/*
    private static void readListFiles(File listaFile){
        //Log.i("readListFiles","Leggo CARTELLA");
        File[] listOfFiles = listaFile.listFiles();
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                //Log.i("readListFiles","Nome Path -> "+listOfFiles[i].getAbsolutePath());
                //Log.i("readListFiles","Nome FIlE -> "+listOfFiles[i].getName());
                //readFile(listOfFiles[i]);
            }
        }
    }
*/
    public static File getMyDir() {
        //return sendBeaconService.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return myDir;
    }

}