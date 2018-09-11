package a;

import java.io.IOException;
import java.net.Socket;

public class Worker extends Thread{

	private Socket client;
	private TCPServer server;
	
	private String clientIp;

	public Worker(TCPServer server, Socket client) {
		this.server = server;
		this.client = client;
	}

	public void run()
	{
		this.handle();
	}
	
	/**
	 * Handles a connected client socket throughout the lifetime of the connection
	 * 
	 * @param client a client socket
	 */
	public void handle() {
		clientIp = this.client.getInetAddress().toString();

		server.insertLogEntry("Client Connnected", this.clientIp);

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
