package a;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import projA.Course;
import projA.Util;

public class Worker implements Runnable {

	private TCPServer server;
	private Socket client;

	private String clientIp;
	private Map<String, String> authMap;

	/**
	 * Instantiates a worker to manage the socket, while leaving the server free to
	 * connect to other clients. Creates an authorization Map and implements dummy
	 * usernames and passwords for testing.
	 * 
	 * @param server
	 *            stores a reference to the TCP server for accessing methods
	 * @param client
	 *            the socket corresponding to the connected client
	 */
	public Worker(TCPServer server, Socket client) {
		this.server = server;
		this.client = client;
		authMap = new HashMap<>();

		// garbage security
		authMap.put("root", "root");

		// garbage password
		authMap.put("Daud", "badpassword");
	}

	/**
	 * Each client should be connected on it's own thread. This method delegates to
	 * the handle method and catches all IO exceptions that may occur in the handle
	 * method.
	 */
	public void run() {
		try {
			handle();
		} catch (IOException e) {
			server.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + " shutting down");
			System.exit(1);
		}
	}

	/**
	 * Handles a connected client socket throughout the lifetime of the connection
	 * by first inserting a log entry with the client's IP, printing when the client
	 * connects and disconnects, and initiating IO between the client and server.
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
	}

	/**
	 * Responsible for sending and receiving input and output, to and from the
	 * client respectively. Sends the user input to the parseClient method and
	 * continues to ask for input until the parseClient method returns false, which
	 * occurs when the client enters 'exit' or 'bye'.
	 * 
	 * @throws IOException
	 *             if the server cannot get the client input or output stream.
	 */
	private void clientIO() throws IOException {
		PrintStream clientOutput = new PrintStream(client.getOutputStream());
		Scanner clientScanner = new Scanner(client.getInputStream());
		String clientInput;

		long start, end;
		double seconds;

		boolean keepParse, first;
		first = keepParse = true;
		boolean isBot = false;

		while (keepParse) {
			start = System.currentTimeMillis();
			clientOutput.println("Please enter a command, type 'bye' to exit");
			clientInput = clientScanner.nextLine();

			end = System.currentTimeMillis() - start;
			seconds = (double) end / 1000.0;

			keepParse = parseClient(clientInput, clientOutput);
			if (seconds < 2.0 && !first) {
				clientScanner.close();
				client.close();

				server.insertLogEntry("Dropping the connection", clientIp);
				keepParse = false;
				isBot = true;
			}
			first = false;
		}

		if (!isBot) {
			clientOutput.println("Disconnecting from server, have a nice day");
			server.insertLogEntry("Client Disconnected", clientIp);
			clientScanner.close();
			bye();
		}

		// Apologies for messy method, if wasn't a test would refactor.
	}

	/**
	 * Parses the client input using regex and either calls the matching function
	 * and sends out to the client, or tells the client their input was not valid.
	 * 
	 * If the client enters bye, this method returns false.
	 * 
	 * @param clientInput
	 *            the input string passed from the client to the server
	 * @param clientOutput
	 *            a reference to the printstream object to return called method
	 *            outputs
	 * @return true if the client enters any command, false if the client enters bye
	 */
	private boolean parseClient(String clientInput, PrintStream clientOutput) {
		boolean queryClient = true;

		Pattern byePattern = Pattern.compile("(\\s*)(bye|exit)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher byeMatch = byePattern.matcher(clientInput);

		Pattern timePattern = Pattern.compile("(\\s*)(get)(\\s*)(time)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher timeMatch = timePattern.matcher(clientInput);

		Pattern primePattern = Pattern.compile("(\\s*)(prime)(\\s*)(\\d+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher primeMatch = primePattern.matcher(clientInput);

		Pattern authPattern = Pattern.compile("(\\s*)(auth)(\\s*)(\\S+)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher authMatch = authPattern.matcher(clientInput);

		Pattern rosterPattern = Pattern.compile("(\\s*)(roster)(\\s*)(\\S+)(\\s*)", Pattern.CASE_INSENSITIVE);
		Matcher rosterMatch = rosterPattern.matcher(clientInput);

		if (byeMatch.matches()) {
			queryClient = false;

		} else if (timeMatch.matches()) {
			String currTime = server.getTime();
			clientOutput.println(currTime + "\n");

		} else if (primeMatch.matches()) {
			int clientDigits = Integer.parseInt(primeMatch.group(4));
			String clientPrime = prime(clientDigits);

			clientOutput.println(clientPrime + "\n");

		} else if (authMatch.matches()) {
			String userNameInput = authMatch.group(4);
			String passInput = authMatch.group(6);

			clientOutput.println(auth(userNameInput, passInput));

		} else if (rosterMatch.matches()) {
			String courseNum = rosterMatch.group(4);
			String courseJson = roster(courseNum);
			System.out.println(courseJson);
			clientOutput.println(courseJson);
		} else {
			clientOutput.println("Don't understand <" + clientInput + ">\n");
		}

		return queryClient;

	}

	private String prime(int digits) {
		BigInteger prime;
		String stringPrime;
		Map<Integer, BigInteger> primeCache = server.getPrimeCache();

		if (primeCache.containsKey(digits)) {
			prime = primeCache.get(digits);
			stringPrime = prime.toString() + " (from cache)";
		} else {
			prime = makePrime(digits);
			primeCache.put(digits, prime);
			stringPrime = prime.toString();
		}

		return stringPrime;
	}

	private BigInteger makePrime(int digits) {
		final float BITS_PER_DIGIT = 3.00f;
		int bitsToDec = (int) (BITS_PER_DIGIT * digits);

		BigInteger bigPrime = BigInteger.probablePrime(bitsToDec, new Random());

		return bigPrime;
	}

	private String auth(String username, String password) {
		if (authMap.containsKey(username)) {
			String validPass = authMap.get(username);
			if (validPass.matches(password)) {
				return "You are in!";
			}
		}

		return "Auth Failure!";
	}

	private String roster(String courseNum) {
		Course course = Util.getCourse(courseNum);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String courseJson = gson.toJson(course);

		return courseJson;
	}

	private void bye() throws IOException {
		client.close();
	}
}
