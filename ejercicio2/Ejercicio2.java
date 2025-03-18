package ejercicio2;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Security;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class Ejercicio2 {

	public static void main(String[] args) {
		System.out.println("INICIO");
		Scanner teclado = new Scanner(System.in);
		String usuarioGMail;
		String contrasenaGMail;

		FTPClient clienteFTP = new FTPClient();
		String servidorFTP = "127.0.0.1";
		String usuarioFTP;
		String contrasenaFTP;

		System.out.println("Introduce tu dirección de Gmail: ");
		usuarioGMail = "luisdeblasmaster@gmail.com";
		System.out.println("Introduce tu contraseña de Gmail: ");
		contrasenaGMail = "zpdzkpzwpnlmnwrm";
		System.out.println("Introduce tu usuario FTP: ");
		usuarioFTP = "alumno1";
		System.out.println("Introduce tu contraseña FTP: ");
		contrasenaFTP = "alumno1";

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Message[] mensajes = null;
		try {

			clienteFTP.connect(servidorFTP);
			boolean login = clienteFTP.login(usuarioFTP, contrasenaFTP);
			if (!login) {
				System.out.println("Error en login al servidor FTP.");
				return;
			}

			String host = "imap.gmail.com";
			String puerto = "993";
			String fabricaSocketsSSL = "javax.net.ssl.SSLSocketFactory";
			//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());


			Properties propiedades = new Properties();
			propiedades.setProperty("mail.imap.socketFactory.class", fabricaSocketsSSL);
			propiedades.setProperty("mail.imap.socketFactory.fallback", "false");
			propiedades.setProperty("mail.imap.port", puerto);
			propiedades.setProperty("mail.imap.socketFactory.port", puerto);
			propiedades.setProperty("mail.imap.ssl.trust", "*");
			propiedades.setProperty("mail.store.protocol", "imaps");
			Session session = Session.getDefaultInstance(propiedades, new Authenticator() {

				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(usuarioGMail, contrasenaGMail);
				}
			});

			IMAPStore store = (IMAPStore) session.getStore("imaps");
			store.connect(host, usuarioGMail, contrasenaGMail);

			IMAPFolder inbox = (IMAPFolder) store.getFolder("inbox");
			inbox.open(Folder.READ_ONLY);

			int nummensajes = inbox.getMessageCount();
			System.out.println("numero de msjs de " + inbox.getName() + ":" + nummensajes);
			mensajes = new com.sun.mail.imap.IMAPMessage[nummensajes];
			mensajes = inbox.getMessages();
			

			final String[] meses = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO", "JULIO", "AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
			for (int i = mensajes.length-1; i > 0 && i > mensajes.length-100 && mensajes[i] != null; i--) {
				clienteFTP.changeWorkingDirectory("/");
				System.out.println("Mensaje " + (i + 1));
				Date fechaemail = mensajes[i].getSentDate();
				LocalDate date = fechaemail.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				int anio = date.getYear(); 
				int mes = fechaemail.getMonth(); ///comienza en 0 (se podría trabajar con LocalDatedate.getMonthValue() o date.getMonth())
				if (!clienteFTP.changeWorkingDirectory(""+anio)) {
					// crear directorio con el año
					clienteFTP.makeDirectory(""+anio);
					clienteFTP.changeWorkingDirectory(""+anio);
				}
				if (!clienteFTP.changeWorkingDirectory(""+meses[mes])) {
					// crear directorio con el mes
					clienteFTP.makeDirectory(""+meses[mes]);
					clienteFTP.changeWorkingDirectory(""+meses[mes]);
				}
				// crear fichero
				File fich = new File("mensaje_"+i+".txt");
				FileWriter fr = new FileWriter(fich);
				BufferedWriter bfr = new BufferedWriter(fr);
				
				bfr.write("Remitente:"+mensajes[i].getFrom()[0]+"\n");
				bfr.write("Asunto:"+mensajes[i].getSubject()+"\n");
				bfr.write("Fecha:"+mensajes[i].getSentDate()+"\n");
				bfr.write(""+mensajes[i].getContent().toString());
				bfr.close();
				fr.close();
				clienteFTP.setFileType(FTP.ASCII_FILE_TYPE);
				FileInputStream fis = new FileInputStream(fich);
				BufferedInputStream bis = new BufferedInputStream(fis);
				// subir fichero
				boolean subida = clienteFTP.storeFile("mensaje_"+i+".txt", bis);
				if (!subida) {
					System.out.println("Hubo un problema al subir el fichero.");
				}
				else {
					System.out.println("Se ha subido con exito el fichero: mensaje_"+i+".txt al servidor FTP.");
				}
			}
			inbox.close(true);
			store.close();
			clienteFTP.logout();
			clienteFTP.disconnect();
		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}
		System.out.println("FIN");
	}

}
