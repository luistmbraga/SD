package Servidor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Utilizador {

    private String username;
    private String password;
    private Lock lock;
    private Socket cs;
    private boolean status;

    public Utilizador(String username, String password){
        this.username = username;
        this.password = password;
        this.lock = new ReentrantLock();
        this.status = false;
    }

    public String getUsername(){
        return this.username;
    }

    public boolean login(String password, Socket cs){
        if (this.password.equals(password)){
            this.cs = cs;
            return (this.status = true);
        }

        return false;
    }

    public void logout(){
        this.lock.lock();
        this.status = false;
        this.lock.unlock();
    }


    public void notifyMusic(String msg){
        try {
            this.lock.lock();
            if (this.status){
                PrintWriter out = new PrintWriter(this.cs.getOutputStream(), true);
                out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            this.lock.unlock();
        }
    }

}
