package com;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

	public static void main(String[] args) {
		ServerSocket servidor = null;
		final int puerto = 5000;
		int ip_index = 14;
		//holiwi
		///////////////////////////////////////////////////////////////////
		
		//////////////////Aca escucha a clientes y dirige a los threads////////////////////
		try {
			servidor = new ServerSocket(puerto);
			System.out.println("Servidor iniciado");
			while(true) { //Loop infinito para atender clientes y dirigir hacia threads
				Socket s = null;
				try 
	            { 
	                // socket object que recibe llamadas de cliente 
	                s = servidor.accept();
					for (int i=13;i<28;i++){
						if (s.toString().charAt(i)==','){
							ip_index = i;
							break;
						}
					}
	                //Se separa la ip específica del cliente para utilizarla como ID
	                String client_ip = s.toString().substring(13,ip_index);
	                System.out.println("Se ha conectado cliente: " + client_ip);
	                // Obtener input y output streams
	                DataInputStream dis = new DataInputStream(s.getInputStream()); 
	                DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
	                System.out.println("Asignando nuevo Thread para el cliente");	
	                // Se crea nuevo thread con el puerto y los input/output stream
	                Thread t = new ClientHandler(s, dis, dos); 
	  
	                // Invoking the start() method 
	                t.start();  
	            } 
	            catch (Exception e){ 
	                s.close(); 
	                e.printStackTrace(); 
	            } 
			}
		}
		catch (IOException ex) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE,null,ex);
		}

	}

}
