package socket;

/**
 * Data de Criação: 06/03/2014
 *
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Requester {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;

	Requester() {
	}

	void run() {
		try {
			// 1. creating a socket to connect to the server
			this.requestSocket = new Socket("localhost", 2004);
			System.out.println("Connected to localhost in port 2004");
			// 2. get Input and Output streams
			this.out = new ObjectOutputStream(this.requestSocket.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(this.requestSocket.getInputStream());
			// 3: Communicating with the server
			do {
				try {
					this.message = (String) this.in.readObject();
					System.out.println("server>" + this.message);
					this.sendMessage("Hi my server");
					this.message = "bye";
					this.sendMessage(this.message);
				} catch (ClassNotFoundException classNot) {
					System.err.println("data received in unknown format");
				}
			} while (!this.message.equals("bye"));
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				this.in.close();
				this.out.close();
				this.requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		try {
			this.out.writeObject(msg);
			this.out.flush();
			System.out.println("client>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Requester client = new Requester();
		client.run();
	}
}