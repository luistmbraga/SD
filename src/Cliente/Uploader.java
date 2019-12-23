package Cliente;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class Uploader implements Runnable{
    private String conteudo;
    private File file;
    private int MAXSIZE = 1500; // 5 000 000 0 = 50 MB

    public Uploader(String conteudo, File file){
        this.conteudo = conteudo;
        this.file = file;
    }

    public void enviaPedido(Socket s) throws IOException {
        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        pw.println(this.conteudo);
    }

    public void getMusica(Socket socket) throws IOException {
        long fileLength = this.file.length();
        long current = 0;
        int size = this.MAXSIZE;
        byte[] contents;

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.file));

        OutputStream os = socket.getOutputStream();

        while(current!=fileLength){

            if (fileLength - current >= size){
                current += size;
            }
            else {
                size = (int) (fileLength - current);
                current = fileLength;
            }

            contents = new byte[size];
            bis.read(contents, 0, size);
            os.write(contents);
            os.flush();
        }

    }

    public void run() {
        try {

        //Initialize socket
        Socket socket;

        socket = new Socket("127.0.0.1", 3002);

        enviaPedido(socket);

        getMusica(socket);

        socket.shutdownOutput();

        // recebe mensagem
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(in.readLine());

        socket.shutdownInput();
        socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
