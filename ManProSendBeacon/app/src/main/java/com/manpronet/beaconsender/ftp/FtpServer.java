package com.manpronet.beaconsender.ftp;

import com.manpronet.beaconsender.dati.Costanti;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FtpServer{

    public static boolean uploadFile(String fileName, File file) {
        boolean flag = false;
        FTPClient ftp = new FTPClient();
        //Log.i("CICLO 5 MINUTI","Host -> "+Costanti.hostFTP);
        //Log.i("CICLO 5 MINUTI","User FTP ->"+Costanti.userFTP);
        //Log.i("CICLO 5 MINUTI","Password FTP ->"+Costanti.passwordFTP);
        //Log.i("CICLO 5 MINUTI","Path FTP ->"+Costanti.pathFTP);
        try {
            ftp.connect(Costanti.hostFTP);
            ftp.login(Costanti.userFTP, Costanti.passwordFTP);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.makeDirectory(Costanti.pathFTP);//Costruisco il percorso
            //Log.i("CICLO 5 MINUTI","makeDirectory FTP -> "+m);
            ftp.changeWorkingDirectory(Costanti.pathFTP);//Mi posiziono nel percorso
            //Log.i("CICLO 5 MINUTI","changeWorkingDirectory FTP -> "+c);
            //Log.i("CICLO 5 MINUTI","Stato della connessione FTP -> "+ftp.getStatus());
            //Log.i("CICLO 5 MINUTI","Nome del file che voglio madare in FTP -> "+fileName);
            int reply = ftp.getReplyCode();
            //Log.i("CICLO 5 MINUTI", "Valore reply -> "+reply);
            if (FTPReply.isPositiveCompletion(reply)) {
                //Log.i("CICLO 5 MINUTI", "Mando file via FTP");
                FileInputStream in = new FileInputStream(file);
                flag = ftp.storeFile(fileName, in);
            }
            ftp.logout();
            ftp.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.i("CICLO 5 MINUTI", "Esecuzione invio FTP -> "+flag);
        return flag;
    }
}