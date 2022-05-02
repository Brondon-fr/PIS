package pis.hue2.client;


import java.net.*;
import java.io.*;
import java.util.*;

import pis.hue2.common.Common;
import pis.hue2.common.Instruction;
import pis.hue2.server.LaunchServer;

// pis.hue2.client.MultiClient.java
public class LaunchClient {
    Scanner sc= new Scanner(System.in);
    String cmsg;
    String retain;
    String smsg;
    String filename;
    Socket[] sockets;
    DataOutputStream outs;
    DataInputStream ins;
    BufferedReader inFromServer=null;
    BufferedReader getInFromUser=null;
    Instruction doit;
    int numberOfClients;
    boolean con,dsc,get,put,lst,del=false;
    Stack<Instruction> getstack = new Stack<>();

    public LaunchClient(int port) throws Exception {
        getstack.push(Instruction.ACK);
        getstack.push(Instruction.ACK);
        numberOfClients=0;
        sockets = new Socket[3];
        connect();
        new Thread(new Client0Thread()).start();
        //new Thread(new ClientGUI()).start();

    }

    public boolean connect() throws IOException {
        BufferedReader serverStatus = new BufferedReader(new FileReader("status.txt"));
        String status = serverStatus.readLine();
        if (status.equals("true")) {
            System.out.println("try to connect...");
            sockets[0] = new Socket("localhost", LaunchServer.PORT);
            System.out.println("connected");
            outs= new DataOutputStream(sockets[0].getOutputStream());
            //ins[0] = new DataInputStream(sockets[0].getInputStream());
            inFromServer= new BufferedReader(new InputStreamReader(sockets[0].getInputStream()));
            //getInFromUser= new BufferedReader(new InputStreamReader(System.in));

            return  true;
        } else {
            System.out.println("Server is actually down");
        }
        return false;
    }

    public void countClient(){
        numberOfClients+=1;
    }
    public void discountClient(){
        numberOfClients-=1;
    }

    private class Client0Thread implements Runnable {

        public void run() {
            try {
                int count=0;
                while((smsg=inFromServer.readLine().trim())!=null) {

                    System.out.println("sever sent " + smsg);
                    for (int i = 0; i < Instruction.values().length; i++) {
                        if (smsg.toUpperCase().equals(Instruction.values()[i].toString())) doit = Instruction.values()[i];
                    }

                    if (cmsg != null && doit!=null) {
                        if (doit.equals(Instruction.ACK) && cmsg.equals(Instruction.LST.toString())) {
                            if(count!=2) {
                                outs.writeUTF(Instruction.ACK.toString() + "\n");
                                outs.flush();
                                count++;
                            }
                        }


                        if (doit.equals(Instruction.ACK) && cmsg.equals(Instruction.GET.toString())) {
                            if(count!=2){
                                outs.writeUTF(Instruction.ACK.toString()+"\n");
                                outs.flush();
                                count++;
                            }
                        }

                        if (doit.equals(Instruction.ACK) && cmsg.equals(Instruction.PUT.toString())) {

                            if (filename != null) {
                                Common.put(filename, outs);
                                cmsg=null;
                            }
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void in(){

        }

        public void out(){

        }

        /**
         * allow a client to communicate with the server depend of the instructions
         * @param instruction
         */
    }

    public static void main(String[] args) throws Exception {
        new LaunchClient(LaunchServer.PORT);
        //new LaunchClient(1000);

    }
}
