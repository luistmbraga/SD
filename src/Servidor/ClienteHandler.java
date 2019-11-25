package Servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class ClienteHandler implements Runnable {

    private Socket cs;
    private PrintWriter out;
    private BufferedReader in;
    private SoundCloud soundCloud;
    private Utilizador userAtual;

    private long musicaASerRecebidaId;
    private String titulo;
    private String interprete;
    private String ano;
    private List<String> tags;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private String pastaDB = "C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\";

    public ClienteHandler(Socket cs, SoundCloud sounds) throws IOException {
        this.cs = cs;
        this.soundCloud = sounds;
    }

    public void limparCampos() throws IOException {
        this.tags.clear();
        this.fos.close();
        this.bos = null;
    }


    public void comandos(String msg) throws IOException, InterruptedException {


        if (msg == null){
            return;
        }
        // LOGIN:username:pass
        if (msg.startsWith("LOGIN")){
            String[] args = msg.split(":");

            if((this.userAtual = this.soundCloud.login(args[1],args[2], this.cs))!=null){
                this.out.println("ACCESSGRANTED");
            }
            else this.out.println("WARNING:Dados inválidos.");

        }
        else if(msg.startsWith("LOGOUT")){
            this.userAtual.logout(); // desnecessario
        }
        // REGISTAR:username:pass
        else if(msg.startsWith("REGISTAR")){
            String[] args = msg.split(":");

            if (this.soundCloud.registarUser(args[1],args[2])){
                this.out.println("SUCCESS:Utilizador registado com sucesso.");
            }
            else this.out.println("WARNING:Username já utilizado.");
        }
        // DOWNLOAD:id
        else if(msg.startsWith("DOWNLOAD")){
            String[] args = msg.split(":");
            String tit;
            Long id = Long.parseLong(args[1]);
            if ((tit = this.soundCloud.downloadOk(id)) != null){
                this.out.println("OKDOWNLD:"+tit);
                this.soundCloud.download(id, this.cs);
                this.out.println("FILEND");
            }
            else this.out.println("WARNING:Id fornecido não existe.");
        }
        // UPLOAD:titulo:interprete:ano:tag1,tag2,tag3...
        else if(msg.startsWith("UPLOAD")){
            String[] args = msg.split(":");
            this.musicaASerRecebidaId = this.soundCloud.incMusicId();
            this.titulo = args[1];
            this.interprete = args[2];
            this.ano = args[3];
            this.tags = new ArrayList<>();
            String[] alltags = args[4].split(",");
            for (String r : alltags)
                this.tags.add(r);

            this.fos = new FileOutputStream(this.pastaDB+this.musicaASerRecebidaId+".mp3"); // retirar
            this.bos = new BufferedOutputStream(fos);
        }
        else if(msg.startsWith("FILEND")){
            this.bos.flush();
            this.soundCloud.upload(this.musicaASerRecebidaId, this.titulo, this.interprete, this.ano, this.tags);
            limparCampos();
            this.soundCloud.notifyAllUsers(this.titulo, this.interprete); // é o interprete ou o username
        }
        else if (msg.startsWith("SEARCH")){
            String[] args = msg.split(":");
            this.out.println("SEARCHRESULT:"+this.soundCloud.pesquisarMusica(args[1]));
        }
        else {
            byte[] contents;
            contents = Base64.getDecoder().decode(msg);   // tirar mais tarde
            this.bos.write(contents, 0, contents.length);
        }

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
            e.printStackTrace();
            System.out.println("Um cliente saiu abruptamente.");
        }

    }
}
