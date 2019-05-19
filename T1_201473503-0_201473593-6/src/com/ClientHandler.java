package com;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


class ClientHandler extends Thread{ 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    final Socket s; 
    final String srvpsw = "holasoyunapassword";
      
    // Constructor 
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
		this.s = s; 
        this.dis = dis; 
        this.dos = dos; 
	}

	@Override
    public void run()  
    {
        JSONObject obj;
        String received; 
        String psw = null;
        int ip_index = 14;
        String b64enc;
        byte[] response;
        String frag;
        int parte;
        int offset;
        int count;

        for (int i=13;i<28;i++){
            if (s.toString().charAt(i)==','){
                ip_index = i;
                break;
            }
        }
        String client_ip = s.toString().substring(13,ip_index);
		try {
			psw = dis.readUTF();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	if(psw.equals(srvpsw)) { //chequeo de password para ingresar, intercambio de info de control, "handshake"
    		try {
				dos.writeUTF("OK");
			} catch (IOException e1) {
				e1.printStackTrace();
			}  //"OK" es el mensaje que el cliente espera para proceder, si recibe otro valor cierra la conexion
    		while (true){ 
                try { 
                    // Recibir comando de cliente
                    obj = new JSONObject();
                    received = dis.readUTF();
                    System.out.println("Cliente "+client_ip+" | Input "+received+" recibido");
                    if(received.equals("exit")){
                        //this.dos.writeUTF("Exit");
                        System.out.println("Cliente " + client_ip + " termina conexion.");
                        this.s.close(); 
                        break; 
                    }
                    else if (received.equals("ls")){
                        File folder = new File("./");
                        File[] listOfFiles = folder.listFiles();
                        JSONArray list = new JSONArray();
                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                                System.out.println("File " + listOfFiles[i].getName());
                                list.add("File " + listOfFiles[i].getName());

                            } else if (listOfFiles[i].isDirectory()) {
                                System.out.println("Directory " + listOfFiles[i].getName());
                                list.add("Directory " + listOfFiles[i].getName());
                            }

                        }
                        obj.put("action","ls");
                        obj.put("lista",list);
                    }
                    else if (received.equals("get")){
                        String FILEPATH = "./prueba.jpg";
                        File file = new File(FILEPATH);
                        try {
                            byte[] content = Files.readAllBytes(file.toPath());
                            System.out.println(content.length);

                            b64enc = Base64.getEncoder().encodeToString(content);
                            int tamaño = 65536;
                            parte = b64enc.length()/tamaño +1;
                            System.out.println(b64enc.length());
                            offset=0;
                            count=0;
                            obj.put("action","get");

                            while (count<parte){
                                obj.put("parte",count+1);
                                frag = b64enc.substring(offset,offset+tamaño);
                                obj.put("file",frag);
                                response=obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                                System.out.println(response.length);
                                System.out.println(obj.toJSONString());
                                dos.writeInt(response.length);
                                count++;
                            }
                            dos.writeInt(-1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println("Error de sistema de archivos. Conexion terminada.");
                            break;
                        }
                    }
                    dos.writeUTF(obj.toJSONString());
                    obj.clear();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Conexion con cliente " + client_ip + " terminada de forma imprevista, cerrando.");
                    break;
                } 
            } 
    	}
    	else {
    		System.out.println("Password incorrecta, cerrando conexi�n"); 
            try {
				this.s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
        try{ // Cerrar recursos
            this.dis.close(); 
            this.dos.close();  
        }
        catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
}