package Cliente;

public class ClienteState {

    private boolean logged;
    private boolean unlogged;
    private boolean waiting_for_response;

    public ClienteState(){
        this.logged = false;
        this.unlogged = false;
        this.waiting_for_response = false;
    }

    public synchronized void login(){
        this.logged = true;
    }

    public synchronized void logout(){
        this.unlogged = true;
    }

    public synchronized boolean getLogged(){
        return this.logged;
    }

    public synchronized boolean getUnlogged(){
        return this.unlogged;
    }

    public synchronized boolean getWaitingRes(){
        return this.waiting_for_response;
    }

    public synchronized void finishWaiting(){
        this.waiting_for_response = false;
        notifyAll();
    }

    public synchronized void initWaiting() throws InterruptedException {
        this.waiting_for_response = true;
        while (waiting_for_response)
            wait();
    }
}
