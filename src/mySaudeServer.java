/***************************************************************************
*   Seguranca Informatica
*	Projeto 1:
*		- Guilherme Soares
*		- Vitória Correia
*		- Duarte Soares
*
*	mySaudeServer.java
*
***************************************************************************/

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class mySaudeServer{

	public static void main(String[] args) throws IOException {
		System.out.println("servidor: main");
		mySaudeServer server = new mySaudeServer();
		server.startServer();
	}

	public void startServer () throws IOException{
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(23456);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
				sSoc.close();
		        e.printStackTrace();
		    }
		    
		}
	}


	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

	    private Socket socket = null;

	    ServerThread(Socket inSoc) {
	        socket = inSoc;
	        System.out.println("thread do server para cada cliente");
	    }

	    public void run() {
	        try {
	            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
	            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

	            String user = (String) inStream.readObject();
	            String passwd = (String) inStream.readObject();

	            System.out.println("User: " + user + ", Pass: " + passwd);

	            boolean loginOk = user.equals("guigui") && passwd.equals("eueu");
	            outStream.writeObject(loginOk);
	            outStream.flush();

	            if (loginOk) {
	                // --- PDF transfer ---
	                DataInputStream dataIn = new DataInputStream(socket.getInputStream());

	                long fileSize = dataIn.readLong();
	                byte[] buffer = new byte[8192];
	                int read;
	                long remaining = fileSize;

	                java.io.FileOutputStream fos = new java.io.FileOutputStream("../pdfs/received.pdf");

	                while (remaining > 0 &&
	                       (read = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
	                    fos.write(buffer, 0, read);
	                    remaining -= read;
	                }

	                fos.close();
	                System.out.println("PDF received (" + fileSize + " bytes)");
	            }

	            // --- cleanup ---
	            inStream.close();
	            outStream.close();
	            socket.close();

	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	    }
	}
}