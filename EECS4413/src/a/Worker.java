package a;

import java.io.IOException;
import java.net.Socket;

public class Worker {

	private Socket client;
	private TCPServer server;
	private String clientIp;

	public Worker(TCPServer server) {
		this.server = server;
	}

	/**
	 * Handles a connected client socket throughout the lifetime of the connection
	 * 
	 * @param client a client socket
	 */
	public void handle(Socket client) {
		this.client = client;
		clientIp = client.getInetAddress().toString();

		server.insertLogEntry("Client Connnected", clientIp);

		System.out.println("CLIENT CONNECTED");
		bye();
		System.out.println("CLIENT DISCONNECTED");
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
