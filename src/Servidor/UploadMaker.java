package Servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UploadMaker implements Runnable{
    private Socket cs;
    private Musicas musicas;
    private Clientes clientes;
    private long musicaASerRecebidaId;
    private String titulo;
    private String interprete;
    private String ano;
    private List<String> tags;
    private String extensao;

    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private String pastaDB = "C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\";

    public UploadMaker(Socket cs, Musicas musicas, Clientes clientes){
        this.cs = cs;
        this.musicas = musicas;
        this.clientes = clientes;
    }

    private void comando_upload(String msg) throws FileNotFoundException {
        String[] args = msg.split(":");
        this.musicaASerRecebidaId = this.musicas.incMusicId();
        this.titulo = args[0];
        this.interprete = args[1];
        this.ano = args[2];
        this.tags = new ArrayList<>();
        String[] alltags = args[3].split(",");
        for (String r : alltags)
            this.tags.add(r);

        this.extensao = args[4];
        this.fos = new FileOutputStream(this.pastaDB+this.musicaASerRecebidaId+this.extensao);
        this.bos = new BufferedOutputStream(fos);
    }

    private void receiveData() throws IOException {
        byte[] contents = new byte[10000];

        InputStream is = this.cs.getInputStream();

        //No of bytes read in one read() call
        int bytesRead;

        while((bytesRead=is.read(contents))!=-1){
            bos.write(contents, 0, bytesRead);
            bos.flush();
        }
    }

    public void run() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            String msg = in.readLine();
            System.out.println(msg);
            comando_upload(msg);

            receiveData();

            this.musicas.upload(this.musicaASerRecebidaId, this.titulo, this.interprete, this.ano, this.tags, this.extensao);

            Thread notify = new Thread(new NotifyUsersThread(this.clientes, this.titulo, this.interprete)); // é o interprete ou o username
            notify.start();

            this.bos.close();

            PrintWriter pw = new PrintWriter(this.cs.getOutputStream(), true);
            pw.println("UPLOAD_ID:" + this.musicaASerRecebidaId);

            this.cs.shutdownInput();
            this.cs.shutdownOutput();
            this.cs.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
