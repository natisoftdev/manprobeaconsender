package com.manpronet.beaconsender.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.manpronet.beaconsender.dati.Reportistica;

public class DatabaseHelper extends SQLiteOpenHelper {
    //information of database
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "serviceiBeaconOne.db";

    /**
     * Costruttore DatabaseHelper
     * @param context il contesto dell'applicazione
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create table
        db.execSQL(Reportistica.CREATE_TABLE);
    }

    //DA IMPLEMENTARE
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion){
            db.execSQL("ALTER TABLE " + Reportistica.TABLE_NAME + " ADD COLUMN " + Reportistica.COLUMN_CICLOSCANNER + " TEXT DEFAULT ''");
        }
    }

    //DA IMPLEMENTARE
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
    }

    public long insertRecord(String blocco, String connettivita, String cicloScanner, String data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Reportistica.COLUMN_BLOCCO, blocco);
        values.put(Reportistica.COLUMN_CONNETTIVIA, connettivita);
        values.put(Reportistica.COLUMN_CICLOSCANNER, cicloScanner);
        values.put(Reportistica.COLUMN_DATA, data);

        // insert row
        long id = db.insert(Reportistica.TABLE_NAME, null, values);
        // close db connection
        db.close();
//        this.close();
        // return newly inserted row id
        return id;
    }

    /**
     * Metodo che mi restituisce l'oggetto Reportistica
     * @param data del record
     * @return oggetto con i dati
     */
    public Reportistica getReportistica(String data){
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        //Costruisco la query per ottenere l'eventuale valore -> SELECT * FROM 'Reportistica' WHERE Data = '13-01-2019'
        Cursor cursor = db.rawQuery("SELECT * FROM 'Reportistica' WHERE "+Reportistica.COLUMN_DATA+" = '"+data+"'",null);
        //Stampo un numero intero che rappresenta i record presenti nel DB
        //Log.i("DBSQL_getReportistica","Numero di record presenti nel DB ->"+cursor.getCount());
        if (cursor != null) cursor.moveToFirst();
        // prepare note object
        assert cursor != null;
        Reportistica reportistica = new Reportistica(
            cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_ID)),
            cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_BLOCCO)),
            cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CONNETTIVIA)),
            cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CICLOSCANNER)),
            cursor.getString(cursor.getColumnIndex(Reportistica.COLUMN_DATA))
        );
        // close the db connection
        cursor.close();
        db.close();
//        this.close();
        return reportistica;
        //return null;
    }

    /**
     * Metodo che data una data mi dice se quel valore è già presente  nel DB
     * @param data data in formato dd-mm-yyyy
     * @param where a cosa devo confrontare
     * @return true se è presente || false se non è presente
     */
    public boolean getReportistica(String data,String where){
        boolean flag;
        //Log.i("DBSQL_getReportistica","Sono dentro");
        //Log.i("DBSQL_getReportistica","Data da inserire -> "+data);
        //Log.i("DBSQL_getReportistica","Colonna dove inserire -> "+where);
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        //Costruisco la query per ottenere l'eventuale valore -> SELECT * FROM 'Reportistica' WHERE Data = '13-01-2019'
        Cursor cursor = db.rawQuery("SELECT * FROM 'Reportistica' WHERE "+where+" = '"+data+"'",null);
        //Stampo un numero intero che rappresenta i record presenti nel DB
        //Log.i("DBSQL_getReportistica","Numero di record presenti nel DB ->"+cursor.getCount());
        flag = cursor.getCount() > 0;
        // close the db connection
        cursor.close();
        db.close();
//        this.close();
        //Log.i("DBSQL_getReportistica","Cosa mando alla classe principale -> "+flag);
        return flag;
    }

    /**
     * Metodo che mi restituisce l'ultimo oggetto Reportistica presente nel DB
     * @return oggetto REportistica
     */
    public Reportistica getLastReportistica(){
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        //Costruisco la query per ottenere l'eventuale valore -> SELECT * FROM 'Reportistica' WHERE Data = '13-01-2019'
        Cursor cursor = db.rawQuery("SELECT * FROM 'Reportistica' ORDER BY "+Reportistica.COLUMN_ID+" DESC",null);
        if (cursor != null) cursor.moveToFirst();
        // prepare note object
        assert cursor != null;
        Reportistica reportistica = new Reportistica(
                cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_ID)),
                cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_BLOCCO)),
                cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CONNETTIVIA)),
                cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CICLOSCANNER)),
                cursor.getString(cursor.getColumnIndex(Reportistica.COLUMN_DATA))
        );
        // close the db connection
        cursor.close();
        db.close();
//        this.close();
        return reportistica;
    }

/*
    public int getReportisticaCount() {
        String countQuery = "SELECT  * FROM " + Reportistica.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

    public List<Reportistica> getAllReportistica(){
        List<Reportistica> arrayReport = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Reportistica.TABLE_NAME + " ORDER BY " +
                Reportistica.COLUMN_DATA + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Reportistica reportistica = new Reportistica();
                reportistica.setId(cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_ID)));
                reportistica.setBlocco(cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_BLOCCO)));
                reportistica.setConnettivita(cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CONNETTIVIA)));
                reportistica.setCicloScanner(cursor.getInt(cursor.getColumnIndex(Reportistica.COLUMN_CICLOSCANNER)));
                reportistica.setData(cursor.getString(cursor.getColumnIndex(Reportistica.COLUMN_DATA)));

                arrayReport.add(reportistica);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return arrayReport;
    }
*/

    public int updateReportistica(String nomeColonna, int valore, String where[]) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //Log.i("updateReportistica","------------------------------------");
        //Log.i("updateReportistica","Nome della colonna -> "+nomeColonna);
        //Log.i("updateReportistica","Valore che voglio inserire -> "+valore);
        //Log.i("updateReportistica","WHERE "+where[0]+" = "+where[1]);
        //Log.i("updateReportistica","------------------------------------");
        // associo a colonna il valore
        values.put(nomeColonna, valore);
        // updating row
        return db.update(Reportistica.TABLE_NAME, values,where[0] + " = '"+where[1]+"'",null);
    }

/*
    public void deleteReportistica(Reportistica reportistica) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Reportistica.TABLE_NAME, Reportistica.COLUMN_ID + " = ?",
                new String[]{String.valueOf(reportistica.getId())});
        db.close();
    }
*/
    public void deleteAllDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Reportistica.TABLE_NAME,null,null);
        db.close();
//        this.close();
    }
}