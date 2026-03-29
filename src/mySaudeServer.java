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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class mySaudeServer{
	
	private static final String NO_DIRECTORY = "NO_DIRECTORY";
	private static final String OK = "OK";
	private static final String FILE_NOT_FOUND_FLAG = "__FILE_NOT_FOUND__";
	private static final String SERVER_FILE_EXISTS = "SERVER_FILE_EXISTS";
	private static final String OK_TO_SEND = "OK_TO_SEND";
	private static final String CLIENT_FILE_EXISTS = "CLIENT_FILE_EXISTS";
	private static final String FILE_INFO = "FILE_INFO";
	
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
	           	            
	            
	            String option = (String) inStream.readObject();
	            
	            switch (option) {

		            case "-e":
		            case "-ce":
		            case "-ae":
		            case "-ace":
		                receiveFiles(inStream, outStream, "../servidor/");
		                break;
	
		            case "-r":
		            case "-rd":
		            case "-rv":
		            case "-rdv":
		                sendFiles(inStream, outStream, "../servidor/");
		                break;
	
		            default:
		                System.out.println("Unknown operation: " + option);
	            }
	            
	            inStream.close();
	            outStream.close();
	            socket.close();

	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	    }
	    private String getBaseName(String fileName) {
	        if (fileName.endsWith(".cifrado")) {
	            return fileName.replace(".cifrado", "");
	        }
	        if (fileName.endsWith(".assinado")) {
	            return fileName.replace(".assinado", "");
	        }
	        if (fileName.endsWith(".envelope")) {
	            return fileName.replace(".envelope", "");
	        }
	        return fileName;
	    }
	    
	    private void sendFiles(ObjectInputStream objIn, ObjectOutputStream objOut, String baseFolder) {
	        try {
	            int numFiles = objIn.readInt();
	            String username = objIn.readUTF();

	            File folder = new File(baseFolder + username);

	            if (!folder.exists() || !folder.isDirectory()) {
	                for (int i = 0; i < numFiles; i++) {
	                    objIn.readObject();
	                }

	                for (int i = 0; i < numFiles; i++) {
	                    objOut.writeObject(NO_DIRECTORY);
	                    objOut.flush();
	                }
	                return;
	            }

	            for (int i = 0; i < numFiles; i++) {
	                String fileName = (String) objIn.readObject();
	                File file = new File(folder, fileName);

	                if (!file.exists() || !file.isFile()) {
	                    objOut.writeObject(FILE_NOT_FOUND_FLAG);
	                    objOut.flush();
	                    continue;
	                }

	                objOut.writeObject(FILE_INFO);
	                objOut.flush();

	                objOut.writeLong(file.length());
	                objOut.flush();

	                String clientResponse = (String) objIn.readObject();

	                if (clientResponse.equals(CLIENT_FILE_EXISTS)) {
	                    System.out.println("ERRO: ficheiro já existe do lado do cliente: " + fileName);
	                    continue;
	                }

	                if (!clientResponse.equals(OK_TO_SEND)) {
	                    System.out.println("Resposta inválida do cliente para o ficheiro: " + fileName);
	                    continue;
	                }

	                try (FileInputStream fis = new FileInputStream(file)) {
	                    byte[] buffer = new byte[8192];
	                    int bytesRead;

	                    while ((bytesRead = fis.read(buffer)) != -1) {
	                        objOut.write(buffer, 0, bytesRead);
	                    }
	                    objOut.flush();
	                }

	                System.out.println("Ficheiro enviado com sucesso: " + file.getAbsolutePath());
	            }

	        } catch (IOException | ClassNotFoundException e) {
	            System.err.println("ERRO: A enviar ficheiros: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	    
	    private boolean fileExists(String filePath) {
	        String[] types = {".cifrado", ".assinado", ".envelope"};

	        // check original file
	        File file = new File(filePath);
	        if (file.exists()) {
	            return true;
	        }

	        // check variations
	        for (String type : types) {
	            File variant = new File(filePath + type);
	            if (variant.exists()) {
	                return true;
	            }
	        }

	        return false;
	    }
	    
	    private void receiveFiles(ObjectInputStream objIn, ObjectOutputStream objOut, String destFolder) {
	        try {
	            boolean previousWasSignature = false;
	        	
	            int numFiles = objIn.readInt();
	            System.out.println("Receiving " + numFiles + " file(s).");

	            String receiver = objIn.readUTF();
	            destFolder += receiver;

	            File folder = new File(destFolder);

	           
	            if (!folder.exists() || !folder.isDirectory()) {
	                System.out.println("ERRO: diretoria do utilizador '" + receiver + "' não existe no servidor.");

	                objOut.writeObject(NO_DIRECTORY);
	                objOut.flush();
	                return;
	            }

	            
	            objOut.writeObject(OK);
	            objOut.flush();
	            
	            for (int i = 0; i < numFiles; i++) {

	                String fileName = (String) objIn.readObject();

	                
	                if (fileName.equals(FILE_NOT_FOUND_FLAG)) {
	                    String originalPath = (String) objIn.readObject();
	                    System.out.println("ERRO: ficheiro não existe do lado do cliente: " + originalPath);
	                    continue;
	                }
	                File file;
	                String destPath;
	                long fileSize = objIn.readLong();

	                String baseName = getBaseName(fileName);
	                destPath = destFolder + "/" + baseName;	
	                file = new File(destPath);
	                if (fileExists(destPath)) {
	                    System.out.println("ERRO: O ficheiro " + fileName + " ou alguma das suas variacoes, já existe no servidor");

	                    objOut.writeObject(SERVER_FILE_EXISTS);
	                    objOut.flush();
	                    continue;
	                }
	                destPath = destFolder + "/" + fileName;
	                
	                if(previousWasSignature) {
	                	if(fileName.contains(".cifrado")) {
	                		System.out.println(destPath);
	                		destPath = destPath.replace(".cifrado", ".envelope");
	                		System.out.println(destPath);
	                	}else if(!fileName.contains(".chave")){
		                	destPath += ".assinado";
		                	previousWasSignature = false;
	                	}
	                }
	                
	                if(fileName.contains(".assinatura")) {
	                	previousWasSignature = true;
	                }
	                
	                
	                file = new File(destPath);

	               
	                objOut.writeObject(OK_TO_SEND);
	                objOut.flush();

	             
	                try (FileOutputStream fos = new FileOutputStream(file)) {
	                    byte[] buffer = new byte[8192];
	                    long remaining = fileSize;

	                    while (remaining > 0) {
	                        int bytesRead = objIn.read(buffer, 0, (int)Math.min(buffer.length, remaining));

	                        if (bytesRead == -1) {
	                            throw new EOFException("ERRO: Fim inesperado ao receber ficheiro " + fileName);
	                        }

	                        fos.write(buffer, 0, bytesRead);
	                        remaining -= bytesRead;
	                    }
	                }

	                System.out.println("Ficheiro recebido com sucesso: " + destPath);
	            }

	        } catch (EOFException e) {
	            System.err.println("ERRO: Fim inesperado da comunicação.");
	        } catch (IOException | ClassNotFoundException e) {
	            System.err.println("ERRO: A receber ficheiros: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	}
}
