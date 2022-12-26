

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class WebServer {
    int port =5003;

    public boolean recieveFile(String fileName, String fileType,int filesize,DataInputStream dataInputStreamFile,DataOutputStream dataOutputStreamFile,int CHUNK_SIZE) throws IOException {

        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.dir")+"\\uploaded\\"+fileName);
        int CUR_BUFFER_SIZE=0;
        try{
            System.out.println(filesize);
            int size = filesize;     // read file size
            byte[] buffer = new byte[CHUNK_SIZE];
            int CHUNK = 0;

            CUR_BUFFER_SIZE += CHUNK_SIZE;
            while (size > 0) {

                boolean ok;
                try {
                    System.out.println("uuu");
                    ok = (bytes = dataInputStreamFile.read(buffer, 0, Math.min(buffer.length, size))) != -1;
                }catch (SocketTimeoutException socketTimeoutException){
                    CUR_BUFFER_SIZE -= CHUNK_SIZE;
                    fileOutputStream.close();
                    System.out.println("FileOutputStream Closed");
                    return false;
                }

                if(!ok) break;

                if(CHUNK % 10000 == 0)System.out.println("Chunk #"+CHUNK);
                CHUNK++;

                fileOutputStream.write(buffer,0,bytes);

                size -= bytes;      // read upto file size

                dataOutputStreamFile.writeUTF("ACK");
                dataOutputStreamFile.flush();
//            }

            }

            CUR_BUFFER_SIZE -= CHUNK_SIZE;
            fileOutputStream.close();
            System.out.println("FileOutputStream Closed");

        }catch (Exception e)
        {
            System.out.println("Exception ... ");
            CUR_BUFFER_SIZE -= CHUNK_SIZE;
            fileOutputStream.close();
            System.out.println("FileOutputStream Closed");
        }

        // check confirmation and validate file size
        String msg = dataInputStreamFile.readUTF();
        if(msg.equals("ACK")){
            File file = new File(System.getProperty("user.dir")+"\\uploaded\\"+fileName);
            if(file.length() != filesize)
            {
                System.out.println("File size mismatch");
                file.delete();
                return false;
            }
        }
        else
        {
            File file = new File(System.getProperty("user.dir")+"\\uploaded\\"+fileName);
            file.delete();
            return false;
        }

        return true;
    }


    WebServer() throws IOException {


        ServerSocket servsocket = new ServerSocket(port);

        while(true) {


            Socket connectionSocketFile =servsocket.accept();
            String addr=connectionSocketFile.getRemoteSocketAddress().toString();
//            connectionSocketFile.setSoTimeout(5000);
            DataInputStream disFile = new DataInputStream(connectionSocketFile.getInputStream());
            DataOutputStream dosFile = new DataOutputStream(connectionSocketFile.getOutputStream());
            BufferedReader reader =new BufferedReader(new InputStreamReader(connectionSocketFile.getInputStream()));

            Thread workerThreadFile = new Thread(new Runnable() {
String r="";
                @Override
                public void run() {
while(true){

    try{







        String textFromClient = disFile.readUTF();


        if(textFromClient.startsWith("send")){
            System.out.println("good");
            String []s=textFromClient.split(" ");
            System.out.println("siuu2"+s.length);

String fileName=s[3];
String fileType=s[4];
            System.out.println(s[2]);
            System.out.println(s[5]);
int fileSize= Integer.parseInt(s[2]);
int chunk=Integer.parseInt(s[5]);
//            System.out.println("siuu");
            try
            {
                System.out.println();
                connectionSocketFile.setSoTimeout(5000);
                System.out.println("bbb");
                boolean ok=recieveFile(fileName,fileType,fileSize,disFile,dosFile,chunk);
                connectionSocketFile.setSoTimeout(0);

                if(ok)
                {
                   dosFile.writeUTF("ACK");
                    System.out.println("File Upload Completed");
                }
                else
                {
//                    File file = new File(""fileName);
//                    System.out.println(file.delete());
//                    curUser.writeToFileStream("NOT_ACK");
                    System.out.println("File Upload Failed");
                }

            }
            catch(Exception e)
            {
//                File file = new File("files/"+curUser.getId()+"/"+fileType+"/"+fileName);
//                System.out.println(file.delete());
//                curUser.writeToFileStream("File Deleted");
                System.err.println("Could not transfer file.");
            }
        }else{


            System.out.println("sd");

        }

    }catch(Exception e){


    }



}

                }


            });

            Thread fileReader=new Thread(new Runnable() {
                String clientRequest="";
                String req="";
                @Override
                public void run() {
                try{
                    while ((clientRequest = reader.readLine()) != null) {
                        if (req.equals("")) {
                            req  = clientRequest;
                        }
                        if (clientRequest.equals("")) { // If the end of the http request, stop
                            break;
                        }
                    }

                    if (req != null && !req.equals("")) {
                        new HttpWorker(req,connectionSocketFile).start();
                    }

                }catch(Exception e){

                }

                }
            });
if(addr.contains("127.0.0.1")){
    workerThreadFile.start();
}else
fileReader.start();



        }


        }



    public static void main(String args[]) throws IOException {

WebServer wb=new WebServer();

    }

}