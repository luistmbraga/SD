package Servidor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Musica {

    private long id;
    private String titulo;
    private String interprete;
    private String ano;
    private List<String> etiquetas;
    private String extensao;
    private int numDownloads;

    private Lock lock;

    public Musica(Long id, String titulo, String interprete, String ano, List<String> etiquetas, String extensao){
        this.id = id;
        this.titulo = titulo;
        this.interprete = interprete;
        this.ano = ano;
        this.etiquetas = new ArrayList<>();
        etiquetas.stream().forEach(h-> this.etiquetas.add(h));
        this.extensao = extensao;
        this.lock = new ReentrantLock();
        this.numDownloads = 0;
    }

    // é preciso fazer lock ?
    public boolean containsEtiqueta(String etiqueta){
        return this.etiquetas.contains(etiqueta);
    }

    public String dadosPesquisa(){

        StringBuilder sb = new StringBuilder();
        sb.append("Id "+this.id);
        sb.append(", Título "+this.titulo);
        sb.append(", Intérprete "+this.interprete);
        sb.append(", Ano "+this.ano);
        sb.append(", Etiquetas ");
        for(String et: this.etiquetas)
            sb.append(et+";");

        this.lock.lock(); // necessario ?

        sb.append(", Número de Downloads "+this.numDownloads);

        this.lock.unlock();

        return sb.toString();
    }

    public void incNumDownld(){
        this.lock.lock();
        this.numDownloads++;
        this.lock.unlock();
    }

    public String getTituloDownload(){        return this.titulo+this.extensao;    }

    public String getExtensao(){
        return this.extensao;
    }

    public void lockMusica(){
        this.lock.lock();
    }

    public void unlockMusica(){
        this.lock.unlock();
    }
}
