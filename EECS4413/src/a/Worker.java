package a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;

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
	 * @param client
	 *            a client socket
	 * @throws IOException
	 */
	public void handle() throws IOException {
		clientIp = this.client.getInetAddress().toString();
		server.insertLogEntry("Client Connnected", this.clientIp);
		boolean connected = true;

		while (connected) {
			connected = clientServComm();
		}

		bye();
		System.out.println("CLIENT DISCONNECTED");
	}

	// Fix these atrocious names and refactor heavily
	private boolean clientServComm() throws IOException {
		PrintStream clientOutput = new PrintStream(client.getOutputStream());
		BufferedReader clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));

		System.out.println("CLIENT CONNECTED TYPE SOMETING");

		String clientString = clientInput.readLine();
		System.out.println(clientString);
		clientOutput.println("YOU WROTE " + clientString);

		if (clientString.compareToIgnoreCase("exit") != 0) {
			return true;
		} else {
			return false;
		}

	}

	private void bye() {
		try {
			client.close();
		} catch (IOException e) {
			server.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
		}

		server.insertLogEntry("Client Disconnected", clientIp);
	}
}
