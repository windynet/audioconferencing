package se.ltu.M7017E.lab2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import lombok.Getter;
import se.ltu.M7017E.lab2.common.messages.AnswerCall;
import se.ltu.M7017E.lab2.common.messages.Hello;
import se.ltu.M7017E.lab2.common.messages.Left;

/**
 * Manage the control channel
 */
public class ControlChannel implements Runnable {
	@Getter
	private App app;
	@Getter
	private BufferedReader in;
	private PrintStream out;
	private boolean quit = false; // set to true to exit thread
	private int index = 0;
	public ArrayList<String> msgList = new ArrayList<String>();

	public ControlChannel(App app) {
		System.out.println("Creating control channel");
		this.app = app;

		try {
			Socket socket = new Socket(InetAddress.getByName("localhost"), 4000);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
			// socket.setSoTimeout(2000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// int static index = 0;
		String message = null;
		while (!quit) {
			try {
				message = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (message == null) {
				// usually means that the client disconnected improperly
				quit = true;
			} else {
				// do something with the message
				System.out.println("Got raw msg: " + message);
				caseMessage(message);
			}
		}
	}

	public void caseMessage(String message) {
		if (message.startsWith("JOINED")) {
			// someone joins a room
			app.msg(Hello.fromString(message));
		} else if (message.startsWith("LEFT")) {
			// someone left a room
			app.msg(Left.fromString(message));
		} else if (message.startsWith("ROOMS_START")) {
			app.setServerIsWriting(true);
			msgList.clear();
		} else if (message.startsWith("AUDIENCE")) {
			msgList.add(index, message);
			index++;
		} else if (message.startsWith("ROOMS_STOP")) {
			index = 0;
			app.setServerIsWriting(false);
		} else if (message.startsWith("CALL")) {
			System.out.println("allo");
			app.getGui().acceptACall(message, this.app);
		} else if (message.startsWith("ANSWERCALL")) {
			AnswerCall answer = AnswerCall.fromString(message);
			if (answer.getAnswer().equals("yes")) {
				app.getGui().showMessage(
						answer.getReceiver() + " accepted the call");
				app.call("test", Integer.parseInt(answer.getPortReceiver()),
						answer.getIpReceiver());

			}
			if (answer.getAnswer().equals("no")) {
				app.getGui().showMessage(
						answer.getReceiver() + " declined the call");
			}

		} else if (message.startsWith("ERROR")) {
			app.getGui().showMessage(message.substring(6, message.length()));
		}
		;
	}

	/**
	 * Send the message.
	 * 
	 * @param message
	 *            without any formatting (no '\n' at the end for example)
	 */
	public void send(String message) {
		out.println(message);
	}

	// public void updateMsgFromServer(ArrayList<String> CloneArray) {
	// app.msgFromserver = new ArrayList<String>();
	//
	// for (int i = 0; i < CloneArray.size(); i++) {
	// app.msgFromserver.add(i, CloneArray.get(i));
	// }
	// }

}
