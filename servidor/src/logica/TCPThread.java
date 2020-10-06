package logica;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TCPThread extends Thread{
	
	private int threadId;
	
	private Socket socket;
	
	private String path;
	
	private String client;
	
	private double seconds = 0;
	
	private File file;
	
	private String logPath ;
	
	
	public TCPThread(Socket pSocket, int pId, String pPath, String pLogPath) {
		socket = pSocket;
		threadId = pId;
		path = pPath;
		client = socket.getInetAddress().getHostAddress();
		logPath = pLogPath;
	}
	
	public void start(){
		try {
			file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis); 
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			DataOutputStream dos = new DataOutputStream(os);
			DataInputStream dis = new DataInputStream(is);
			
	        long start = System.nanoTime();
	        String CK = getFileChecksum();
	        dos.writeUTF(CK);
	        dos.flush();
	        String resp = "";
	        while(!resp.equals("CK recibido")) {
	        	resp = dis.readUTF();
	        }
	        dos.writeUTF(file.getName());
	        dos.flush();
	        while(!resp.equals("Nombre recibido")) {
	        	resp = dis.readUTF();
	        }
	        byte[] contents;
	        long fileLength = file.length();
	        dos.writeUTF(fileLength+"");
	        dos.flush();
	        while(!resp.equals("Tamano recibido")) {
	        	resp = dis.readUTF();
	        }
	        long current = 0;
	        while(current!=fileLength){ 
	            int size = 20000;
	            if(fileLength - current >= size)
	                current += size;    
	            else{ 
	                size = (int)(fileLength - current); 
	                current += size; 
	            } 
	            contents = new byte[size]; 
	            bis.read(contents, 0, size); 
	            os.write(contents);
	            //System.out.println((double)current/(double)fileLength);
	        }
	        while(!(resp.equals("Falla")||resp.equals("Exito"))) {
	        	resp = dis.readUTF();
	        }System.out.println("Llego confirmacion");
	        long end = System.nanoTime();
	        seconds = (end-start)/(double)1000000000;
	        log(resp);
	        os.flush(); 
	        socket.close();
	        System.out.println("Envio completado");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void log(String estadoEnvio) throws Exception {
		File logFile = new File(logPath);
		PrintWriter outLog = new PrintWriter(new FileWriter(logFile, true));
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		outLog.println("");
		outLog.println("Cliente: "+client);
		outLog.println("Nombre archivo enviado: "+file.getName());
		outLog.println("Tamano archivo: "+file.length()+" Bytes");
		outLog.println("Fecha: "+ dateFormat.format(date));
		outLog.println("Tiempo: "+ seconds+" seg");
		outLog.println("Envio "+ estadoEnvio);
		outLog.println("");
		outLog.close();
	}
	
	private String getFileChecksum() throws IOException, NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
	    FileInputStream fis = new FileInputStream(file);
	    byte[] byteArray = new byte[1024];
	    int bytesCount = 0; 
	    while ((bytesCount = fis.read(byteArray)) != -1) {
	        digest.update(byteArray, 0, bytesCount);
	    };
	    fis.close();
	    byte[] bytes = digest.digest();
	    StringBuilder sb = new StringBuilder();
	    for(int i=0; i< bytes.length ;i++)
	    {
	        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	   return sb.toString();
	}
	
	
}
