package com.manpronet.beaconsender.dati;

/**
 * @author Lorenzo Malferrari
 * Classe rappresentante i dati del Beacon cn l'implementazione dei getter setter e del toString
 */

public class Beacon {
    //Id nel DB locale
    private int id;
    //Nome del DB dove inserirò i dati
    private String db_connect;
    //Nome dell'utente
    private String utente;
    //Password dell'utente
    private String password;
    //Modello del telefono
    private String model;
    //Data del campionamento
    private String datacampionamento;
    //API del telefono
    private int android;
    //Percentuale della batteria nel momento della analisi
    private int livelloBatteria;
    //Tempo di campionamento da parte del servizio
    private int tempocampionamento;
    //Numero della versione installata sul telefono
    private int numeroversione;
    //Indirizzo MAC del dispositivo Beacon
    private String mac;
    //Distanza del dispositivio dal telefono
    private String distance;
    //Potenza del dispositivo
    private int txpower;
    //RSSI del dispositivo
    private int rssi;
    //Bit di controllo
    private int bit;
    //Tringa che identifica il telefono
    private String android_id;

    /**
     * Costruttore dell'oggetto Beacon di default
     */
    public Beacon() {}

    /**
     * Costruttore parametrico dell'oggetto Beacon
     * @param id
     * @param db_connect
     * @param utente
     * @param password
     * @param model
     * @param datacampionamento
     * @param android
     * @param livelloBatteria
     * @param tempocampionamento
     * @param numeroversione
     * @param mac
     * @param distance
     * @param txpower
     * @param rssi
     * @param bit
     * @param android_id
     */
    public Beacon(int id, String db_connect, String utente, String password, String model, String datacampionamento, int android, int livelloBatteria, int tempocampionamento, int numeroversione, String mac, String distance, int txpower, int rssi, int bit, String android_id) {
        this.id = id;
        this.db_connect = db_connect;
        this.utente = utente;
        this.password = password;
        this.model = model;
        this.datacampionamento = datacampionamento;
        this.android = android;
        this.livelloBatteria = livelloBatteria;
        this.tempocampionamento = tempocampionamento;
        this.numeroversione = numeroversione;
        this.mac = mac;
        this.distance = distance;
        this.txpower = txpower;
        this.rssi = rssi;
        this.bit = bit;
        this.android_id = android_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDb_connect() {
        return db_connect;
    }

    public void setDb_connect(String db_connect) {
        this.db_connect = db_connect;
    }

    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDatacampionamento() {
        return datacampionamento;
    }

    public void setDatacampionamento(String datacampionamento) { this.datacampionamento = datacampionamento; }

    public int getAndroid() {
        return android;
    }

    public void setAndroid(int android) {
        this.android = android;
    }

    public int getLivelloBatteria() {
        return livelloBatteria;
    }

    public void setLivelloBatteria(int livelloBatteria) {
        this.livelloBatteria = livelloBatteria;
    }

    public int getTempocampionamento() {
        return tempocampionamento;
    }

    public void setTempocampionamento(int tempocampionamento) { this.tempocampionamento = tempocampionamento; }

    public int getNumeroversione() {
        return numeroversione;
    }

    public void setNumeroversione(int numeroversione) {
        this.numeroversione = numeroversione;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "id=" + id +
                ", db_connect='" + db_connect + '\'' +
                ", utente='" + utente + '\'' +
                ", password='" + password + '\'' +
                ", model='" + model + '\'' +
                ", datacampionamento='" + datacampionamento + '\'' +
                ", android=" + android +
                ", livelloBatteria=" + livelloBatteria +
                ", tempocampionamento=" + tempocampionamento +
                ", numeroversione=" + numeroversione +
                ", mac='" + mac + '\'' +
                ", distance='" + distance + '\'' +
                ", txpower=" + txpower +
                ", rssi=" + rssi +
                ", bit=" + bit +
                ", android_id='" + android_id + '\'' +
                '}';
    }
}