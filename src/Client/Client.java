package Client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Client {
    private Socket clientSocketFile = null;
    private DataInputStream disFile = null;
    private DataOutputStream dosFile = null;
    public void sendFile(String fileName,File file, String fileType, int CHUNK_SIZE,DataInputStream dataInputStreamFile,DataOutputStream dataOutputStreamFile) throws IOException {


        FileInputStream fileInputStream = new FileInputStream(file);

        System.out.println("sent");
        long fileLength = file.length();

        dataOutputStreamFile.writeUTF("send file "+ fileLength +" "+fileName+" "+fileType+" "+CHUNK_SIZE);
        dataOutputStreamFile.flush();

        // break file into chunks
        int bytes = 0;
        byte[] buffer = new byte[CHUNK_SIZE];
        int CHUNK = 0;
        while ((bytes=fileInputStream.read(buffer))!=-1){

//            if(CHUNK % 10000 == 0) System.out.println("Chunk #"+CHUNK);
            CHUNK++;

            dataOutputStreamFile.write(buffer,0,bytes);
            dataOutputStreamFile.flush();
            System.out.println("written "+bytes);


            try {
                // ACK
                String msg = dataInputStreamFile.readUTF();
                if(!msg.equals("ACK"))
                {
                    System.out.println("Did not receive ACK...");
                    break;
                }

            }catch (SocketTimeoutException socketTimeoutException){
                System.out.println("TIMEOUT");
                dataOutputStreamFile.writeUTF("TIMEOUT "+fileType+" "+fileName);
                dataOutputStreamFile.flush();
                fileInputStream.close();
                return;
            }
        }
        fileInputStream.close();

        // send confirmation
        dataOutputStreamFile.writeUTF("ACK");
        dataOutputStreamFile.flush();

        String msg = dataInputStreamFile.readUTF();
        if(msg.equals("ACK")) System.out.println("File Upload Completed");
        else System.out.println("File Upload Failed");

    }
    Client() throws IOException {

        clientSocketFile = new Socket("localhost", 2540);
        disFile = new DataInputStream(clientSocketFile.getInputStream());
        dosFile = new DataOutputStream(clientSocketFile.getOutputStream());

        Thread fileStream = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // first
                    Scanner sc = new Scanner(System.in);
                    String s = sc.nextLine();
                    final String[] path2 = {""};
                    try {
                        Files.list(new File((System.getProperty("user.dir"))+"/src/Client").toPath())
                                .forEach(path -> {
                                    System.out.println(path.toString());
                                    if (path.toString().substring(path.toString().lastIndexOf("\\")+1).equalsIgnoreCase(s)) {
                                   //     System.out.println("exists");
                                        path2[0] =path.toString();
                                    }else{
                                       // System.out.println("not exists");
                                    }

                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    File f=new File(path2[0]);
                    int CHUNK_SIZE = 10;

                    String fileName = f.getName();
                    String fileType = path2[0].toString().substring(path2[0].toString().lastIndexOf("\\")+1);
                    System.out.println(fileType);

              //     clientSocketFile.setSoTimeout(5000);
                    try {
                        sendFile(fileName,f,"txt",CHUNK_SIZE,disFile,dosFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //    clientSocketFile.setSoTimeout(0);

                }


            }

        });

        fileStream.start();
    }
    public static void main(String[] args) throws IOException {

        Client client = new Client();

    }
}
