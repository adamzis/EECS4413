package a;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Worker extends Thread {

	private Socket client;
	private TCPServer server;

	private String clientIp;

	public Worker(TCPServer server, Socket client) {
		this.server = server;
		this.client = client;
	}

	public void run() {
		try {
			handle();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Handles a connected client socket throughout the lifetime of the connection
	 * 
	 * @param client a client socket
	 * @throws IOException
	 */
	public void handle() throws IOException {
		clientIp = client.getInetAddress().toString();
		server.insertLogEntry("Client Connnected", clientIp);

		System.out.println("Client Connected");

		clientCommand();
		bye();

		System.out.println("Client Disconnected");
	}

	private void clientCommand() {
		PrintStream clientOutput = null;
		Scanner clientScanner = null;

		try {
			clientOutput = new PrintStream(client.getOutputStream());
			clientScanner = new Scanner(client.getInputStream());
		} catch (IOException e) {
			bye();
		}

		clientOutput.println("Please enter a command, type 'exit' to exit");
		String clientString = clientScanner.nextLine();

		System.out.println("Client entered " + clientString);
		clientOutput.println("You entered " + clientString);

		while (clientString.compareToIgnoreCase("exit") != 0) {
			clientString = clientScanner.nextLine();
			System.out.println(clientString);
			clientOutput.println("You entered " + clientString);
		}

		clientOutput.println("Exiting");
		clientScanner.close();
	}

	private void bye() {
		try {
			client.close();
		} catch (IOException e) {
			server.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + " shutting down");
			System.exit(1);
		}

		server.insertLogEntry("Client Disconnected", clientIp);
	}
}
