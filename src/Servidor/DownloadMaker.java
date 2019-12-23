package Servidor;

import java.io.IOException;
import java.net.Socket;

public class DownloadMaker implements Runnable{

    private Musicas musicas;
    private PedidosDownload pedidosDownload;
    private Socket cs;
    private long musicId;

    public DownloadMaker(Musicas musicas, PedidosDownload pedidosDownload, Socket cs, long musicId){
        this.musicas = musicas;
        this.pedidosDownload = pedidosDownload;
        this.cs = cs;
        this.musicId = musicId;
    }

    public void run() {
        try {

            // faz download
            this.musicas.download(this.musicId, this.cs);

            // decrementa o numero de donwloads a ocorrer
            this.pedidosDownload.notifyWaitDownload();


        } catch (IOException e) {

            // enviar erro ao cliente

            e.printStackTrace();
        }

    }
}
