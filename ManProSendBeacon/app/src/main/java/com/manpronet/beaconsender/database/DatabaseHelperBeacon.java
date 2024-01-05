package com.manpronet.beaconsender.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.manpronet.beaconsender.dati.Beacon;
import com.manpronet.beaconsender.dati.Reportistica;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelperBeacon extends SQLiteOpenHelper {
    private final static String TAG = "DatabaseHelperBeacon";

    //information of database
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "BeaconRilevamenti.db";
    //Nome della tabella
    private static final String TABLE_NAME = "BeaconRilevati";
    //Indice autoincrementale
    public static final String COLUMN_ID = "ID";
    //Valori importanti
    public static final String COLUMN_DB_CONNECT = "db_connect";
    public static final String COLUMN_UTENTE = "utente";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_DATACAMPIONAMENTO = "dataCampionamento";
    public static final String COLUMN_ANDROID = "android";
    public static final String COLUMN_LIVELLOBATTERIA = "livelloBatteria";
    public static final String COLUMN_TEMPOCAMPIONAMENTO = "tempoCampionamento";
    public static final String COLUMN_NUMEROVERSIONE = "numeroVersione";
    //Valori del Beacon
    public static final String COLUMN_BEACON_MAC = "mac";
    public static final String COLUMN_BEACON_DISTANCE = "distance";
    public static final String COLUMN_BEACON_TXPOWER = "txpower";
    public static final String COLUMN_BEACON_RSSI = "rssi";
    //Bit di rilevamento 0 == non inviato || 1 == inviato
    public static final String COLUMN_BIT = "bit";
    //Stringa che rappresenta il telefono
    public static final String COLUMN_ANDROID_ID = "ANDROID_ID";

    // Create table SQL query
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + "(" +
                    COLUMN_ID +" integer PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DB_CONNECT +" text, " +
                    COLUMN_UTENTE +" text, " +
                    COLUMN_PASSWORD +" text, " +
                    COLUMN_MODEL +" text, " +
                    COLUMN_DATACAMPIONAMENTO +" text, " +
                    COLUMN_ANDROID +" integer, " +
                    COLUMN_LIVELLOBATTERIA +" integer," +
                    COLUMN_TEMPOCAMPIONAMENTO +" integer, " +
                    COLUMN_NUMEROVERSIONE +" integer, " +
                    COLUMN_BEACON_MAC +" text, " +
                    COLUMN_BEACON_DISTANCE +" text, " +
                    COLUMN_BEACON_TXPOWER +" integer, " +
                    COLUMN_BEACON_RSSI +" integer, " +
                    COLUMN_BIT +" integer, " +
                    COLUMN_ANDROID_ID +" text " +
                    ")";

    /**
     * Costruttore DatabaseHelperBeacon
     * @param context il contesto dell'applicazione
     */
    public DatabaseHelperBeacon(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create table
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_ANDROID_ID + " TEXT DEFAULT ''");
        }
    }

    public long insertRecord(Beacon beacon){
        SQLiteDatabase db = this.getWritableDatabase();
        //SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        //qb.setTables(TABLE_NAME);
        ContentValues values = new ContentValues();

        values.put(COLUMN_DB_CONNECT, beacon.getDb_connect());
        values.put(COLUMN_UTENTE, beacon.getUtente());
        values.put(COLUMN_PASSWORD, beacon.getPassword());
        values.put(COLUMN_MODEL, beacon.getModel());
        values.put(COLUMN_DATACAMPIONAMENTO, beacon.getDatacampionamento());
        values.put(COLUMN_ANDROID, beacon.getAndroid());
        values.put(COLUMN_LIVELLOBATTERIA, beacon.getLivelloBatteria());
        values.put(COLUMN_TEMPOCAMPIONAMENTO, beacon.getTempocampionamento());
        values.put(COLUMN_NUMEROVERSIONE, beacon.getNumeroversione());
        values.put(COLUMN_BEACON_MAC, beacon.getMac());
        values.put(COLUMN_BEACON_DISTANCE, beacon.getDistance());
        values.put(COLUMN_BEACON_TXPOWER, beacon.getTxpower());
        values.put(COLUMN_BEACON_RSSI, beacon.getRssi());
        values.put(COLUMN_BIT, beacon.getBit());
        values.put(COLUMN_ANDROID_ID, beacon.getAndroid_id());

        // insert row
        long id = db.insert(TABLE_NAME, null, values);
        // close db connection
        db.close();
        this.close();
        // return newly inserted row id
        return id;
    }
/*
    public int updateRecord(String nomeColonna, int valore, String where[]) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // associo a colonna il valore
        values.put(nomeColonna, valore);
        // updating row
        return db.update(TABLE_NAME, values,where[0] + " = '"+where[1]+"'",null);
    }
*/
    public List<Beacon> getBeacon(){
        List<Beacon> arrayReport = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_BIT + " = 0 ORDER BY "+COLUMN_DATACAMPIONAMENTO+" ASC";

        //Log.i("QUERY",""+selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Beacon beaconRilevati = new Beacon();

                beaconRilevati.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                beaconRilevati.setDb_connect(cursor.getString(cursor.getColumnIndex(COLUMN_DB_CONNECT)));
                beaconRilevati.setUtente(cursor.getString(cursor.getColumnIndex(COLUMN_UTENTE)));
                beaconRilevati.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                beaconRilevati.setModel(cursor.getString(cursor.getColumnIndex(COLUMN_MODEL)));
                beaconRilevati.setDatacampionamento(cursor.getString(cursor.getColumnIndex(COLUMN_DATACAMPIONAMENTO)));
                beaconRilevati.setAndroid(cursor.getInt(cursor.getColumnIndex(COLUMN_ANDROID)));
                beaconRilevati.setLivelloBatteria(cursor.getInt(cursor.getColumnIndex(COLUMN_LIVELLOBATTERIA)));
                beaconRilevati.setTempocampionamento(cursor.getInt(cursor.getColumnIndex(COLUMN_TEMPOCAMPIONAMENTO)));
                beaconRilevati.setNumeroversione(cursor.getInt(cursor.getColumnIndex(COLUMN_NUMEROVERSIONE)));
                beaconRilevati.setMac(cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_MAC)));
                beaconRilevati.setDistance(cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_DISTANCE)));
                beaconRilevati.setTxpower(cursor.getInt(cursor.getColumnIndex(COLUMN_BEACON_TXPOWER)));
                beaconRilevati.setRssi(cursor.getInt(cursor.getColumnIndex(COLUMN_BEACON_RSSI)));
                beaconRilevati.setBit(cursor.getInt(cursor.getColumnIndex(COLUMN_BIT)));
                beaconRilevati.setAndroid_id(cursor.getString(cursor.getColumnIndex(COLUMN_ANDROID_ID)));

                arrayReport.add(beaconRilevati);
            } while (cursor.moveToNext());
        }

        // close db connection
        cursor.close();
        db.close();
        this.close();
        // return notes list
        return arrayReport;
    }

    public int getBeaconCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    public void deleteRecord(String colonna, int valore) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,colonna +" = "+valore,null);
        this.close();
    }

    public void deleteAllDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,null,null);
        this.close();
    }
}