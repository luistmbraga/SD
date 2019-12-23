package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(3000);

        Socket cs;

        Musicas musicas = new Musicas();

        Clientes clientes = new Clientes();

        PedidosDownload pedidos = new PedidosDownload();

        Thread d = new Thread(new DownloadDispatcher(pedidos, musicas));
        d.start();

        Thread downld = new Thread(new DownloadServidor(pedidos));
        downld.start();

        Thread upload = new Thread(new UploadServidor(musicas, clientes));
        upload.start();

        while(true){

            cs = ss.accept();

            Thread t = new Thread(new ClienteHandler(cs, musicas, clientes));

            t.start();
        }

    }

}
