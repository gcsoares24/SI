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
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class mySaudeServer{

	private int port;
    public ObjectOutputStream objOut;
    public ObjectInputStream objIn;
	
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
	            receiveFiles(inStream, "../pdfs/");

	            // --- cleanup ---
	            inStream.close();
	            outStream.close();
	            socket.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    private void receiveFiles(ObjectInputStream objIn, String destFolder) {
	    	try {
	            // Number of files to receive
	            int numFiles = objIn.readInt();
	            System.out.println("Receiving " + numFiles + " file(s).");

	            // Append "sim" to destination folder
	            destFolder += "sim";

	            // Check if the folder exists
	            File folder = new File(destFolder);
	            if (!folder.exists()) {
	                if (folder.mkdirs()) {
	                    System.out.println("Destination folder created: " + destFolder);
	                } else {
	                    System.out.println("Failed to create destination folder.");
	                    return;
	                }
	            } else if (!folder.isDirectory()) {
	                System.out.println("Path exists but is not a directory.");
	                return;
	            }
	            

	            // Receive each file
	            for (int i = 0; i < numFiles; i++) {
	                // Receive filename
	                String fileName = (String) objIn.readObject();
	                // Receive file content as byte[]
	                byte[] fileBytes = (byte[]) objIn.readObject();

	                String destPath = destFolder + "/" + fileName;

	                File file = new File(destPath);
		            if (file.exists()) {
	                    System.out.println("File already exists at: " + destPath);
	                    continue;
		            }
	                // Save file to disk
	                try (FileOutputStream fos = new FileOutputStream(destPath)) {
	                    fos.write(fileBytes);
	                }

	                System.out.println("File received (" + fileBytes.length + " bytes) at " + destPath);
	            }

	        } catch (EOFException e) {
	            System.err.println("The client tried to send files but none of them existed.");
	        } catch (IOException | ClassNotFoundException e) {
	            System.err.println("Error receiving files: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	}
}
