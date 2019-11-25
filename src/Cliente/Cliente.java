package Cliente;

import java.io.IOException;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args){

        try {

            // definir pasta de downloads


            Socket cs = new Socket("127.0.0.1", 3000);

            ClienteState cls = new ClienteState();

            Thread r = new Thread(new Receiver(cs, cls));

            Thread n = new Thread(new MenuNavigator(cs,cls));

            r.start();

            n.start();

        } catch (IOException e) {
            System.out.println("Erro : "+ e.getMessage());
        }



    }
}
