package vista;

import java.util.Scanner;
import logica.*;

public class TCPServerView {
	public static TCPServer tcpServer;
	public static void main(String[] args) throws Exception
	{
		Scanner linea = new Scanner(System.in);
		boolean fin = false; 
		while(!fin){
			printMenu();
			int option = linea.nextInt();
			switch(option){
			case 1: 
				System.out.println("Ingrese el puerto del servidor");
				int puerto = Integer.parseInt(linea.next());
				System.out.println("Ingrese numero de conexiones");
				int conexiones = Integer.parseInt(linea.next());
				System.out.println("Ingrese el archvio que desea enviar");
				int intArchivo = Integer.parseInt(linea.next());
				tcpServer = new TCPServer(puerto, conexiones, intArchivo);
				String resp = "";
				
				tcpServer.sendFile();
				fin = true;
				break;
			case 2: 
				fin = true;
				linea.close();
				break;
			default: 
				break;
			}
		}
	}
	private static void printMenu(){
		System.out.println("1: Iniciar Transmision");
		System.out.println("2: Salir");
	}
}
