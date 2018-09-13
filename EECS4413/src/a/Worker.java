package a;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		clientIp = client.getInetAddress().toString();
		server.insertLogEntry("Client Connnected", clientIp);

		System.out.println("Client Connected");

		clientIO();
		bye();

		System.out.println("Client Disconnected");
	}

	private void clientIO() throws IOException {
		PrintStream clientOutput = new PrintStream(client.getOutputStream());
		Scanner clientScanner = new Scanner(client.getInputStream());

		String clientInput = clientScanner.nextLine();
		boolean closeClient = false;

		while (!closeClient) {
			clientOutput.println("Please enter a command, type 'bye' to exit");
			clientInput = clientScanner.nextLine();
			clientOutput.println("You entered " + clientInput + " processing\n");

			closeClient = clientCommand(clientInput, clientOutput);
		}

		clientOutput.println("Disconnecting from server, have a nice day");
		clientScanner.close();
	}

	// Input from client is passed to this method, which calls the appropriate
	// methods
	private boolean clientCommand(String clientInput, PrintStream clientOutput) {
		boolean exitMatch = false;

		Pattern exitPattern = Pattern.compile("(\\s*)(bye)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher exitMatcher = exitPattern.matcher(clientInput);
		Pattern timePattern = Pattern.compile("(\\s*)(get)(\\s*)(time)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher timeMatcher = timePattern.matcher(clientInput);

		if (exitMatcher.matches()) {
			exitMatch = true;
		} else if (timeMatcher.matches()) {
			clientOutput.println(server.getTime());
		}

		return exitMatch;
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
