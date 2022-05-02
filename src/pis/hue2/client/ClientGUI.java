package pis.hue2.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pis.hue2.common.Common;
import pis.hue2.common.Instruction;
import pis.hue2.server.LaunchServer;

import javax.swing.*;
import java.io.*;

public class ClientGUI extends Application implements Runnable{
    String[] operations={"CON","LST","PUT","GET","DEL"};
    Instruction go;
    static LaunchClient client;
    public static void main(String[] args) throws Exception {
        Thread t= new Thread(new ClientGUI());
        t.start();

    }
    public static void con() throws Exception {
        client =new LaunchClient(LaunchServer.PORT);
    }

    public static String files() throws IOException {
        String filesList="";
        File file = new File(Common.serverDir);
        if(file.isDirectory()){
            File listOfFiles[]=file.listFiles();
            for(int i=0; i<listOfFiles.length;i++){
                filesList+=listOfFiles[i].getName()+"\n";;
            }
        }
        return filesList;
    }

    @Override
    public void start(Stage stage) {
        root(stage, operations);

    }
    void root(Stage stage, String[] operations) {
        FlowPane newRoot= new FlowPane();
        newRoot.setPadding(new Insets(8));
        newRoot.setHgap(30);
        newRoot.setVgap(30);
        ComboBox opList=new ComboBox(FXCollections.observableArrayList(operations));
        Button request = new Button("request");
        request.setStyle("-fx-background-radius: 10;");
        Button condsc= new Button("connect");
        condsc.setStyle("-fx-background-radius: 10;");
        Text put = new Text("to put");
        Text get = new Text("to get/del");
        Text cod= new Text("to con. or disc.");
        TextField input = new TextField();
        TextField output = new TextField();
        TextArea activity= new TextArea();
        activity.setPrefWidth(150);
        request.setStyle("-fx-background-radius: 20;");
        newRoot.getChildren().add(opList);
        newRoot.setHgap(30);
        newRoot.getChildren().add(condsc);;
        //newRoot.getChildren().add(cod);
        newRoot.getChildren().add(input);
        newRoot.getChildren().add(put);
        newRoot.getChildren().add(output);
        newRoot.getChildren().add(get);
        newRoot.setHgap(60);
        newRoot.getChildren().add(request);
        newRoot.getChildren().add(activity);

        Scene scene = new Scene(newRoot,350,500);

        condsc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if(condsc.getText().equals("connect")) {
                        condsc.setText("disconnect");
                        con();
                        client.countClient();
                    }
                    else {
                        condsc.setText("connect");
                        client.outs.writeUTF(Instruction.DSC.toString()+"\n");
                        client.outs.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        opList.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (int i = 0; i< Instruction.values().length; i++){
                    if (opList.getValue().equals(Instruction.values()[i].toString())) {
                        go=Instruction.values()[i];
                        break;
                    }
                }
            }
        });

        request.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    if (condsc.getText().equals("connect") ) {
                        JOptionPane.showMessageDialog(new JFrame(), "you are not connected", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(go!=null){

                        if (go.equals(Instruction.LST) && input.getText().isEmpty() && output.getText().isEmpty()) {
                            client.outs.writeUTF(go.toString().trim() + "\n");
                            client.outs.flush();
                            client.cmsg=go.toString();
                            activity.setText(files());



                        } else if (go.equals(Instruction.GET) && input.getText().isEmpty() && !output.getText().isEmpty()) {
                            client.outs.writeUTF(go.toString().trim()+" "+output.getText().trim()+"\n");
                            client.outs.flush();
                            client.cmsg=go.toString();
                            client.filename=output.getText().trim();
                        }
                        else if(go.equals(Instruction.PUT) && !input.getText().isEmpty() && output.getText().isEmpty()){
                            client.outs.writeUTF(go.toString().trim()+" "+input.getText().trim()+"\n");
                            client.outs.flush();
                            client.cmsg=go.toString();
                            client.filename=input.getText().trim();

                        }
                        else if(go.equals(Instruction.CON) && input.getText().isEmpty() && output.getText().isEmpty()){
                            client.outs.writeUTF(go.toString().trim()+"\n");
                            client.outs.flush();
                            client.cmsg=go.toString();
                        }
                        else if(go.equals(Instruction.DEL) && input.getText().isEmpty() && !output.getText().isEmpty()){
                            client.outs.writeUTF(go.toString().trim()+" "+output.getText().trim()+"\n");
                            client.outs.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                if(client.sockets[0].isConnected() && condsc.getText().equals("disconnect")) {
                    JOptionPane.showMessageDialog(new JFrame(), "you will be automatic disconnect", "Info", JOptionPane.INFORMATION_MESSAGE);
                    client.discountClient();
                    try {
                        client.sockets[0].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    stage.close();
                }
            }
        });

        stage.setTitle("Session");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void run() {
        launch();
    }
}
