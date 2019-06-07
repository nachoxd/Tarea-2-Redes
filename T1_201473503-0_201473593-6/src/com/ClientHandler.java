package com;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

    final String[][] lista={{"prueba",".jpg","3","1,2"}};
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
        JSONObject objreceived;
        JSONParser parser = new JSONParser();
        String psw = null;
        int ip_index = 14;
        String b64enc;
        byte[] response, income;
        String frag;
        int parte;
        int offset,len;
        int count, totalbytes;
        String FILEPATH;
        FileReader fileReader;
        String[] split;

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
        assert psw != null;
        if(psw.equals(srvpsw)) { //chequeo de password para ingresar, intercambio de info de control, "handshake"
    		try {
				dos.writeUTF("OK");
			} catch (IOException e1) {
				e1.printStackTrace();
			}  //"OK" es el mensaje que el cliente espera para proceder, si recibe otro valor cierra la conexion
            label:
            while (true){
                try {
                // Recibir comando de cliente
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
                System.out.println("Cliente "+client_ip+" | Input "+received+" recibido");
                    switch (objreceived.get("function").toString()) {
                        case "exit":
                //this.dos.writeUTF("Exit");
                            System.out.println("Cliente " + client_ip + " termina conexion.");
                            this.s.close();
                            break label;
                        case "ls":
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


                            obj.put("action", "ls");
                            obj.put("lista", list);
                            response = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                            dos.writeInt(response.length);
                            dos.write(response, 0, response.length);
                            break;
                        case "get":
                            try {
                                String name = objreceived.get("extra").toString();
                                FILEPATH = "./"+name;
                                int cantidad = Integer.parseInt(lista[0][2]);
                                String type = lista[0][1];
                                count = 1;
                                int i;
                                String segment = "";
                                obj.put("action", "get");
//                                obj.put("name", lista[0][0] + type);
                                obj.put("name", name);
                                /*while (count <= cantidad) {
                                    fileReader = new FileReader(FILEPATH + "_" + count + ".txt");

                                    while ((i = fileReader.read()) != -1) {
                                        segment += (char) i;
                                    }
                                    obj.put("file", segment);
                                    response = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                                    dos.writeInt(response.length);
                                    dos.write(response, 0, response.length);
                                    dis.readInt();
                                    fileReader.close();
                                    segment = "";
                                    count++;
                                }
                                */

                                File file = new File(FILEPATH);
                                byte[] content = Files.readAllBytes(file.toPath());
                                b64enc = Base64.getEncoder().encodeToString(content);
                                obj.put("file", b64enc);
                                response = obj.toJSONString().getBytes(StandardCharsets.UTF_8);
                                dos.writeInt(response.length);
                                dos.write(response, 0, response.length);
                                dis.readInt();


                                dos.writeInt(-1);
                                dis.readInt();

                            } catch (IOException e) {
                //e.printStackTrace();
                                System.out.println("Error de sistema de archivos. Conexion terminada.");
                                break;
                            }
                            break;
                        case "put":

                            break;
                        case ("delete"):

                            break;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Conexion con cliente " + client_ip + " terminada de forma imprevista, cerrando.");
                    break;
                }
                catch (ParseException e) {
                    e.printStackTrace();
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

    private String get(String file){

	    return "";
    }

//    private  static int put(JSONObject response){
//
//        byte[] dbase64;
//        File file;
//        FileWriter writer;
//        FileOutputStream fos;
//        String segment;
//        String name;
//        int parte;
//        int offset;
//        int count;
//
//        String FILEPATH="";
//        file = new File(FILEPATH);
//        byte[] content = Files.readAllBytes(file.toPath());
//        System.out.println(content.length);
//
//        b64enc = Base64.getEncoder().encodeToString(content);
//        float tamano =65536f;
//
//        parte = (int)Math.ceil(b64enc.length()/tamano);
//        if (b64enc.length()<tamano*parte){
//
//            int needed = ((int)tamano*parte)-b64enc.length();
//            char[] filler = new char[needed];
//            Arrays.fill(filler, '#');
//            b64enc+= new String(filler);
//        }
//        System.out.println(b64enc.length());
//        offset=0;
//        count=0;
//
//
//        while (count<parte){
//            obj.put("parte",count+1);
//            frag = b64enc.substring(offset,offset+(int)tamano);
//            obj.put("file",frag);
//            //System.out.println("Largo es"+frag.length());
//            response=obj.toJSONString().getBytes(StandardCharsets.UTF_8);
//            System.out.println(response.length);
//            System.out.println(obj.toJSONString());
//            dos.writeInt(response.length);
//            dos.write(response,0,response.length);
//            dis.readInt();
//            offset+=tamano;
//
//            count++;
//        }
//        dos.writeInt(-1);
//
//
//        try {
//            name = "./prueba_"+response.get("parte")+".txt";
//            name2 = "./prueba_"+response.get("parte")+".jpg";
//            segment = response.get("file").toString();
////			dbase64 = Base64.getDecoder().decode(segment.trim());
////			file = new File(name2);
////
////			fos = new FileOutputStream(file);
////			fos.write(dbase64);
//            System.out.println((response.get("file").toString().length()));
//            writer = new FileWriter(name);
//            writer.write(response.get("file").toString());
//            writer.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 1;
//
//    }
}