package a;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class TCPServer {

	private PrintStream log;
	private Set<InetAddress> firewall;
	ServerSocket server;

	public TCPServer(int port, PrintStream log, Set<InetAddress> firewall) throws IOException {

		this.log = log;
		this.firewall = firewall;
		this.server = new ServerSocket(port);

		InetAddress localHost = InetAddress.getLoopbackAddress();

		insertLogEntry("\nServer Start", ipPortToString(server.getInetAddress(), port));

		punch(localHost);

	}

	public void listen() {
		while (!server.isClosed()) {
			Socket client = createClient(server);

			if (allowedIp(client)) {
				Worker clientHandler = new Worker(this, client);
				Thread workerThread = new Thread(clientHandler);
				workerThread.start();
			} else {
				firewallViol(client);
			}
		}
	}

	public void closeServer() {
		try {
			server.close();
			insertLogEntry("Server Shutdown", null);
		} catch (IOException e) {
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println("Error " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Adds an IP Address to the fire wall for authorization.
	 * 
	 * @param inetAddress an IP address that should be allowed to connect.
	 */
	public void punch(InetAddress inetAddress) {
		firewall.add(inetAddress);
	}

	/**
	 * Removes a previously authorized IP address from the fire wall.
	 * 
	 * @param inetAddress an IP address that should no longer be allowed to connect.
	 */
	public void plug(InetAddress inetAddress) {
		firewall.remove(inetAddress);
	}

	/**
	 * Inserts an entry into the log file with the server's date and time.
	 * 
	 * @param entry    main event that occurred (Server start, client connection,
	 *                 etc).
	 * @param subEntry additional information such as the client's IP address and
	 *                 port.
	 */
	public void insertLogEntry(String entry, String subEntry) {
		log.println(entry + " " + "(" + subEntry + ")" + " - " + getTime());
	}

	/**
	 * Retrieves the date and time of the server formatted into a string.
	 * 
	 * @return A String containing the server's date and time.
	 */
	public String getTime() {
		ZonedDateTime currTime = ZonedDateTime.now();
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("E d MMM yyyy HH:mm:ss z");

		String formattedTime = currTime.format(timeFormat);
		return formattedTime;
	}

	private Socket createClient(ServerSocket server) {
		Socket client = null;

		try {
			client = server.accept();
		} catch (IOException e) {
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + " shutting down");
			System.exit(1);
		}

		return client;
	}

	private String ipPortToString(InetAddress ip, int port) {
		String ipPort = ip.toString() + ":" + Integer.toString(port);

		return ipPort;
	}

	private void firewallViol(Socket client) {
		insertLogEntry("Firewall Violation", client.getInetAddress().toString());

		try {
			PrintStream clientOutput = new PrintStream(client.getOutputStream());
			clientOutput.println("You are not authorized, closing");
			client.close();
		} catch (IOException e) {
			insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println(e.getMessage() + " shutting down");
			System.exit(1);
		}
	}

	private boolean allowedIp(Socket client) {
		InetAddress clientIp = client.getInetAddress();
		boolean allowedIp = firewall.contains(clientIp);

		if (allowedIp) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws IOException {

		int listenPort = 10560;

		String userHome = System.getProperty("user.home");
		File logFile = new File(userHome, "/Documents/4413/log.txt");

		Set<InetAddress> firewall = new HashSet<>();

		PrintStream log = new PrintStream(new FileOutputStream(logFile, true));

		System.out.println("Starting Server, connection port is " + listenPort);

		TCPServer theServer = new TCPServer(listenPort, log, firewall);
		theServer.listen();
		theServer.closeServer();

		System.out.println("Server shutting down");

		log.close();
	}
}
