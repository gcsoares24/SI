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
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class mySaudeServer{
	
	private static final String NO_DIRECTORY = "NO_DIRECTORY";
	private static final String OK = "OK";
	private static final String FILE_NOT_FOUND_FLAG = "__FILE_NOT_FOUND__";
	private static final String SERVER_FILE_EXISTS = "SERVER_FILE_EXISTS";
	private static final String OK_TO_SEND = "OK_TO_SEND";
	private static final String CLIENT_FILE_EXISTS = "CLIENT_FILE_EXISTS";
	private static final String FILE_INFO = "FILE_INFO";
	
	private static final String PATH_KEYSTORE = "../keystore/";
	
	
	private int port;
    public ObjectOutputStream objOut;
    public ObjectInputStream objIn;

	// MAC Logic Variables
	public static String macPassword; 
	private static final String USERS_FILE = "../servidor/users.txt";
	private static final String MAC_FILE = "../servidor/mySaude.mac";

	// 1B. Calcula o MAC do ficheiro atual usando a password inserida
	public static String calcularMac(String password) throws Exception {
	    File f = new File(USERS_FILE);
	    if (!f.exists()) return ""; 

	    byte[] fileBytes = Files.readAllBytes(f.toPath());
	    byte[] passBytes = password.getBytes();
	    
	    SecretKeySpec key = new SecretKeySpec(passBytes, "HmacSHA256");
	    Mac mac = Mac.getInstance("HmacSHA256");
	    mac.init(key);
	    
	    byte[] macBytes = mac.doFinal(fileBytes);
	    return Base64.getEncoder().encodeToString(macBytes);
	}

	// 2. Verifica se o MAC guardado bate certo com o ficheiro atual
	public static boolean verificarMac(String password) {
	    try {
	        File macFile = new File(MAC_FILE);
	        File usersFile = new File(USERS_FILE);
	        
	        if (!usersFile.exists() && !macFile.exists()) return true; 
	        if (usersFile.exists() && !macFile.exists()) return false; 
	        
	        String macGuardado = new String(Files.readAllBytes(macFile.toPath())).trim();
	        String macCalculado = calcularMac(password);
	        
	        return macGuardado.equals(macCalculado);
	    } catch (Exception e) {
	        return false;
	    }
	}

	private static String getBasePath() {
	    return System.getProperty("user.home")
	            + File.separator + "mySaude";	            
	}
	public static void main(String[] args) throws IOException {
		System.out.println("server: main");
		mySaudeServer server = new mySaudeServer();
		
		if (args.length < 1) {
			System.out.println("Usage: java mySaudeServer <port>");
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

		// 1. Pedir a password do MAC usando o método seguro
		macPassword = readPassword("server> Enter server MAC password to start: ");

		if (new File(USERS_FILE).exists()) {
			// ATUALIZADO AQUI para verificarMac
			if (!verificarMac(macPassword)) {
				System.err.println("FATAL ERROR: Invalid MAC. The password file has been tampered with!");
				System.exit(-1); 
			}
			System.out.println("server> File integrity validated successfully.");
		}

		// 2. Pedir a password da Keystore usando o método seguro
		String keystorePath = getBasePath() + File.separator + "keystore" + File.separator + "keystore.server";

	    File trustStoreFile = new File(keystorePath);

	    if (!trustStoreFile.exists()) {
	        throw new IllegalArgumentException(
	            "ERRO TLS: servers keystore was not found at: " + keystorePath +
	            "\nPlace keystore.server in there and try again."
	        );
	    }
		
		String keyStorePassword = readPassword("server> Enter server keystore password: ");

	    System.setProperty("javax.net.ssl.keystore", trustStoreFile.getAbsolutePath());
		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
		System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		
		server.startServer();
	}

	/**
	 * Método auxiliar para ler passwords de forma segura (sem eco no terminal)
	 * Mantém compatibilidade com IDEs e terminais (WSL/Linux)
	 */
	private static String readPassword(String prompt) {
		// Tenta usar a consola do sistema para esconder os carateres
		if (System.console() != null) {
			char[] passwordChars = System.console().readPassword(prompt);
			return new String(passwordChars);
		}

		// Fallback para quando a consola não está disponível (ex: Eclipse, IntelliJ)
		System.out.print(prompt);
		try {
			Scanner sc = new Scanner(System.in);
			if (sc.hasNextLine()) {
				return sc.nextLine();
			}
			return "";
		} catch (Exception e) {
			throw new RuntimeException("FATAL ERROR: Could not read password from input.", e);
		}
	}

	public void startServer () throws IOException{
		SSLServerSocket sSoc = null;

		try {
		    SSLServerSocketFactory ssf =
		        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

		    sSoc = (SSLServerSocket) ssf.createServerSocket(port);
		    
		    System.out.println("servidor> TLS is active.Listening at the port: " + port + "...");
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
	        System.out.println("serverThreat for each client...");
	    }

	    public void run() {
	        try {
	            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
	            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
	           	            

	            String option = (String) inStream.readObject();
	            
	            switch (option) {

		            case "-e":
		            	break;
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
		            case "GET_CERT":
		            	sendCert(inStream, outStream);
		            	break;
		            default:
		                System.out.println("Unknown operation: " + option);
	            }
	            
	            inStream.close();
	            outStream.close();
	            socket.close();

	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
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
	    private void sendCert(ObjectInputStream objIn, ObjectOutputStream objOut) throws KeyStoreException, CertificateEncodingException, IOException {
	    	try {
	    		String username = objIn.readUTF();
	    		

			    KeyStore ks = KeyStore.getInstance("PKCS12");
			    File ksFile = new File(PATH_KEYSTORE + "keystore.users");
			    
	    		if(!ksFile.exists()) {
	    			objOut.writeUTF("NOT_FOUND");
	    			objOut.flush();
	    			return;
	    		}else{
			        try (FileInputStream ksfis = new FileInputStream(ksFile)) {
					    
					    char[] ksPassword = "password".toCharArray(); // Define uma pass para a KS
			            ks.load(ksfis, ksPassword);
					    
			        } catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CertificateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		Certificate cert = ks.getCertificate(username);
	    		if(cert == null) {
	    			objOut.writeUTF(FILE_NOT_FOUND_FLAG);
	    			objOut.flush();
	    	        return;
	    		}
	    		byte[] certBytes = cert.getEncoded();
	    		
	    		// enviar OK
	    		objOut.writeUTF(OK);
	    		objOut.flush();

	    		//resposta ao OK
	    		String response = objIn.readUTF();
	    		if(response == CLIENT_FILE_EXISTS) {
	    			return;
	    		}

			    
	    		// enivar tamanho
			    objOut.writeLong(certBytes.length);
	    		objOut.flush();
	    		
	    		

	    		// enviar os bytes do certificado
			    objOut.write(certBytes);
			    objOut.flush();
	    		
	    		
	    		System.out.println("CERT OF THE USER" + username);
                System.out.println(cert);
               
                
                
	    	} catch (IOException e) {
	            System.err.println("ERROR:While trying to Sending the files: " + e.getMessage());
	            e.printStackTrace();
	        } catch (KeyStoreException e) {
	        	objOut.writeUTF(FILE_NOT_FOUND_FLAG);
	        	objOut.flush();
	            System.err.println("ERRO: The cert does NOT exist!");
	        	
	        }
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
	                    System.out.println("ERROR: The file already exists in the client side: " + fileName);
	                    continue;
	                }

	                if (!clientResponse.equals(OK_TO_SEND)) {
	                    System.out.println("Invalid response from the client for the file: " + fileName);
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

	                System.out.println("File send with sucess: " + file.getAbsolutePath());
	            }

	        } catch (IOException | ClassNotFoundException e) {
	            System.err.println("ERROR: While trying to send the files: " + e.getMessage());
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
	                System.out.println("ERROR: The directory of the user '" + receiver + "' doesnt exist in the server.");

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
	                    System.out.println("ERROR: The file DOES NOT exist on the client side: " + originalPath);
	                    continue;
	                }
	                File file;
	                String destPath;
	                long fileSize = objIn.readLong();

	                String baseName = getBaseName(fileName);
	                destPath = destFolder + "/" + baseName;	
	                file = new File(destPath);
	                if (fileExists(destPath)) {
	                    System.out.println("ERROR:The file " + fileName + " or any of its variations, already exists on the server.");

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
	                	//meter isto aqui eu acho: previousWasSignature = false;
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
	                            throw new EOFException("ERROR: Non expected error from the server, while retrieving the file: " + fileName);
	                        }

	                        fos.write(buffer, 0, bytesRead);
	                        remaining -= bytesRead;
	                    }
	                }

	                System.out.println("File retrieved with success: " + destPath);
	            }

	        } catch (EOFException e) {
	            System.err.println("ERROR: Unexpected end of connection.");
	        } catch (IOException | ClassNotFoundException e) {
	            System.err.println("ERROR: Non expected error from the server, while retrieving the file: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	}
}