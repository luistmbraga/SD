package Servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClienteHandler implements Runnable {

    private Socket cs;
    private PrintWriter out;
    private BufferedReader in;
    private Musicas musicas;
    private Clientes clientes;
    private Utilizador userAtual;

    // Dados relativos a um upload
    private long musicaASerRecebidaId;
    private String titulo;
    private String interprete;
    private String ano;
    private List<String> tags;
    private String extensao;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private String pastaDB = "C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\";

    private String extensionRegex = "(.3gp$|.aa$|.aac$|.aax$|.act$|.aiff$|.alac$|.amr$|.ape$|.au$|." +
            "awb$|.dct$|.dss$|.dvf$|.flac$|.gsm$|.iklax$|.ivs$|.m4a$|.m4b$|.m4p$|.mmf$|.mp3$|." +
            "mpc$|.msv$|.nmf$|.nsf$|.ogg$|.oga$|.mogg$|.opus$|.ra$|.rm$|.raw$|.sln$|.tta$|.voc$|." +
            "vox$|.wav$|.wma$|.wv$|.webm$|.8svx$)";

    public ClienteHandler(Socket cs, Musicas musicas, Clientes clientes) throws IOException {
        this.cs = cs;
        this.musicas = musicas;
        this.clientes = clientes;
    }

    public String isAudioFile(String file){
        String r = null;
        Pattern pattern =
                Pattern.compile(this.extensionRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(file);

        if (m.find()){
            r = file.substring(m.start(), m.end());
        }

        return r;
    }

    public void limparCampos() throws IOException {
        this.tags.clear();
        this.fos.close();
        this.bos = null;
        this.interprete = null;
        this.titulo = null;
        this.ano = null;
        this.musicaASerRecebidaId = -1;
        this.extensao = null;
    }


    public void comandos(String msg) throws IOException, InterruptedException {


        if (msg == null){
            return;
        }
        // LOGIN:username:pass
        if (msg.startsWith("LOGIN")){
            comando_login(msg);
        }
        // REGISTAR:username:pass
        else if(msg.startsWith("REGISTAR")){
            comando_registar(msg);
        }
        else if (msg.startsWith("LOGOUT")){
            this.userAtual.logout();
        }
        // DOWNLOAD:id
        else if(msg.startsWith("DOWNLOAD")){
            comando_download(msg);
        }
        // UPLOAD:titulo:interprete:ano:tag1,tag2,tag3...:extensao
        else if(msg.startsWith("UPLOAD")){
            comando_upload(msg);
        }
        else if(msg.startsWith("FILEND")){
            comando_filend();
        }
        else if (msg.startsWith("SEARCH")){
            comando_search(msg);
        }
        else {
            byte[] contents;
            contents = new byte[msg.getBytes().length];
            //contents = msg.getBytes();
            contents = Base64.getDecoder().decode(msg);   // tirar mais tarde
            this.bos.write(contents, 0, contents.length);
        }

    }

    private void comando_search(String msg) {
        String[] args = msg.split(":");
        this.out.println("SEARCHRESULT:"+this.musicas.pesquisarMusica(args[1]));
    }

    private void comando_filend() throws IOException {
        this.bos.flush();
        this.musicas.upload(this.musicaASerRecebidaId, this.titulo, this.interprete, this.ano, this.tags, this.extensao);
        this.out.println("UPLOAD_ID:" + this.musicaASerRecebidaId);
        Thread notify = new Thread(new NotifyUsersThread(this.clientes, this.titulo, this.interprete)); // é o interprete ou o username
        notify.start();
        limparCampos();
    }

    private void comando_upload(String msg) throws FileNotFoundException {
        String[] args = msg.split(":");
        this.musicaASerRecebidaId = this.musicas.incMusicId();
        this.titulo = args[1];
        this.interprete = args[2];
        this.ano = args[3];
        this.tags = new ArrayList<>();
        String[] alltags = args[4].split(",");
        for (String r : alltags)
            this.tags.add(r);

        this.extensao = args[5];
        this.fos = new FileOutputStream(this.pastaDB+this.musicaASerRecebidaId+this.extensao); // retirar
        this.bos = new BufferedOutputStream(fos);
    }

    private void comando_download(String msg) {
        String[] args = msg.split(":");
        String tit;
        Long id = Long.parseLong(args[1]);
        if ((tit = this.musicas.downloadOk(id)) != null){
            this.out.println("OKDOWNLD:"+tit);
            this.musicas.download(id, this.cs, isAudioFile(tit));
            this.out.println("FILEND");
        }
        else this.out.println("WARNING:Id fornecido não existe.");
    }

    private void comando_registar(String msg) {
        String[] args = msg.split(":");

        if (this.clientes.registarUser(args[1],args[2])){
            this.out.println("SUCCESS:Utilizador registado com sucesso.");
        }
        else this.out.println("WARNING:Username já utilizado.");
    }

    private void comando_login(String msg) {
        String[] args = msg.split(":");

        if((this.userAtual = this.clientes.login(args[1],args[2], this.cs))!=null){
            this.out.println("ACCESSGRANTED");
        }
        else this.out.println("WARNING:Dados inválidos.");
    }


    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.cs.getInputStream()));
            this.out = new PrintWriter(this.cs.getOutputStream(), true);
            String msg;
            while (( msg = this.in.readLine())!=null){

                System.out.println("Recebi a seguinte mensagem: " +msg); /////////////////
                comandos(msg);
            }

            this.userAtual.logout();
            this.cs.shutdownOutput();
            this.cs.shutdownInput();
            this.cs.close();

            System.out.println("Um cliente acabou de sair.");

        } catch (IOException | InterruptedException e) {
            this.userAtual.logout();
            System.out.println("Um cliente saiu abruptamente.");
        }

    }
}
