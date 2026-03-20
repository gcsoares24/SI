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

	private int port;
	
	public static void main(String[] args) throws IOException {
		System.out.println("servidor: main");
		mySaudeServer server = new mySaudeServer();
		 if (args.length < 1) {
		        System.out.println("Usage: java mySaudeServe <port>");
		        return;
		    }

		    try {
		        int port = Integer.parseInt(args[0]);

		        if (port < 1 || port > 65535) {
		            System.out.println("Invalid port! Must be 1-65535.");
		            return;
		        }

		        server.port = port;

		    } catch (NumberFormatException e) {
		        System.out.println("Port must be a number!");
		        return;
		    }

		    server.startServer();
	}

	public void startServer () throws IOException{
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(port);
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
	            
	            String option = inStream.readUTF();
	            
	            switch (option) {

	            case "-e": // cliente envia ficheiros
	                receiveFiles(socket, "../pdfs/");
	                break;

	            case "-r": // cliente quer receber ficheiros
	                sendFiles(socket);
	                break;

	            default:
	                System.out.println("Unknown operation: " + option);
	        }
	            
	            // --- LOGIN ---
//	            String user = (String) inStream.readObject();
//	            String passwd = (String) inStream.readObject();
//
//	            System.out.println("User: " + user + ", Pass: " + passwd);
//
//	            boolean loginOk = user.equals("guigui") && passwd.equals("eueu");
//	            outStream.writeObject(loginOk);
//	            outStream.flush();
//
//	            if (loginOk) {
//	                // Call a method to receive a file
//	                receiveFile(socket, "../pdfs/received.pdf");
//	            } else {
//	                System.out.println("Login falhou para o usuário: " + user);
//	            }
	            

	            // --- cleanup ---
	            inStream.close();
	            outStream.close();
	            socket.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private void receiveFiles(Socket socket, String destFolder) {
	        try {
	            DataInputStream dataIn = new DataInputStream(socket.getInputStream());

	            // Number of files to receive
	            int numFiles = dataIn.readInt();
	            System.out.println("Receiving " + numFiles + " file(s).");

	            for (int i = 0; i < numFiles; i++) {
	                // Read file name and size from the client
	                String fileName = dataIn.readUTF();
	                long fileSize = dataIn.readLong();
	                byte[] buffer = new byte[8192];
	                int read;
	                long remaining = fileSize;

	                // Destination path as string
	                String destPath = destFolder + "/" + fileName;

	                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destPath)) {
	                    while (remaining > 0 &&
	                           (read = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
	                        fos.write(buffer, 0, read);
	                        remaining -= read;
	                    }
	                }

	                System.out.println("File received (" + fileSize + " bytes) at " + destPath);
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}