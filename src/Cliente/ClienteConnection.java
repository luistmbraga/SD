package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteConnection {

    private static Socket cs;
    private static PrintWriter out;
    private static BufferedReader in;
    private static ClienteConnection obj;

    private ClienteConnection() throws IOException {
        this.cs = new Socket("127.0.0.1", 3000);
        this.out = new PrintWriter(this.cs.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.cs.getInputStream()));
    }

    public static ClienteConnection getClienteConnection() throws IOException {
        if(obj == null){
            synchronized (ClienteConnection.class){
                if(obj == null)
                    obj = new ClienteConnection();
            }
        }
        return obj;
    }

    public void send(String msg){
        this.out.println(msg);
    }

    public String receive() throws IOException {
        return this.in.readLine();
    }

    public void  close() throws IOException {
        this.cs.close();
    }
}
