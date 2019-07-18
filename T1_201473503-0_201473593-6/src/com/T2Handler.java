package com;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.net.DatagramSocket;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


class T2Handler extends Thread{
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final String srvpsw = "holasoyunapassword";

    final String[][] lista={{"prueba",".jpg","3","1,2"}};
    // Constructor
    public T2Handler(Socket s, DataInputStream dis, DataOutputStream dos) {
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
        JSONObject message = new JSONObject();
        JSONParser parser = new JSONParser();
        String psw = null;
        int ip_index = 14;
        String b64enc;
        byte[] response, income;
        String frag,name;
        int parte,port=4500;
        int offset,len;
        int count =0,totalbytes;
        String FILEPATH;
        FileReader fileReader;
        String[] split;
        byte[] buffer;
        int position;
        String msg;

        //ServerSocket archive_ss;
        //Socket archive_socket;
        //DataInputStream archive_in;
        //DataOutputStream archive_on;

        ///////////////////Obtener IP de cliente///////////////////
        for (int i=13;i<28;i++){
            if (s.toString().charAt(i)==','){
                ip_index = i;
                break;
            }
        }
        String client_ip = s.toString().substring(13,ip_index);


        ///////Agrega servidores almacenados en txt////////////////
        File servers = new File("Servers.txt");
        String st;
        BufferedReader br = null;
        String [] ServerArray = new String[100];
        try {
            br = new BufferedReader(new FileReader(servers));
            while((st = br.readLine()) != null){
                ServerArray[count] = st;
                count+=1;

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        count = 0;


        /////////////////Cheque de password o handshake//////////////////
        try {
            psw = dis.readUTF();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        assert psw != null;


        ////Con password correcta se inician las funciones de servidor intermedio////
        if(psw.equals(srvpsw)) {
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
                            name = objreceived.get("extra").toString();
                            File index = new File("Index.txt");
                            String str,concat;
                            BufferedReader brr = null;
                            String[] PartArray = new String[100];
                            count = 0;
                            try { //Revisa el archivo index para localizar todas las partes del archivo
                                brr = new BufferedReader(new FileReader(index));
                                while((str = brr.readLine()) != null){
                                    String[] splitted = str.split("\\s+");
                                    if (splitted[0].equals(name)){
                                        concat = splitted[1].concat("|").concat(splitted[2]);
                                        PartArray[count] = concat;
                                        count+=1;
                                    }

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //Hasta aca PartArray tiene indices de todas las partes del archivo solicitado
                            count = 0;
                            while (true){ //Ciclo que realiza sucesivas conexiones para obtener las partes
                                //Version de prueba solo busca el archivo y lo pasa al cliente.
                                if (PartArray[count]!=null) {
                                    //String tempip = "192.168.0.5";
                                    String tempindex = PartArray[count].split("\\|" )[1];
                                    Socket archive_socket = new Socket(ServerArray[Integer.parseInt(tempindex)-1], 6000);
                                    DataInputStream archive_in = new DataInputStream(archive_socket.getInputStream());
                                    DataOutputStream archive_out = new DataOutputStream(archive_socket.getOutputStream());
                                    //String request = "get".concat("|").concat(PartArray[count].split("|")[0]);
                                    //archive_out.writeUTF(request);

                                    //Desde aca empieza a mandar request a cada maquina virtual descargando partes
                                    String[] tempstringarray = PartArray[count].split("|");
                                    message.put("function", "get");
                                    System.out.println(PartArray[count].split("\\|")[0]);
                                    message.put("extra", PartArray[count].split("\\|")[0]);
                                    buffer = message.toJSONString().getBytes(StandardCharsets.UTF_8);

                                    //////Aca se envia request a maquina virtual
                                    try {
                                        archive_out.writeInt(buffer.length);
                                        archive_out.write(buffer, 0, buffer.length);
                                        message.clear();
                                    } catch (IOException var10) {
                                        System.out.println("Servidor no disponible. Cerrando...");
                                        break;
                                    }

                                    /////Se recibe recibe json object como bytes y se pasa sin conversion a cliente////
                                    try {
                                        len = archive_in.readInt();
                                        buffer = new byte[len];
                                        archive_in.read(buffer, 0, len);
                                        dos.writeInt(len);
                                        dos.write(buffer, 0, len);
                                        dis.readInt();
                                        dos.writeInt(-1);
                                        dis.readInt();
                                    } catch (NullPointerException e) {
                                        //e.printStackTrace();
                                        System.out.println("Comando invalido, intente nuevamente.");
                                        //break;
                                    }
                                    count+=1;
                                }
                                else{
                                    break;
                                }
                            }


                        case "gut":
                            try {
                                name = objreceived.get("extra").toString();
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

                            }catch (IOException e) {
                                //e.printStackTrace();
                                System.out.println("Error de sistema de archivos. Conexion terminada.");
                                break;
                            }
                            break;
                        case "put":

                            break;
                        case "delete":

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
}