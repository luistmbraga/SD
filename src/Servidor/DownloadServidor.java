package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloadServidor implements Runnable{

    private PedidosDownload pedidos;

    public DownloadServidor(PedidosDownload pedidos){
        this.pedidos = pedidos;
    }

    public void run() {
        ServerSocket ss;
        try {
            ss = new ServerSocket(3001);

            Socket cs;

        while(true){

            cs = ss.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            String msg = in.readLine();
            String[] args = msg.split(":");
            this.pedidos.add(Long.parseLong(args[0]), args[1],cs);

        }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
