package socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Provider {
	ServerSocket providerSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;

	Provider() {
	}

	void run() {
		try {
			// 1. creating a server socket
			this.providerSocket = new ServerSocket(2004, 10);
			// 2. Wait for connection
			System.out.println("Waiting for connection");
			this.connection = this.providerSocket.accept();
			System.out.println("Connection received from " + this.connection.getInetAddress().getHostName());
			// 3. get Input and Output streams
			this.out = new ObjectOutputStream(this.connection.getOutputStream());
			this.out.flush();
			this.in = new ObjectInputStream(this.connection.getInputStream());
			this.sendMessage("Connection successful");
			// 4. The two parts communicate via the input and output streams
			do {
				try {
					this.message = (String) this.in.readObject();
					System.out.println("client>" + this.message);
					if (this.message.equals("bye")) {
						this.sendMessage("bye");
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} while (!this.message.equals("bye"));
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				this.in.close();
				this.out.close();
				this.providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		try {
			this.out.writeObject(msg);
			this.out.flush();
			System.out.println("server>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Provider server = new Provider();
		while (true) {
			server.run();
		}
	}
}
