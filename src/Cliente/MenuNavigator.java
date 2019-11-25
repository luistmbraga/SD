package Cliente;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MenuNavigator implements Runnable{

    private PrintWriter out;

    private Socket cs;

    private ClienteState cls;

    private int menu_status;

    private int MAXSIZE = 50000000;

    public MenuNavigator(Socket cs, ClienteState cls) throws IOException {
        this.cls = cls;
        this.cs = cs;
        this.out = new PrintWriter(this.cs.getOutputStream(), true);
        this.menu_status = 0;
    }

    public int readOption(){
        int option = -1;
        boolean valid = false;
        String msg;
        Scanner is = new Scanner(System.in);

        while(!valid){
            try{
                msg = is.nextLine();
                option = Integer.parseInt(msg);
                valid = true;
            }
            catch (NumberFormatException e){
                System.out.println("Insira um dígito.\n");
            }
        }

        return option;
    }

    public void sendMsg(String msg){
        try {
            this.out.println(msg);
            this.cls.initWaiting();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isInvalid(String msg){
        return  (msg == null || msg.equals("") || msg.matches("(.*):(.*)") || msg.matches("(.*),(.*)"));
    }

    public void menuOneLogin(){
        String username, password;
        Scanner sc = new Scanner(System.in);

        System.out.println("Username:");
        username = sc.nextLine();
        System.out.println("Password:");
        password = sc.nextLine();

        if (isInvalid(username) || isInvalid(password)){
            System.out.println("Dados inválidos.");
            return;
        }

        sendMsg(String.join(":","LOGIN",username,password));

        if (this.cls.getLogged())
            this.menu_status++;
    }

    public void menuOneSignup(){
        String username, password, repeatPass;
        Scanner sc = new Scanner(System.in);

        System.out.println("Username:");
        username = sc.nextLine();
        System.out.println("Password:");
        password = sc.nextLine();
        System.out.println("Confirmar Password:");
        repeatPass = sc.nextLine();

        if (isInvalid(username) || isInvalid(password)){
            System.out.println("Dados inválidos.");
            return;
        }

        if (!password.equals(repeatPass)){
            System.out.println("As passwords não coincidem.");
        }else sendMsg(String.join(":","REGISTAR",username,password));

    }

    public void menuOne(){
        int option = readOption();

        switch (option){
            case 0:{
                this.cls.logout();
                break;
            }
            case 1:{
                menuOneLogin();
                break;
            }
            case 2:{
                menuOneSignup();
                break;
            }
            default:{
                System.out.println("Insira uma das possíveis opções.");
                menuOne();
                break;
            }
        }
    }

    public void menuTwoPesquisar(){
        String etiqueta;
        Scanner sc = new Scanner(System.in);

        System.out.println("Escreva a sua etiqueta de pesquisa:");
        etiqueta = sc.nextLine();

        if (isInvalid(etiqueta)){
            System.out.println("Dados Inválidos.");
            return;
        }

        sendMsg(String.join(":","SEARCH",etiqueta));

    }

    public boolean isAudioFile(String file){
        Pattern pattern =
                Pattern.compile("(.3gp$|.aa$|.aac$|.aax$|.act$|.aiff$|.alac$|.amr$|.ape$|.au$|." +
                        "awb$|.dct$|.dss$|.dvf$|.flac$|.gsm$|.iklax$|.ivs$|.m4a$|.m4b$|.m4p$|.mmf$|.mp3$|." +
                        "mpc$|.msv$|.nmf$|.nsf$|.ogg$|.oga$|.mogg$|.opus$|.ra$|.rm$|.raw$|.sln$|.tta$|.voc$|." +
                        "vox$|.wav$|.wma$|.wv$|.webm$|.8svx$)", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(file);

        return m.find();
    }

    public void getMusica(File file) throws IOException {
        long fileLength = file.length();
        long current = 0;
        int size = 10000;
        byte[] contents;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        PrintWriter pw = new PrintWriter(this.cs.getOutputStream());


        while(current!=fileLength){

            if(fileLength - current >= size)
                current += size;
            else{
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents, 0, size);
            pw.println(Base64.getEncoder().encodeToString(contents));
        }
        pw.flush();

    }

    public void menuTwoUpload(){
        String titulo, interprete, filePath;
        int ano;
        StringBuilder tags;
        Scanner sc = new Scanner(System.in);
        File file;
        boolean insereTag = true;

        System.out.println("Caminho até ao ficheiro:");
        filePath = sc.nextLine();
        file = new File(filePath);

        if (file.exists() && file.isFile() && file.length() < this.MAXSIZE && isAudioFile(titulo = file.getName())){
            System.out.println("Título da música: "+titulo);
            System.out.println("Intérprete da música:");
            interprete = sc.nextLine();
            System.out.println("Ano da música:");
            ano = readOption();

            if (isInvalid(titulo) || isInvalid(interprete)){
                System.out.println("Dados inválidos.");
                return;
            }

            tags = new StringBuilder();

            while (insereTag){
                System.out.println("1 - Nova Tag || 0 - Sair");
                int option = readOption();
                String tag;
                switch (option){
                    case 0:{
                        if (tags.length() == 0) System.out.println("Se não fornecer etiquetas ninguém encontrará a sua música.");
                        else {insereTag = false; tags.deleteCharAt(tags.length()-1);}
                        break;
                    }
                    case 1:{
                        System.out.println("Nova Tag:");
                        tag = sc.nextLine();
                        if(isInvalid(tag)) System.out.println("Etiqueta inválida.");
                        else {tags.append(tag); tags.append(",");}
                        break;
                    }
                    default:{
                        System.out.println("Escolha umas das possíveis opções.");
                    }
                }
            }
            this.out.println(String.join(":","UPLOAD",titulo,interprete,String.valueOf(ano), tags.toString()));
            try {
                getMusica(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.out.println("FILEND");
        }
        else System.out.println("Ficheiro inválido.");

    }

    public void menuTwoDownload(){
        long option;
        String msg;
        Scanner is = new Scanner(System.in);

        try{
            System.out.println("Id da música:");
            msg = is.nextLine();
            option = Integer.parseInt(msg);
            sendMsg(String.join(":","DOWNLOAD",String.valueOf(option)));
        }
        catch (NumberFormatException e){
            System.out.println("Identificador Inválido.");
        }

    }

    public void menuTwo(){
        int option = readOption();

        switch (option){
            case 1:{
                menuTwoPesquisar();
                break;
            }
            case 2:{
                menuTwoUpload();
                break;
            }
            case 3:{
                menuTwoDownload();
                break;
            }
            case 0:{
                this.menu_status--;
                break;
            }
            default:{
                System.out.println("Insira uma das possíveis opções.");
                menuTwo();
            }
        }
    }


    public void readMenus(){
        switch (this.menu_status){
            case 0:{
                menuOne();
                break;
            }
            case 1:{
                menuTwo();
                break;
            }
            default:
                break;
        }
    }

    public void drawMenus(){
        switch(this.menu_status){
            case 0:{
                System.out.println("1 - Log In    ||    2 - Registar    ||    0 - Sair");
                break;
            }
            case 1:{
                System.out.println("1 - Pesquisar    ||    2 - Upload de uma Música    ||    3 - Download de uma música    ||    0 - Sair");
            }
        }
    }

    public void run() {

        try {

            while (!this.cls.getUnlogged()){
                drawMenus();
                readMenus();
            }

            this.cs.shutdownOutput();
            this.cs.shutdownInput();
            this.cs.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
