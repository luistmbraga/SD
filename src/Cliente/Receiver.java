package Cliente;

import Servidor.DownloadMaker;

import java.io.*;
import java.net.Socket;


public class Receiver implements Runnable{

    private Socket cs;

    private ClienteState cls;

    private BufferedReader in;

    private String username;

    public Receiver(Socket cs, ClienteState cls) throws IOException {
        this.cs = cs;
        this.cls = cls;
        this.in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
    }

    public void run(){
        String msg;

        try {

            while ((msg = this.in.readLine())!=null) {

                if (msg.startsWith("SUCCESS") || msg.startsWith("WARNING") || msg.startsWith("SEARCHRESULT") || msg.startsWith("UPLOAD_ID")){
                    System.out.println(msg);
                    this.cls.finishWaiting();
                }
                else if (msg.startsWith("NOTF")){
                    System.out.println(msg);
                }
                else if (msg.startsWith("ACCESSGRANTED")){
                    String[] args = msg.split(":");
                    this.username = args[1];
                    this.cls.login();
                    this.cls.finishWaiting();
                }
                else if (msg.startsWith("OKDOWNLD")){
                    String[] args = msg.split(":");

                    // encaminhar pedido para o downloader

                    Thread t = new Thread(new Downloader(args[2], args[1], this.username));
                    t.start();

                }
            }

        } catch (IOException e) {
            this.cls.logout();
            System.out.println("Bye");
        }


    }
}
