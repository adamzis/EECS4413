package a;

import java.io.File;
import java.io.FileNotFoundException;
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

	public TCPServer(int port, PrintStream log, Set<InetAddress> firewall) {

		ServerSocket server = null;
		this.log = log;
		this.firewall = firewall;
		// File running = new File(System.getProperty("user.home"),
		// "/Documents/4413/running.txt");

		server = createServer(port, server);

		InetAddress localHost = InetAddress.getLoopbackAddress();
		String serverIpPort = ipPortToString(server.getInetAddress(), port);

		this.insertLogEntry("Server Start", serverIpPort);

		this.punch(localHost);

		while (!server.isClosed()) {
			Socket client = createClient(server);

			// Testing
			// System.out.println(client.getInetAddress());
			// System.out.println(allowedIp(client));

			if (allowedIp(client)) {
				Worker worker = new Worker(this, client);

				Thread workerThread = new Thread(worker);
				workerThread.start();
			} else {
				firewallVio(client);
			}
		}

		closeServer(server);
	}

	private void firewallVio(Socket client) {
		this.insertLogEntry("Firewall Violation", client.getInetAddress().toString());
		try {
			client.close();
		} catch (IOException e) {
			this.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
		}
	}

	private void closeServer(ServerSocket server) {
		try {
			server.close();
			this.insertLogEntry("Server Shutdown", null);
		} catch (IOException e) {
			this.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println("Error " + e.getMessage());
			System.exit(1);
		}
	}

	private Socket createClient(ServerSocket server) {
		Socket client = null;

		try {
			client = server.accept();
		} catch (IOException e) {
			this.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
		}

		return client;
	}

	private ServerSocket createServer(int port, ServerSocket server) {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			this.insertLogEntry(e.getMessage(), e.getStackTrace().toString());
			System.out.println("Error " + e.getMessage());
			System.exit(1);
		}

		return server;
	}

	/**
	 * Inserts an entry into the log file with the server's date and time.
	 * 
	 * @param entry
	 *            main event that occurred (Server start, client connection, etc).
	 * @param subEntry
	 *            additional information such as the client's IP address and port.
	 */
	public void insertLogEntry(String entry, String subEntry) {
		log.println(entry + " " + "(" + subEntry + ")" + " - " + getTime());
	}

	/**
	 * Adds an IP Address to the fire wall for authorization.
	 * 
	 * @param inetAddress
	 *            an IP address that should be allowed to connect.
	 */
	public void punch(InetAddress inetAddress) {
		firewall.add(inetAddress);
	}

	/**
	 * Removes a previously authorized IP address from the fire wall.
	 * 
	 * @param inetAddress
	 *            an IP address that should no longer be allowed to connect.
	 */
	public void plug(InetAddress inetAddress) {
		firewall.remove(inetAddress);
	}

	/**
	 * Checks if the client's IP is on the fire wall.
	 * 
	 * @param client
	 *            socket object of the connected client
	 * @return true if the client's IP is in the fire wall, false if it is not.
	 */
	private boolean allowedIp(Socket client) {
		InetAddress clientIp = client.getInetAddress();
		boolean allowedIp = firewall.contains(clientIp);

		if (allowedIp) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Formats an IP and port into a string that will be used as a subentry in the
	 * insertLogEntry() method.
	 * 
	 * @param ip
	 *            an InetAddress object.
	 * @param port
	 *            a port number associated with the connection.
	 * @return
	 */
	private String ipPortToString(InetAddress ip, int port) {
		String ipPort = ip.toString() + ":" + Integer.toString(port);

		return ipPort;
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

	public static void main(String[] args) throws FileNotFoundException {

		int listenPort = 10560;

		String userHome = System.getProperty("user.home");
		File logFile = new File(userHome, "/Documents/4413/log.txt");

		Set<InetAddress> firewall = new HashSet<>();

		PrintStream log = new PrintStream(new FileOutputStream(logFile, false));
		log.println();

		System.out.println("Starting Server, connection port is " + listenPort);
		TCPServer theServer = new TCPServer(listenPort, log, firewall);
		System.out.println("Server shutting down");

		log.close();
	}
}
