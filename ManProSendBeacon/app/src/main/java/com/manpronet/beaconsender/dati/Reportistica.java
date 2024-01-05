package com.manpronet.beaconsender.dati;

public class Reportistica {

    public static final String TABLE_NAME = "Reportistica";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_BLOCCO = "reportBlocco";
    public static final String COLUMN_CONNETTIVIA = "reportConnettivita";
    public static final String COLUMN_CICLOSCANNER = "reportCicloScanner";
    public static final String COLUMN_DATA = "Data";

    private int id;
    private int blocco;
    private int connettivita;
    private int cicloScanner;
    private String data;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + "(" +
                    COLUMN_ID +" integer PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_BLOCCO +" integer," +
                    COLUMN_CONNETTIVIA +" integer," +
                    COLUMN_CICLOSCANNER +" integer," +
                    COLUMN_DATA +" DATE)";

    public Reportistica(int id, int blocco, int connettivita, int cicloScanner, String data){
        this.id = id;
        this.blocco = blocco;
        this.connettivita = connettivita;
        this.cicloScanner = cicloScanner;
        this.data = data;
    }

    public int getBlocco() {
        return blocco;
    }

    public int getConnettivita() {
        return connettivita;
    }

    public String getData() {
        return data;
    }

    public int getCicloScanner() {
        return cicloScanner;
    }

    @Override
    public String toString() {
        return "Reportistica{" +
                "id=" + id +
                ", blocco=" + blocco +
                ", connettivita=" + connettivita +
                ", cicloScanner=" + cicloScanner +
                ", data='" + data + '\'' +
                '}';
    }
}
