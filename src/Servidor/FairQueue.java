package Servidor;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class FairQueue {
    private Map<String, Integer> clienteNumDownld;
    private List<String> waitQueue;
    private ReentrantLock lock;
    private int MAXUSERCONT = 1;

    public FairQueue(){
        this.clienteNumDownld = new HashMap<>();
        this.waitQueue = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    private String max(){
        String max = null;
        int maxDown = 0;
        this.lock.lock();

        for(Map.Entry h : this.clienteNumDownld.entrySet()){
            if ((Integer) h.getValue() > maxDown){
                maxDown = (Integer) h.getValue();
                max = (String) h.getKey();
            }
        }

        this.lock.unlock();
        return max;
    }

    private String min(){
        String min = null;
        int minDown = 0;
        this.lock.lock();

        for(Map.Entry h : this.clienteNumDownld.entrySet()){
            if ((Integer) h.getValue() < minDown){
                minDown = (Integer) h.getValue();
                min = (String) h.getKey();
            }
        }

        this.lock.unlock();
        return min;
    }

    public String peek(){
        String r;
        this.lock.lock();
        r = this.waitQueue.get(0);
        this.lock.unlock();
        return r;
    }

    public void remove(String username){
        this.lock.lock();
        this.waitQueue.remove(0);
        int num = this.clienteNumDownld.get(username);
        if (num == 1) this.clienteNumDownld.remove(username);
        else this.clienteNumDownld.replace(username, --num);
        this.lock.unlock();
    }

    private int getContiguousOrders(){
        int i, record, combo;
        combo = record = 1;

        int size = this.waitQueue.size();

        for(i = 1; i < size; ++i){

            if(this.waitQueue.get(i-1).equals(this.waitQueue.get(i))) combo++;
            else combo = 1;

            if(combo > record) record = combo;
        }

        return record;
    }

    private void setmaxusercount(){
        int users = this.clienteNumDownld.size();

        if (users >= 50 && users < 100) this.MAXUSERCONT = 3;
        else if (users >= 100 && users < 200) this.MAXUSERCONT = 4;
        else if (users >= 200 && users < 300) this.MAXUSERCONT = 5;
        else if (users >= 300) this.MAXUSERCONT = 6;
    }

    private int getmaxcont(int numD){

        if (numD <= this.MAXUSERCONT){
            return numD;
        }

        return this.MAXUSERCONT;
    }

    private void sortOrders(){
        List<String> r = new ArrayList<>();
        Map<String, Integer> clienteEmEspera = new HashMap<>(this.clienteNumDownld);

        while(clienteEmEspera.size() > 0){
            for(Map.Entry h : clienteEmEspera.entrySet()){
                int numD = getmaxcont((Integer) h.getValue());
                for (int i = numD; i > 0; i--)
                    r.add((String) h.getKey());
                if (numD == 1) clienteEmEspera.remove(h.getKey());
                else clienteEmEspera.replace((String) h.getKey(), (Integer) h.getValue() - numD);
            }
        }

        this.waitQueue = r;
    }

    public void add(String username){
        this.lock.lock();

        if (this.clienteNumDownld.containsKey(username)) {
            int num  = this.clienteNumDownld.get(username);
            this.clienteNumDownld.replace(username, ++num);
        }
        else {
            this.clienteNumDownld.put(username, 1);
        }

        setmaxusercount();

        this.waitQueue.add(username);

        if (this.clienteNumDownld.size() > 1 && getContiguousOrders() > this.MAXUSERCONT)
            sortOrders();

        this.lock.unlock();
    }

}
