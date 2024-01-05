package com.manpronet.beaconsender.dati;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Costanti {
    public static String android_id;
    //public static int timeScanSec = 30; // 30 secondi //Scansione dei Beacons
    public static int timeSendSec = 300; // 5 minuti = 300 secondi || per sviluppo settare a 1 minuto = 60 //Invio dei dati via FTP
    //public static int timeErrorSec = 180;// 3 minuti = 180 secondi || per controllom reportBlocchi
    //public static int timeUpdateSec = 600; // 10 minuti = 600 secondi || per sviluppo settare a 1 minuto e mezzo = 90
    //public static int timeFinishAppSec = 900;// 15 minuti = 900 secondi || per terminare App e farla ripartire
    //public static int timeDatiTelSec = 1200;// 20 minuti = 1200 secondi || per inviare le informazioni dell'App ad ALLODI
    public static int versionCode; //Codice della versione dell'applicazione
    public static String versionName;
    public  static int maxSizeFile = 22;// Massima dimensione del file da allegare, espresso in MB
    //public static String host = "https://manpronet.com"; //185.63.229.16
    //public static String hostIP = "185.63.229.16"; //185.63.229.16
    public static String host_sviluppo = "https://sviluppo.manpronet.com"; //185.63.229.15
    public static String host = "https://manpronet.com";
    public static String port = ":8089";
    public static String attracco_android = "/attracco_android/risolutore_indirizzo/json-events-login.php";
    public static String ricezione_beacon_utilizzatori = "/app_mobile/ricezione_beacon_utilizzatori.php";
    public static String ricezione_beacon_login_lingue = "/app_mobile/ricezione_beacon_login_lingue.php";
    //public static String host = "https://manpronet.com";
    //public static String host_sviluppoIP = "185.63.229.15"; //185.63.229.15
    public static int portSocket = 8082; //Porta di connesione al socket
    //public static int portReportBlocco = 8085; //Porta di connesione al socket

    //Credenziali per collegamento FTP
    public static String hostFTP = "ftp.natisoft.it";
    public static String pathFTP = "/natisoft.it/serviceiBeaconOne/";
    public static String userFTP = "1506303@aruba.it";
    public static String passwordFTP = "solealto2015";

    public static String https = "https://"; //prefisso da aagiungere 'eventualmente' alla voce Indirizzo Portale

    public static String extractDomain(String url){
        //String d = "";
        String[] parts = url.split("/mobile/");
        if(parts[0].contains("www.")){
            return parts[0].replace("www.","");
        }
        else{
            return parts[0];
        }
    }

    public static String getIndirizzoPortale(Context ctxt) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return preferences.getString("pref_indirizzoInvio", ""/*host_sviluppo*/);
    }

    public static String checkPortalAddress(String dominio,Context ctxt){
        //Log.d("Costanti", "dominio -> " + dominio);
        String dom;
        //if dominio > 0 -> controllo sintassi
        if (dominio.length() > 0){
            String path = getIndirizzoPortale(ctxt);
            //Log.d("Costanti", "path -> " + path);
            if(path.contains(https)){
                dom =  path;
            }
            else{
                dom = https + path;
            }
            //Log.i("URLxINVIO","Se preferenza dominio è presente -> " + dom);
        }
        //else dominio == 0 -> mando sviluppo
        else {
            dom = Costanti.host_sviluppo;
            //Log.i("URLxINVIO","Se la stringa di dominio è vuota diventa -> " + dom);
        }

        dom += ":8089";
        //Log.d("Costanti", "dom -> " + dom);
        return dom;
    }
}