package ejercicio3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class ServidorTCP2 extends Thread {
	final int port = 30502;

	@Override
	public void run() {
		ServerSocket ses;

		try {
			while (true) {
				System.out.println("Servidor2 preparado.");
				ses = new ServerSocket(port);
				Socket sockservidor1 = ses.accept();
				// Se obtiene un stream para leer objetos
				ObjectInputStream inObjeto = new ObjectInputStream(sockservidor1.getInputStream());
				/// recibimos la informacion desde servidor1
				InetAddress[] dirs = (InetAddress[]) inObjeto.readObject();
				int puertocli = inObjeto.readInt();
				InetAddress dircli = (InetAddress) inObjeto.readObject();
				/// cerramos el socket con servidor1
				sockservidor1.close();

				/// conectamos con el cliente para enviarle los File creados
				Socket cliente = new Socket(dircli, puertocli);
				ObjectOutputStream opSal = new ObjectOutputStream(cliente.getOutputStream());

				for (InetAddress add : dirs) {
					URL obj = new URL("http://" + add.getHostName());
					URLConnection conn = obj.openConnection();
					Map<String, List<String>> map = conn.getHeaderFields();
					String cuerpo = "";
					// Se vuelcan las cabeceras de la conexión sobre un String
					for (Map.Entry<String, List<String>> entry : map.entrySet()) {
						cuerpo += entry.getKey() + " : " + entry.getValue() + "\n";
					}
					File fich = new File("cabeceras_" + add.getCanonicalHostName() + ".txt");
					FileWriter fr = new FileWriter(fich);
					BufferedWriter bfr = new BufferedWriter(fr);
					bfr.write(cuerpo);
					bfr.flush();
					bfr.close();
					// Se envia el objeto File al cliente
					opSal.writeObject(fich);
					opSal.flush();

				}
				// se cierra el socket con el cliente
				opSal.close();
				cliente.close();
				ses.close();
			}
		} catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
			e.printStackTrace();
		}

	}
}
