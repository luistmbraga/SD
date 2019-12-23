package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class UploadServidor implements Runnable{
    private Musicas musicas;
    private Clientes clientes;

    public UploadServidor(Musicas musicas, Clientes clientes){
        this.musicas = musicas;
        this.clientes = clientes;
    }

    public void run() {

        ServerSocket ss;
        try {
            ss = new ServerSocket(3002);

            Socket cs;

            while(true){

                cs = ss.accept();

                // lancar thread
                Thread t = new Thread(new UploadMaker(cs, this.musicas, this.clientes));
                t.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
