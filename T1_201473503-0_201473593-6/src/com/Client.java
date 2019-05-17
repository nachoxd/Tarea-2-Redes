package com;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
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
					////////////Bloque que maneja excepcion en JSON al cerrar con exit y otros/////////
        			try{
						message = (JSONObject)parser.parse(dis.readUTF());
					}
        			catch(IOException e){
						e.printStackTrace();
						System.out.println("Conexion con servidor terminada.");
						break;
					}
        			//
        			try{
						String txt = message.get("action").toString();
						if(txt.equals("ls")){
							ls(message);
						}
						else if (txt.equals("get")) {
							get(message);
						}
						else if (txt.equals("exit")) {
							s.close();
							break;
						}
					}
					catch(NullPointerException e){
						e.printStackTrace();
						System.out.println("Comando invalido, intente nuevamente.");
						//break;
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

	private  static int get(JSONObject response){

		byte[] dbase64;
		FileOutputStream fos;
		try {
			System.out.println("holiwis");
			fos = new FileOutputStream("./prueba1.jpg");

			dbase64 = Base64.getDecoder().decode((String)response.get("file"));
			fos.write(dbase64);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;

	}
}
