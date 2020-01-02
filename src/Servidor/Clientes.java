package Servidor;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Clientes {

    private Map<String, Utilizador> clientes;

    private Lock lock;

    public Clientes(){
        this.clientes = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public boolean registarUser(String username, String pass){
        boolean registou = false;

        this.lock.lock();

        if(!this.clientes.containsKey(username)) {
            registou = true;
            this.clientes.put(username, new Utilizador(username, pass));

        }

        this.lock.unlock();

        return registou;
    }

    public Utilizador login(String username, String pass, Socket cs){

        Utilizador r = null;

        this.lock.lock();

        if(this.clientes.containsKey(username) && this.clientes.get(username).login(pass, cs)){
            r = this.clientes.get(username);
        }

        this.lock.unlock();

        return r;
    }

    // passar serviço para uma thread diferente
    public void notifyAllUsers(String titulo, String autor){
        String msg = "NOTF: Nova música ! Título "+ titulo + "  Autor " + autor;

        this.lock.lock();

        this.clientes.values().stream().forEach(h -> h.notifyMusic(msg));

        this.lock.unlock();
    }
}
