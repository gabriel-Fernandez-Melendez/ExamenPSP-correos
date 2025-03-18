package ejercicio3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class ServidorTCP1 extends Thread {
	final int port = 30501;
	String ipservidor2 = "127.0.0.1";
	int puertoServidorTCP2 = 30502;

	@Override
	public void run() {

		try {
			while (true) {

				System.out.println("Servidor1 preparado.");
				ServerSocket ses = new ServerSocket(port);
				Socket sockCli = ses.accept();

				System.out.println("Recibo peticion de: " + sockCli.getInetAddress());
				DataInputStream in = new DataInputStream(sockCli.getInputStream());
				String url = in.readUTF();
				int puertocli = in.readInt();
				String ipcli = in.readUTF();
				boolean urlcorrecta = analizarURL(url);

				DataOutputStream out = new DataOutputStream(sockCli.getOutputStream());
				if (urlcorrecta) {
					out.writeBoolean(true);
					URL obj = new URL(url);
					URLConnection conn = obj.openConnection();
					InetAddress[] dirs = InetAddress.getAllByName(obj.getHost());
					out.writeInt(dirs.length);
					/// conectamos con el servidor2 y le mandamos la coleccion de dirs asociadas a
					/// la url del cliente

					Socket s = new Socket(ipservidor2, puertoServidorTCP2);
					ObjectOutputStream opSal = new ObjectOutputStream(s.getOutputStream());
					// Se envia el objeto con la coleccion de direcciones para la url
					opSal.writeObject((InetAddress[]) dirs);
					// se envia el int con el puerto de escucha del cliente original
					opSal.writeInt(puertocli);
					// se envia la dir del cliente original
					opSal.writeObject((InetAddress) InetAddress.getByName(ipcli));
					s.close();
				} else {
					out.writeBoolean(false);
				}
				/// cerramos el socket con el cliente
				sockCli.close();
				ses.close();
			}
		} catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
			e.printStackTrace();
		}

	}

	private boolean analizarURL(String url) {
		boolean ret = false;
		URL obj;
		try {
			obj = new URL(url);
			obj.openConnection();
			InetAddress.getAllByName(obj.getHost());
			ret = true;
		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
		}

		return ret;
	}
}
