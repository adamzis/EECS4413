package a;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;
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
		
		clientOutput.println("Please enter a command, type 'bye' to exit");

		while (!closeClient) {
			clientInput = clientScanner.nextLine();
			clientOutput.println("You entered " + clientInput + " processing\n");

			closeClient = clientCommand(clientInput, clientOutput);
		}

		clientOutput.println("Disconnecting from server, have a nice day");
		clientScanner.close();
	}

	/**
	 * Parses the client input using regex
	 * 
	 * @param clientInput
	 *            the input string passed from the client to the server
	 * @param clientOutput
	 *            a reference to the printstream object to return called method
	 *            outputs
	 * @return true if the client enters 'bye', false otherwise
	 */
	private boolean clientCommand(String clientInput, PrintStream clientOutput) {
		boolean exitMatch = false;
		Pattern primePattern = Pattern.compile("(\\s*)(prime)(\\s*)(\\d+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher primeMatch = primePattern.matcher(clientInput);

		if (clientInput.matches("(?i)(\\s*)(bye)(\\s*)")) {
			exitMatch = true;
		} else if (clientInput.matches("(?i)(\\s*)(get)(\\s*)(time)(\\s*)")) {
			clientOutput.println(server.getTime());
		} else if(primeMatch.matches()) {
			clientOutput.println(this.prime(primeMatch.group(4)));
		}else {
			clientOutput.println("Unknown input, please enter a valid input");
		}

		return exitMatch;
	}

	private long prime(String digitsStr) {
		int digits = Integer.parseInt(digitsStr);
		Random rnd = new Random();
		int bitsToDec = (int) (3.33 * digits);
		
		BigInteger bigPrime = BigInteger.probablePrime(bitsToDec, rnd);
		long primeNum = bigPrime.longValue();
		return primeNum;
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
