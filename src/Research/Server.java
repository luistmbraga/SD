package Research;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;


public class Server {




    public static void main(String[] args) throws Exception {
        //Initialize Sockets
        ServerSocket ssock = new ServerSocket(5000);
        Socket socket = ssock.accept();

        //The InetAddress specification
        InetAddress IA = InetAddress.getByName("localhost");

        //Specify the file
        File file = new File("C:\\Users\\luisb\\Documents\\GitHub\\SD\\musics\\money.mp3");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);


        //Get socket's output stream
        OutputStream os = socket.getOutputStream();

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        //Read File Contents into contents array
        byte[] contents;
        long fileLength = file.length();
        long current = 0;

        long start = System.nanoTime();
        while(current!=fileLength){
            int size = 10000;
            if(fileLength - current >= size)
                current += size;
            else{
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents, 0, size);
            /*
            for(byte b : contents)
                System.out.println(b);*/

            //os.write(contents);
            out.println(Base64.getEncoder().encodeToString(contents));

            //System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
        }
        out.flush();
        //os.flush();
        //File transfer done. Close the socket connection!
        socket.close();
        ssock.close();
        System.out.println("File sent succesfully!");
    }

    private static String readAllBytesJava7(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
            System.out.println(content.length());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }


}
