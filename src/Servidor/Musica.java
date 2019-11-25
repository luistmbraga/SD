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
    private int numDownloads;

    private Lock lock;

    public Musica(Long id, String titulo, String interprete, String ano, List<String> etiquetas){
        this.id = id;
        this.titulo = titulo;
        this.interprete = interprete;
        this.ano = ano;
        this.etiquetas = new ArrayList<>();
        etiquetas.stream().forEach(h-> this.etiquetas.add(h));
        this.lock = new ReentrantLock();
        this.numDownloads = 0;
    }

    // é preciso fazer lock ?
    public boolean containsEtiqueta(String etiqueta){
        return this.etiquetas.contains(etiqueta);
    }

    public String dadosPesquisa(){

        StringBuilder sb = new StringBuilder();
        sb.append("Id "+this.id+",Título "+this.titulo+", Intérprete "+this.interprete+", Ano "+this.ano+", Etiquetas ");
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

    public String getTitulo(){
        return this.titulo;
    }

    public void lockMusica(){
        this.lock.lock();
    }

    public void unlockMusica(){
        this.lock.unlock();
    }
}
