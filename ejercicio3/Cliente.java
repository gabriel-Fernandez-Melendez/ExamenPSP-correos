package ejercicio3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class Cliente extends Thread {
	@Override
	public void run() {
		String hostservidor1 = "127.0.0.1";
		int puertoservidor1 = 30501;// puerto remoto del servidor1
		InetAddress miip = null;
		int puertoescuchaCliente = 30500;
		System.out.println("PROGRAMA CLIENTE INICIADO....");

		Scanner in = new Scanner(System.in);
		boolean resp = false;
		do {
			System.out.println("Introduzca una URL válida:");
			String url = in.nextLine();
			try {
				Socket cliente = new Socket(hostservidor1, puertoservidor1);
				miip = InetAddress.getLocalHost();
				// se prepara el socket hacia servidor1 y se le manda la url, la ip y el puerto
				DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());
				dos.writeUTF(url); //mandamos 1º la URL como String
				dos.flush();
				dos.writeInt(puertoescuchaCliente); //mandamos 2º el puerto como int
				dos.flush();
				dos.writeUTF(miip.getHostAddress()); //mandamos 3º la InetAddress propia como String
				dos.flush();
				//dos.close();
				///El cliente queda a la espera de la respuesta booleana desde el servidor1
				DataInputStream entradasocket = new DataInputStream(cliente.getInputStream());
				boolean urlcorrecta = entradasocket.readBoolean();
				if (!urlcorrecta) {
					System.out.println("El servidor1 dice que la URL es incorrecta.");
				} else {
					int numficheros = entradasocket.readInt();
					cliente.close(); /// cerramos el sockets con el servidorTCP1
					System.out.println("El servidor1 dice que la URL valida y tiene "+ numficheros+ " dirs InetAddress.");
					
					
					// El cliente espera los ficheros de vuelta desde servidor2
					ServerSocket clienteescucha;
					clienteescucha = new ServerSocket(puertoescuchaCliente);
					Socket socketvuelta = clienteescucha.accept();
					ObjectInputStream inObjeto = new ObjectInputStream(socketvuelta.getInputStream());
					
					FTPClient clienteFTP = new FTPClient();
					String servidorFTP = "127.0.0.1";
					String usuarioFTP = "alumno1";
					String contrasenaFTP = "alumno1";
					clienteFTP.connect(servidorFTP);
					boolean login = clienteFTP.login(usuarioFTP, contrasenaFTP);
					if (!login) {
						System.out.println("Error en login al servidor FTP.");
						return;
					}
					if (!clienteFTP.changeWorkingDirectory("cabeceras")) {
						// crear directorio cabeceras_url
						clienteFTP.makeDirectory("cabeceras");
						clienteFTP.changeWorkingDirectory("cabeceras");
					}
					for(int i=0; i<numficheros; i++) {
						File fich = null;
						fich =(File) inObjeto.readObject();
						clienteFTP.setFileType(FTP.ASCII_FILE_TYPE);
						FileInputStream fis = new FileInputStream(fich);
						BufferedInputStream bis = new BufferedInputStream(fis);
						// subir fichero
						boolean subida = clienteFTP.storeFile(fich.getName(), bis);
						if (!subida) {
							System.out.println("Hubo un problema al subir el fichero.");
						}
						else {
							System.out.println("Se ha subido con exito el fichero al servidor FTP.");
						}
					}
					System.out.println("Se han subido correctamtente los ficheros recibidos al servidor FTP.");
					///cerramos conexion con el servidor FTP
					clienteFTP.logout();
					clienteFTP.disconnect();
					clienteescucha.close(); ///cerramos el socket con servidor2
				}

			} catch (UnknownHostException e) {
				System.out.println("UnknownHostException: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException: " + e.getMessage());
				e.printStackTrace();
			}

			System.out.println("¿Desea repetir el proceso?:");
			resp = leerBoolean();
		} while (resp);

	}

	public static boolean leerBoolean() {
		boolean ret;
		Scanner in;
		char resp;
		do {
			System.out.println("Pulse s para Sí o n para No");
			in = new Scanner(System.in, "ISO-8859-1");
			in.reset();
			resp = in.nextLine().charAt(0);
			if (resp != 's' && resp != 'S' && resp != 'n' && resp != 'N') {
				System.out.println("Valor introducido incorrecto.");
			}
		} while (resp != 's' && resp != 'S' && resp != 'n' && resp != 'N');
		if (resp == 's' || resp == 'S') {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}
}
