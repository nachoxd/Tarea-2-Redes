package com;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
	
	public static void main (String[] args) {
		try
        { 
			Scanner user_input = new Scanner( System.in );
            final String srvpsw = "holasoyunapassword";
            String mensaje,envio;
            JSONObject message = new JSONObject();
			JSONParser parser;
            // obtener ip
            InetAddress ip = InetAddress.getByName("localhost");

            String msg;
            double largo;
            int len;
            int packets;
            int totalbytes;
            int position;
            byte[] buffer;

            // establecer conexion con puerto 5000
            Socket s = new Socket(ip, 5000); 
      
            // Obtener input y output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            // El loop de a continuacion realiza el intercambio de
            // informacion entre cliente y client handler
            dos.writeUTF(srvpsw);
//            message.put("mensaje",dis.readUTF());
        	mensaje = dis.readUTF();
        	if (mensaje.equals("OK")) { //si el password corresponde, se inicia conexion constante
        		while (true){
					parser = new JSONParser();
					System.out.println("Ingrese comando:");
					System.out.println("ls | get | exit");
        			envio = user_input.next( );
        			/////////Bloque que maneja error de escritura cuando crashea servidor/////////////
					try {
						dos.writeUTF(envio);
					} catch (IOException var10) {
						System.out.println("Servidor no disponible. Cerrando cliente.");
						break;
					}

        			//
        			try{
                        len = dis.readInt();
                        buffer = new byte[len];


						if(envio.equals("ls")){
						    dis.read(buffer, 0, len);
                            msg = new String(buffer,0,len,StandardCharsets.UTF_8);
                            message = (JSONObject) parser.parse(msg);
							ls(message);
						}
						else if (envio.equals("get")) {
							String file="";
                            while(len != -1) {
                                totalbytes = dis.read(buffer, 0, len);
                                position = totalbytes;
                                while (totalbytes > 0) {
                                    totalbytes = dis.read(buffer, position, (len - position));
                                    if (totalbytes >= 0) position += totalbytes;
                                }
                                msg = new String(buffer, 0, len, StandardCharsets.UTF_8);
                                message = (JSONObject) parser.parse(msg);
                                file+= message.get("file").toString();

                                dos.writeInt(1);
                                len = dis.readInt();
                            }
                            dos.writeInt(1);
                            get(file);
						}
						else if (envio.equals("exit")) {
							s.close();
							break;
						}
					}
					catch(NullPointerException e){
						//e.printStackTrace();
						System.out.println("Comando invalido, intente nuevamente.");
						//break;
					}
                    ////////////Bloque que maneja excepcion en JSON al cerrar con exit y otros///////
                    catch(IOException e){
                        //e.printStackTrace();
                        System.out.println("Conexion con servidor terminada.");
                        break;
                    }
                }
        	}
        	else {
        		System.out.println("Password incorrecta, iniciar nueva conexion");
        		s.close();
        	}
              
            // closing resources 
            user_input.close(); 
            dis.close(); 
            dos.close(); 
        }
		catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
	private static int ls(JSONObject response){
		JSONArray files =(JSONArray) response.get("lista");
		Iterator<String> iterator = files.iterator();

		while (iterator.hasNext()){
			System.out.println(iterator.next());
		}
		return 1;
	}


	private static int get(String message){
		byte[] dbase64;
		File file;
		FileOutputStream fos;
		String name;
		String content;
		name = "Resultado.jpg";
		content = message.replace("#","");
		dbase64 = Base64.getDecoder().decode(content);
		file = new File(name);
		try {
			fos = new FileOutputStream(file);
			fos.write(dbase64);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	private  static int put(JSONObject response){

		byte[] dbase64;
		File file;
		FileWriter writer;
		FileOutputStream fos;
		String segment;
		String name;
		String name2;
		try {
			name = "./prueba_"+response.get("parte")+".txt";

			segment = response.get("file").toString();
			System.out.println((response.get("file").toString().length()));
			writer = new FileWriter(name);
			writer.write(segment);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;

	}
}
