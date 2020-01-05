package Servidor;

import java.io.*;
import java.net.Socket;


public class ClienteHandler implements Runnable {

    private Socket cs;
    private PrintWriter out;
    private BufferedReader in;
    private Musicas musicas;
    private Clientes clientes;
    private Utilizador userAtual;

    public ClienteHandler(Socket cs, Musicas musicas, Clientes clientes) throws IOException {
        this.cs = cs;
        this.musicas = musicas;
        this.clientes = clientes;
        this.userAtual = null;
    }

    private void comandos(String msg) throws IOException, InterruptedException {


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
        else if (msg.startsWith("SEARCH")){
            comando_search(msg);
        }

    }

    private void comando_search(String msg) {
        String[] args = msg.split(":");
        this.out.println("SEARCHRESULT:"+this.musicas.pesquisarMusica(args[1]));
    }

    private void comando_download(String msg) {
        String[] args = msg.split(":");
        Long id = Long.parseLong(args[1]);
        String tit;
        if ((tit = this.musicas.downloadOk(id)) != null){
            this.out.println("OKDOWNLD:"+id+":"+tit);   // enviar id e titulo
        }
        else this.out.println("WARNING:Id - "+id+" fornecido não existe.");
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
            this.out.println("ACCESSGRANTED:"+args[1]);
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
                comandos(msg);
            }

            if(this.userAtual != null) this.userAtual.logout();
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
