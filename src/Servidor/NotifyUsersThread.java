package Servidor;

public class NotifyUsersThread implements Runnable {

    private Clientes clientes;
    private String titulo;
    private String autor;

    public NotifyUsersThread(Clientes clientes, String titulo, String autor){
        this.clientes = clientes;
        this.titulo = titulo;
        this.autor = autor;
    }

    @Override
    public void run() {
        this.clientes.notifyAllUsers(this.titulo, this.autor);
    }
}
