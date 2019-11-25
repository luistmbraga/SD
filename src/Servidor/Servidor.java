package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static void main(String[] args) throws IOException {

        SoundCloud soundCloud = new SoundCloud();

        ServerSocket ss = new ServerSocket(3000);

        Socket cs;
        while(true){

            cs = ss.accept();

            Thread t = new Thread(new ClienteHandler(cs, soundCloud));

            t.start();
        }

    }

}
