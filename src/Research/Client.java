package Research;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;

public class Client {

    public static void main(String[] args) throws Exception{

        //Initialize socket
        Socket socket = new Socket(InetAddress.getByName("localhost"), 5000);
        byte[] contents = new byte[10000];

        //Initialize the FileOutputStream to the output file's full path.
        FileOutputStream fos = new FileOutputStream("C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\out.mp3");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = socket.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        //No of bytes read in one read() call
        int bytesRead = 0;
        int s = 0;
        int i = 0;
        int j = 0;
        String msg = new String();
        //while ((msg = in.readLine()) != null)
        //while((bytesRead=is.read(contents))!=-1)
        while ((msg = in.readLine()) != null)
        {
            //contents = new byte[msg.getBytes().length];
            contents = Base64.getDecoder().decode(msg);
            System.out.println(msg);

            //msg = new String(contents);

            //System.out.println("Tamanho do array: " + s + "Tamanho da string: "+i+"Tamanho da string -> array: " + (j+=msg.getBytes().length));

            bos.write(contents, 0, contents.length); // 0 bytes read
            /*
            for(byte b : contents)
                System.out.println(b);*/
            //out.println(msg);
        }

        //out.flush();
        bos.flush();
        socket.close();

        System.out.println("File saved successfully!");
    }
}
