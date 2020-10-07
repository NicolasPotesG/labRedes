package logica;

import java.io.*;
import java.net.*;

public class TCPServer {
	
	public final static int maxNumberConnections = 25;
	
	public final static String[] paths = {"./../data/100MiBFile.mp4","./../data/250MiBFile"};
	
	private int port;
	
	private static DataInputStream in = null; 
	
	private ServerSocket serverSocket;
	
	public int numberConnections;
	
	private TCPThread[] connections;
	
	public Socket[] sockets;
	
	private String path;
	
	public TCPServer(int pPort, int pNumberConnections, int fileNumber) throws Exception {
		File file = new File("./../data/FileNumeber.txt");
		if(!file.exists()) {
			file.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		int n = 0;
		String line = br.readLine();
		if(line!=null) {
			n = Integer.parseInt(line)+1;
		}
		PrintWriter writer = new PrintWriter(file);
		writer.print(""+n);
		writer.close();
		String logPath = "./../data/log"+n+".txt";
		file = new File(logPath);
		if(!file.exists()) {
			file.createNewFile();
		}
		
		writer = new PrintWriter(file);
		writer.print("");
		writer.close();
		path = paths[fileNumber];
		port = pPort;
		numberConnections = pNumberConnections>maxNumberConnections?maxNumberConnections:pNumberConnections;
		connections = new TCPThread[numberConnections];
		sockets = new Socket[numberConnections];
		try {
			serverSocket  = new ServerSocket(port);
			waitForConnections(logPath);
		}
		catch(Exception e) {
			System.out.println("Error creating server socket");
			e.printStackTrace();
		}
		
	}
	
	private void waitForConnections(String logPath) throws IOException{
		int connectionsEstablished = 0;
		System.out.println("Esperando nuevas conexiones");
		while(connectionsEstablished!=numberConnections) {
			Socket socket = serverSocket.accept();
			System.out.println("Nueva conexion aceptada");		
			
			
			System.out.println("Esperando notificacion del cliente...");
			DataInputStream linea = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			String resp = "";
			while(!resp.equals("Preparado")) {
				resp = linea.readUTF();			
			}
			System.out.println("Notificacion cliente: " + resp);
			
			
			TCPThread newConnection = new TCPThread(socket, connectionsEstablished, path, logPath);
			connections[connectionsEstablished]=newConnection;
			sockets[connectionsEstablished]= socket;
			connectionsEstablished++;
			System.out.println("Conexion # "+connectionsEstablished + " establecida");
		}
		System.out.println("Todas las conexiones se han establecido");
		
	}
	
	private void waitForConnections2() throws IOException{
		System.out.println("Waiting for new connection");
		Socket socket = serverSocket.accept();
		System.out.println("Connection established");
		
		
		System.out.println("Esperando notificacion del cliente...");
		DataInputStream linea = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		String resp = "";
		while(!resp.equals("Preparado")) {
			resp = linea.readUTF();			
		}
		System.out.println("Notificacion cliente: " + resp);
		
		File file = new File(path);
		
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis); 
		
		OutputStream os = socket.getOutputStream();
		
		byte[] contents;
        long fileLength = file.length(); 
        long current = 0;
        long start = System.nanoTime();
        while(current!=fileLength){ 
            int size = 10000;
            if(fileLength - current >= size)
                current += size;    
            else{ 
                size = (int)(fileLength - current); 
                current = fileLength;
            } 
            contents = new byte[size]; 
            bis.read(contents, 0, size); 
            os.write(contents);
            System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
        }   
        System.out.println("File sent succesfully!");
        os.flush(); 
        socket.close();
        serverSocket.close();
        System.out.println("Sockets closed");
		
	}
	
	public void sendFile() throws IOException {
		
		System.out.println("Iniciando envio de archivos");
		for(TCPThread connection: connections) {
			connection.start();
		}
		boolean socketsClosed = false;
		
		while(!socketsClosed) {
			boolean sock = true;
			for(Socket connection: sockets) {
				sock = sock && connection.isClosed();
			}
			socketsClosed = sock;
		}
		System.out.println("Archivos enviados");
		serverSocket.close();
		System.out.println("Servidor cerrado");
	}

}
