package Cliente;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class Receiver implements Runnable{

    private Socket cs;

    private ClienteState cls;

    private BufferedReader in;

    private String downloadMusicPath;

    private String pastaDB = System.getProperty("user.home") + "\\Downloads\\";

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    public Receiver(Socket cs, ClienteState cls) throws IOException {
        this.cs = cs;
        this.cls = cls;
        this.in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
    }

    public void limparCampos() throws IOException {
        this.fos.close();
        this.bos = null;
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
                    this.cls.login();
                    this.cls.finishWaiting();
                }
                else if (msg.startsWith("OKDOWNLD")){
                    String[] args = msg.split(":");
                    // verificar se já existe o ficheiro
                    //this.downloadMusicPath = this.pastaDB+args[1];

                    File file = new File(this.pastaDB+args[1]);

                    for (int i = 1; file.exists(); i++){
                        file = new File(this.pastaDB+"("+i+")"+args[1]);
                    }

                    this.fos = new FileOutputStream(file);
                    this.bos = new BufferedOutputStream(fos);
                }
                else if (msg.startsWith("FILEND")){
                    this.bos.flush();
                    limparCampos();
                    this.cls.finishWaiting();
                }
                else {
                    byte[] contents;
                    contents = Base64.getDecoder().decode(msg);
                    this.bos.write(contents, 0, contents.length);
                }
            }

        } catch (IOException e) {
            this.cls.logout();
            System.out.println("Bye");
        }


    }
}
