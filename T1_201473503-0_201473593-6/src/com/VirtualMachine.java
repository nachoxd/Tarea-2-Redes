package com;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualMachine {

    public static void main(String[] args) {
        ServerSocket servidor = null;
        final int puerto = 4000;
        int ip_index = 14;
        String in,out,temp;
        JSONObject obj;
        byte[] response;

        ///////////////////////////////////////////////////////////////////

        //////////////////Aca escucha a clientes a traves de servidor////////////////////
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("Maquina virtual iniciada");
            while(true) { //Loop infinito para atender solicitudes de cliente a traves del servidor
                Socket s = null;
                obj = new JSONObject();
                try
                {
                    // socket object que recibe llamadas de cliente
                    s = servidor.accept();
                    // Obtener input y output streams
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    //Acá se maneja que hacer con cada posible input
                    try {
                        in = dis.readUTF();
                        String content = in.substring(1);
                        //nota: cambiar equals ls,get, blah
                        //Primer char = 1 es para ls
                        if (in.charAt(0)=='1'){ //Se le debe preguntar a la maquina virtual 1 a 1 por cada parte que deberia contener
                            File folder = new File("./");
                            File[] listOfFiles = folder.listFiles();
                            JSONArray list = new JSONArray();
                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].isFile()) {
                                    //para que esto funcione asume que la estructura de las partes es
                                    //XXarchivo de forma que los primeros 2 caracteres solo numeran
                                    temp = listOfFiles[i].getName().substring(2);
                                    if (content.equals(temp)){
                                        //SI el archivo es el solicitado
                                    }
                                }

                            }


                            obj.put("action", "ls");
                            obj.put("lista", list);
                            response = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                            dos.writeInt(response.length);
                            dos.write(response, 0, response.length);
                            break;
                        }
                        else if (in.equals("get")){
                            //GET block
                        }
                        else if (in.equals("get")){
                            //PUT block
                        }
                        else if (in.equals("get")){
                            //DELETE block
                        }






                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                catch (Exception e){
                    s.close();
                    e.printStackTrace();
                }
            }
        }
        catch (IOException ex) {
            Logger.getLogger(com.Server.class.getName()).log(Level.SEVERE,null,ex);
        }

    }
}
