package Servidor;

import java.net.Socket;

public class Pedido {
    private long musicId;
    private Socket cs;
    private String username;

    public Pedido(long mId, Socket cs, String username){
        this.musicId = mId;
        this.cs = cs;
        this.username = username;
    }

    public long getMusicId() {
        return musicId;
    }

    public Socket getCs() {
        return cs;
    }

    public String getUsername() {
        return username;
    }
}
