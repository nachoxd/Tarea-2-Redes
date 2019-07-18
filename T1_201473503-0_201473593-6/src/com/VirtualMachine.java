package com;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualMachine {

    public static void main(String[] args) {
        ServerSocket servidor = null;
        final int puerto = 6000;
        int ip_index = 14;
        String in,out,temp;
        JSONObject obj;
        JSONObject objreceived;
        String FILEPATH;
        int offset,len;
        int count, totalbytes;
        byte[] response, income;
        String received;
        JSONParser parser = new JSONParser();
        String b64enc;

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
                        obj = new JSONObject();
                        len = dis.readInt();
                        income = new byte[len];
                        totalbytes = dis.read(income, 0, len);
                        offset = totalbytes;
                        while (totalbytes > 0) {
                            totalbytes = dis.read(income, offset, (len - offset));
                            if (totalbytes >= 0) offset += totalbytes;
                        }
                        received = new String(income, 0, len, StandardCharsets.UTF_8);
                        objreceived = (JSONObject) parser.parse(received);
                        //System.out.println("Cliente "+client_ip+" | Input "+received+" recibido");



                        switch (objreceived.get("function").toString()) {
                            case "get":
                                try {
                                    String name = objreceived.get("extra").toString();
                                    System.out.println(name);
                                    FILEPATH = "./"+name;
                                    //int cantidad = Integer.parseInt(lista[0][2]);
                                    //String type = lista[0][1];
                                    //count = 1;
                                    //int i;
                                    //String segment = "";
                                    obj.put("action", "get");
                                    //obj.put("name", lista[0][0] + type);
                                    obj.put("name", name);
                                    //
                                    //
                                    File file = new File(FILEPATH);
                                    byte[] content = Files.readAllBytes(file.toPath());
                                    b64enc = Base64.getEncoder().encodeToString(content);
                                    obj.put("file", b64enc);
                                    response = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                                    dos.writeInt(response.length);
                                    dos.write(response, 0, response.length);
                                    //dis.readInt();


                                    //dos.writeInt(-1);
                                    //dis.readInt();
                                    obj.clear();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    System.out.println("Error de sistema de archivos. Conexion terminada.");
                                    break;
                                }
                            case "put":
                                //
                            case "remove":

                            /*case "trash":
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

                             */
                        }

                    } catch (IOException e1) {
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
