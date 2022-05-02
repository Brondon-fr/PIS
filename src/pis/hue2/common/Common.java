package pis.hue2.common;

import pis.hue2.server.LaunchServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Common {
    public static String serverDir="C:\\Users\\franc\\OneDrive\\SocketsTest\\ServerFiles\\";
    public static String clientDir="C:\\Users\\franc\\OneDrive\\SocketsTest\\ClientsFiles\\";


    static void data(long length, byte data[],DataOutputStream out) throws IOException {
        System.out.println("Sending...");
        out.write(data,0,(int)length);
        out.flush();
    }

    public static void get(String filename,DataOutputStream out) throws IOException {
        File data = new File(serverDir+filename);
        long orginalSize=data.length();
        byte[] dataInBytes;
        int num=0;
        int rest=0;
        int best=1024*1024;
        if(!data.isFile()){
            out.writeUTF("file not found");
        }else {
            OutputStream file = new DataOutputStream(new FileOutputStream(clientDir+filename));
            if(data.length()>Integer.MAX_VALUE){
                //modulo
                while(orginalSize>0){
                    if(orginalSize-(best)>0) {
                        orginalSize-= (best);
                        num++;
                    }else{
                        rest=(int)orginalSize;
                        orginalSize=0;
                    }
                }

                FileInputStream fis = new FileInputStream(data);
                BufferedInputStream bis = new BufferedInputStream(fis);

                out.writeUTF(Instruction.DAT+" "+Long.toString(data.length())+" bytes\n");
                for (int i = 0; i <num; i++) {
                    dataInBytes = new byte[best];
                    bis.read(dataInBytes, 0, dataInBytes.length);
                    file.write(dataInBytes, 0, dataInBytes.length);
                    System.out.println("Sending...");
                    // out.write(dataInBytes, 0, dataInBytes.length);
                }

                dataInBytes=new byte[rest];
                bis.read(dataInBytes, 0, dataInBytes.length);
                file.write(dataInBytes, 0, dataInBytes.length);
                System.out.println("Sending2...");
                //out.write(dataInBytes, 0, dataInBytes.length);
                out.flush();
                file.close();
            }
            else {
                dataInBytes = new byte[(int) data.length()];
                FileInputStream fis = new FileInputStream(data);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(dataInBytes, 0, dataInBytes.length);
                file.write(dataInBytes, 0, dataInBytes.length);
                file.close();
                out.writeUTF(dataInBytes.length + " bytes\n\n");
                out.flush();
                System.out.println("Sending...");
                out.write(dataInBytes,0,dataInBytes.length);
                out.flush();

            }
        }
    }

    public static void put(String filename,DataOutputStream out) throws IOException {
        File data = new File(clientDir+filename);
        long orginalSize=data.length();
        byte[] dataInBytes;
        int num=0;
        int rest=0;
        int best=1024*1024;
        if(!data.isFile()){
            out.writeUTF("file not found");
        }else {
            OutputStream file = new DataOutputStream(new FileOutputStream(serverDir+filename));
            if(data.length()>Integer.MAX_VALUE){
                //modulo
                while(orginalSize>0){
                    if(orginalSize-(best)>0) {
                        orginalSize-= (best);
                        num++;
                    }else{
                        rest=(int)orginalSize;
                        orginalSize=0;
                    }
                }
                FileInputStream fis = new FileInputStream(data);
                BufferedInputStream bis = new BufferedInputStream(fis);


                for (int i = 0; i <num; i++) {
                    dataInBytes = new byte[best];
                    bis.read(dataInBytes, 0, dataInBytes.length);
                    file.write(dataInBytes, 0, dataInBytes.length);
                    System.out.println("Sending...");
                    //out.write(dataInBytes, 0, dataInBytes.length);
                }

                dataInBytes=new byte[rest];
                bis.read(dataInBytes, 0, dataInBytes.length);
                file.write(dataInBytes, 0, dataInBytes.length);
                System.out.println("Sending2...");
                out.write(dataInBytes, 0, dataInBytes.length);
                out.flush();
                out.writeUTF(Instruction.DAT+" "+Long.toString(data.length())+" bytes\n");
                file.close();
            }
            else {
                dataInBytes = new byte[(int) data.length()];
                FileInputStream fis = new FileInputStream(data);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(dataInBytes, 0, dataInBytes.length);
                file.write(dataInBytes, 0, dataInBytes.length);
                file.close();
                out.writeUTF(dataInBytes.length + " bytes\n\n");
                out.flush();
                System.out.println("Sending...");
                out.write(dataInBytes,0,dataInBytes.length);

                out.flush();

            }
        }
    }

    public static void lst(DataOutputStream out) throws IOException {
        LaunchServer.updateFiles();
        File data = new File(serverDir+"serverfiles.txt");
        byte [] dataInBytes  = new byte [(int)data.length()];
        FileInputStream fis = new FileInputStream(data);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(dataInBytes,0,dataInBytes.length);
        out.writeUTF(dataInBytes.length+" bytes\n\n");
        System.out.println("Sending...");
        out.write(dataInBytes,0,dataInBytes.length);
        out.flush();


    }

    public static void del(String filename,DataOutputStream out) throws IOException {
        File data = new File(serverDir+ filename);
        if(data.isFile()){
            data.delete();
            out.writeUTF(Instruction.ACK.toString());
            out.writeUTF(filename+" deleted\n");
        }else{
            out.writeUTF(Instruction.DND.toString());
            out.writeUTF("file not exists");
        }
    }

}
