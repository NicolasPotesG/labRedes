package logica;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;
import javax.xml.crypto.Data;


public class ClientePrueba {
	
	private static DataOutputStream out = null;
	
	private static String ip = "192.168.1.10";
	
	public static void main(String[] args) throws Exception{
        Socket socket = new Socket(ip, 5000);   
        out = new DataOutputStream(socket.getOutputStream());

        System.out.println("Conexión establecida");
        out.writeUTF("Preparado");
        System.out.println("Preparado para recepción de archivos");
        
        byte[] contents = new byte[10000];
        
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        DataInputStream dis = new DataInputStream(is);
        DataOutputStream dos = new DataOutputStream(os);
        long start = System.nanoTime();
       	String resp = dis.readUTF();
        String CK = resp;
        dos.writeUTF("CK recibido");
        dos.flush();
        resp = dis.readUTF();
        String fileName = resp;
        dos.writeUTF("Nombre recibido");
        dos.flush();
        resp = dis.readUTF();
        long lengthFile = Long.parseLong(resp);
        dos.writeUTF("Tamano recibido");
        dos.flush();
        
        FileOutputStream fos = new FileOutputStream("./dataPrueba/"+fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = 0;
        
        System.out.println("RECEPCION ARCHIVO");
        
        while (is.available()>0){
        	
        	bytesRead=is.read(contents);
        	bos.write(contents, 0, bytesRead);
        	Thread.sleep(0,1);
		} ;
        bos.flush();
        System.out.println("Archivo recibido");
        String CKcalculado = getFileChecksum("./dataPrueba/"+fileName);
        String estado;
        if(CKcalculado.equals(CK)) {
        	estado = "Exito";
        }
        else {
        	estado ="Falla";
		}
        dos.writeUTF(estado);
    	dos.flush();
    	long end = System.nanoTime();
    	double seconds = (end-start)/(double)1000000000;
        log("./dataPrueba/"+fileName, estado, seconds);
        
        socket.close(); 
        System.out.println("El archivo se guardó exitosamente");
    }
	
	private static String getFileChecksum(String path) throws IOException, NoSuchAlgorithmException
	{
		File file = new File(path);
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
	
	public static void log(String nombreArchivo, String estado, double seconds) throws Exception {
		File file = new File("./dataPrueba/FileNumeber.txt");
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
		String logPath = "./dataPrueba/log"+n+".txt";
		file = new File(logPath);
		if(!file.exists()) {
			file.createNewFile();
		}
		writer = new PrintWriter(file);
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		writer.println("");
		writer.println("Cliente: "+ip);
		writer.println("Nombre archivo enviado: "+file.getName());
		writer.println("TamaÃ±o archivo: "+file.length()+" Bytes");
		writer.println("Fecha: "+ dateFormat.format(date));
		writer.println("Tiempo: "+ seconds+" seg");
		writer.println("Envio "+ estado);
		writer.println("");
		writer.close();
	}
	

}
