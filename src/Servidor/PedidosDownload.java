package Servidor;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PedidosDownload {

    private int MAXDOWN = 50;
    private int numdownloads;

    private ReentrantLock lock;
    private Condition waitDownload;
    private Condition waitClients;

    private List<Pedido> pedidos;

    public PedidosDownload(){
        this.numdownloads = 0;
        this.lock = new ReentrantLock();
        this.waitDownload = this.lock.newCondition();
        this.waitClients = this.lock.newCondition();
        this.pedidos = new ArrayList<>();
    }

    public void add(long id, String username, Socket s){
        this.lock.lock();
        this.pedidos.add(new Pedido(id, s, username));
        this.waitClients.signal();
        this.lock.unlock();
    }

    public void notifyWaitDownload(){
        this.lock.lock();
        System.out.println("Acabei o download e vou notificar a thread");
        this.numdownloads--;
        this.waitDownload.signal();
        this.lock.unlock();
    }

    public List<Pedido> getPedidos(int pedidosEspera) {
        List<Pedido> r = new ArrayList<>();

        this.lock.lock();
        try {

        while( pedidosEspera == 0 && this.pedidos.size() == 0){
            System.out.println("Nao ha clientes, vou dormir.");
            this.waitClients.await();

        }

        System.out.println("Os pedidos chegaram, vou trabalhar");

        for (Pedido p : this.pedidos)
            r.add(p);

        this.pedidos.clear();

        this.lock.unlock();

        } catch (InterruptedException e) { // vem do await
            this.lock.unlock();
            e.printStackTrace();
        }

        return r;
    }

    public void incDownload() {
        this.lock.lock();

        try {

        while (this.numdownloads == this.MAXDOWN) {
                System.out.println("O numero maximo de downloads foi atingido.");
                this.waitDownload.await();

        }

        this.numdownloads++;

        this.lock.unlock();

        } catch (InterruptedException e) {
            this.lock.unlock();
            e.printStackTrace();
        }
    }

}
