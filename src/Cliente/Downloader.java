package Cliente;

import java.io.*;
import java.net.Socket;

public class Downloader implements Runnable {

    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private String titulo;
    private String id;
    private String username;

    private String pastaDB = System.getProperty("user.home") + "\\Downloads\\";

    public Downloader(String tit, String id, String username){
        this.titulo = tit;
        this.id = id;
        this.username = username;
    }

    public void createFile() throws FileNotFoundException {
        File file = new File(this.pastaDB+this.titulo);

        for (int i = 1; file.exists(); i++){
            file = new File(this.pastaDB+"("+i+")"+this.titulo);
        }

        this.fos = new FileOutputStream(file);
        this.bos = new BufferedOutputStream(fos);
    }

    public void enviaPedido(Socket s) throws IOException {
        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        pw.println(this.id+":"+this.username);
    }

    private void receiveData(Socket socket) throws IOException {
        byte[] contents = new byte[10000];

        InputStream is = socket.getInputStream();

        //No of bytes read in one read() call
        int bytesRead;

        while((bytesRead=is.read(contents))!=-1){
            bos.write(contents, 0, bytesRead);
            bos.flush();
        }
    }

    public void run() {
        try {
        //Initialize socket
        Socket socket;

        socket = new Socket("127.0.0.1", 3001);

        // enviar pedido
        enviaPedido(socket);

        //Initialize the FileOutputStream to the output file's full path.
        createFile();

        receiveData(socket);

        this.bos.close();

        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();

        } catch (IOException e) {
            System.out.println("Algo inesperado aconteceu com o seu download.");
        }
    }

}
