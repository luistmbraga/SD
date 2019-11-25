package Servidor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SoundCloud {

    private Map<String, Utilizador> clientes;
    private Map<Long, Musica> musicas;
    private Lock lock;
    private Condition waitDownload;
    int MAXDOWN = 3;

    private long musicId;

    private String pastaBD = "C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\";

    private int numDownloads;

    public SoundCloud(){
        this.clientes = new HashMap<>();
        this.musicas = new HashMap<>();
        this.lock = new ReentrantLock();
        this.waitDownload = this.lock.newCondition();
        this.musicId = 0;
        this.numDownloads = 0;
    }

    public boolean registarUser(String username, String pass){
        boolean registou = false;

        synchronized (this.clientes){
            if(!this.clientes.containsKey(username)) {
                registou = true;
                this.clientes.put(username, new Utilizador(username, pass));
            }
        }

        return registou;
    }

    public Utilizador login(String username, String pass, Socket cs){

        synchronized (this.clientes){
            if(this.clientes.containsKey(username) && this.clientes.get(username).login(pass, cs)){
                return this.clientes.get(username);
            }
        }

        return null;
    }


    public ArrayList<String> pesquisarMusica(String etiqueta){
        ArrayList<String> musicas = new ArrayList<>();
        ArrayList<Long> temEtiqueta = new ArrayList<>();

        synchronized (this.musicas){
            this.musicas.entrySet().stream().forEach(h -> {
                if (h.getValue().containsEtiqueta(etiqueta)) {
                    temEtiqueta.add(h.getKey());
                }
            });
            temEtiqueta.stream().sorted().forEach(h -> this.musicas.get(h).lockMusica()); // necessario ?
        }

        temEtiqueta.stream().sorted().forEach(h -> {
            musicas.add(this.musicas.get(h).dadosPesquisa());
            this.musicas.get(h).unlockMusica();
        });

        return musicas;
    }

    public String downloadOk(long id){
        synchronized (this.musicas){
            if (this.musicas.containsKey(id)){
                return this.musicas.get(id).getTitulo();
            }
            return null;
        }
    }

    public long incMusicId(){
        synchronized ((Object) this.musicId) {
            return this.musicId++;
        }
    }

    public void upload(Long id, String titulo, String interprete, String ano, List<String> tags){

        synchronized (this.musicas){
                this.musicas.put(id, new Musica(id, titulo, interprete, ano, tags));
            }
    }

    // passar serviço para uma thread diferente
    public void notifyAllUsers(String titulo, String autor){
        String msg = "NOTF: Nova música ! Título "+ titulo + "  Autor " + autor;

        synchronized (this.clientes){
            this.clientes.values().stream().forEach(h -> h.notifyMusic(msg));
        }
    }

    public void download(long id, Socket cs) {

        this.lock.lock();

        try {
        while (this.numDownloads == this.MAXDOWN) {

            this.waitDownload.await();

            // ordenar threads
        }
        this.numDownloads++;
        this.musicas.get(id).incNumDownld();


        this.lock.unlock();

        getMusica(id, cs);

        this.lock.lock();

        --this.numDownloads;
        this.waitDownload.signal();

        this.lock.unlock();
        } catch (InterruptedException e) {
            this.lock.unlock();
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Download Impossível.");
        }
    }

    public void getMusica(long id, Socket cs) throws IOException {
        File file = new File(this.pastaBD+id+".mp3");   // tirar mais tarde mudar para txt
        long fileLength = file.length();
        long current = 0;
        int size = 10000;
        byte[] contents;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        PrintWriter pw = new PrintWriter(cs.getOutputStream());
        StringBuilder result = new StringBuilder();

        while(current!=fileLength){

            if(fileLength - current >= size)
                current += size;
            else{
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents, 0, size);
            pw.println(Base64.getEncoder().encodeToString(contents));  // tirar mais tarde
        }

        pw.flush();
    }


}
