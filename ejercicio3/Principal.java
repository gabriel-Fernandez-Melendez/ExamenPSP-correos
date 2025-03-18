package ejercicio3;

public class Principal {
	public static void main(String[] args0) {
		Thread servidor1 = new ServidorTCP1();
		Thread servidor2 = new ServidorTCP2();
		Thread cliente = new Cliente();
		servidor1.start();
		servidor2.start();
		cliente.start();
	}
}
