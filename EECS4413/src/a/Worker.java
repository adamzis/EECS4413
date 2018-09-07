package a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Worker {

	Socket client;
	TCPServer server;

	public Worker(TCPServer server) {
	}

	public void handle(Socket client) {
		this.client = client;
		System.out.println("CLIENT CONNECTED");
		bye();
		System.out.println("CLIENT DISCONNECTED");
	}

	private void bye() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
