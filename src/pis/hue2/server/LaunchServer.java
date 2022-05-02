package pis.hue2.server;
import pis.hue2.common.Common;
import pis.hue2.common.Instruction;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LaunchServer {
    public static final int PORT = 8080;
    public int numberOfClients;
    public static List<String> listOfClients;
    public static String filesList;

    public LaunchServer(int port) throws IOException {
        listOfClients=new ArrayList<>();
        filesList="";
        numberOfClients=0;
        updateFiles();
        ServerSocket server = new ServerSocket(port);

        PrintWriter status = new PrintWriter
                (new BufferedWriter
                        (new FileWriter (Common.serverDir+"status.txt"))) ;
        status.append("true");
        status.flush();
        status.close();
        while(true){
            System.out.println("Server available on port: "+PORT);
            if(numberOfClients<4) {
                new ServerThread(server.accept());
                System.out.println("Client Connected!");
            }else{
                JOptionPane.showMessageDialog(new JFrame(),"maximum Clients reached","Error",JOptionPane.ERROR_MESSAGE);
                server.close();
                break;
            }
            numberOfClients++;
            System.out.println("Number of client: "+numberOfClients);

        }
    }

    public static void updateFiles() throws IOException {
        File file = new File(Common.serverDir);
        if(file.isDirectory()){
            File listOfFiles[]=file.listFiles();
            PrintWriter serverfiles=new PrintWriter(new BufferedWriter(new FileWriter(Common.serverDir+"serverfiles.txt")));
            for(int i=0; i<listOfFiles.length;i++){
                serverfiles.append(listOfFiles[i].getName()+"\n");
                filesList+=listOfFiles[i].getName()+"\n";;
            }
            serverfiles.flush();
            serverfiles.close();
        }
    }

    private class ServerThread extends Thread {
        boolean con,dsc,get,put,lst,del=false;
        Stack<Instruction> getstack = new Stack<>();
        private final Socket socket;
        DataOutputStream out=null;
        FileInputStream file=null;
        BufferedReader inFromClient=null;
        String serveFiles;
        Instruction go;

        public ServerThread(Socket socket) {
            serveFiles="";
            this.socket=socket;
            start();
        }


        public void run() {
            if(this.socket!=null) {
                handling(this.socket);
            }
        }

        private void handling(Socket socket){
            try {
                out = new DataOutputStream(socket.getOutputStream());
                //in = new DataInputStream(socket.getInputStream());

                inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Instruction[] msgs = new Instruction[4];
                int count=0;
                String inmsg;
                String remember=null;
                boolean found=false;
                boolean change=false;
                while ((inmsg = inFromClient.readLine().trim())!= null) {
                    System.out.println("client sent "+inmsg);
                    String inside[] = csmkSlipt(inmsg);
                    for (int i = 0; i < Instruction.values().length; i++) {
                        if (inside[0].toUpperCase().equals(Instruction.values()[i].toString())) {
                            go=Instruction.values()[i];
                            // found = true;
                        }
                    }

                    /*if (found) {
                        found = false;*/

                    if(go.equals(Instruction.CON)) con=true;
                    if(con) {

                        boolean b = inside[0].toUpperCase().equals(Instruction.GET.toString()) || inside[0].toUpperCase().equals(Instruction.PUT.toString()) || inside[0].toUpperCase().equals(Instruction.DEL.toString());
                        if (b && inside.length < 2) {
                            count = 3;
                            change = true;
                        }
                        if (b && inside.length > 1) {
                            remember = inside[1];
                        }
                        if (go.equals(Instruction.CON)) {
                            count = 4;
                            change = true;
                        } else if (go.equals(Instruction.DAT)) {
                            count = 5;
                            change = true;
                        }
                        else if(go.equals(Instruction.DEL)){
                            count=6;
                            change=true;
                        }
                        else if(go.equals(Instruction.PUT)){
                            count=7;
                            change=true;
                        }
                        else if (!go.equals(Instruction.ACK)) {
                            msgs[count] = go;
                            if (!go.equals(Instruction.LST) && !go.equals(Instruction.DSC) && !go.equals(Instruction.ACK)) {
                                msgs[3] = Instruction.DAT;
                            }
                            if (go.equals(Instruction.DSC)) {
                                count = 2;
                                change = true;
                            } else {
                                go = Instruction.ACK;
                            }
                        }

                        if (!change) {
                            count++;
                        }

                        if (count == 3) {
                            go = Instruction.DND;
                            count = 0;
                        } else if (count == 4) {
                            go = Instruction.CON;
                            count = 0;
                        } else if (count == 5) {
                            go = Instruction.DAT;
                            count = 0;
                        }
                        else if(count==6){
                            go=Instruction.DEL;
                            count=0;
                        }
                        else if(count==7){
                            go=Instruction.ACK;
                            count=0;
                        }
                        else if (count == 2) {
                            go = msgs[0];
                            count = 0;
                        }
                        change = false;

                        switch (go) {

                            case CON:
                                if (numberOfClients < 4) {
                                    out.writeUTF(Instruction.ACK + "\n");
                                    out.flush();
                                } else {
                                    out.writeUTF(Instruction.DND + "\n");
                                    out.flush();
                                    this.socket.close();
                                }
                                break;

                            case ACK:
                                out.writeUTF(Instruction.ACK.toString() + "\n");
                                out.flush();
                                break;

                            case DND:
                                out.writeUTF(Instruction.DND + "\n");
                                out.writeUTF("no file selected\n");
                                break;

                            case DSC:
                                out.writeUTF(Instruction.DSC.toString() + "\n");
                                numberOfClients -= 1;
                                this.socket.close();
                                System.out.println("Client deconnected\nNumber of client: " + numberOfClients);

                                break;

                            case LST:
                                out.writeUTF(Instruction.DAT.toString() + "\n");
                                Common.lst(out);

                                break;

                            case GET:
                                if (remember != null) {
                                    Common.get(remember, out);
                                } else {
                                    out.writeUTF("no file selected\n");
                                }
                                break;

                            case DEL:
                                if (remember != null) {
                                    Common.del(remember, out);
                                } else {
                                    out.writeUTF("no file selected\n");
                                }
                                break;

                            case DAT:out.writeUTF(Instruction.ACK.toString()+"\n");
                                break;
                        }
                    }else {
                        out.writeUTF("you have to send CON to server\nbefore to do anything\n");
                        out.flush();
                    }
                    /*} else {
                        out.writeUTF("unknown command '"+inside[0]+"' \n");
                        out.flush();
                    }*/
                }
            } catch(Throwable t){
                System.out.println("Caught " + t + " - closing thread");
            }

        }

    }
    public String[] csmkSlipt(String tosplit){
        String result[];
        if(tosplit.length()>=5) {
            result=new String[2];
            result[0]=result[1]="";
            for(int i=0; i<3; i++){
                result[0]+=tosplit.charAt(i);
            }
            for (int i=4; i<tosplit.length();i++){
                result[1]+=tosplit.charAt(i);
            }
        }else {
            result=new String[1];
            result[0]="";
            result[0]=tosplit.trim();
        }
        return result;
    }
    public static void main(String[] args) throws IOException {
        new LaunchServer(PORT);
    }
}
