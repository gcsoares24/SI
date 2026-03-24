/***************************************************************************
*   Seguranca Informatica
*	Projeto 1:
*		- Guilherme Soares
*		- Vitória Correia
*		- Duarte Soares
*
*	mySaude.java
*
***************************************************************************/

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;


public class mySaude {
	
	private static final String NO_DIRECTORY = "NO_DIRECTORY";
	private static final String OK = "OK";
	private static final String FILE_NOT_FOUND_FLAG = "__FILE_NOT_FOUND__";
	private static final String SERVER_FILE_EXISTS = "SERVER_FILE_EXISTS";
	private static final String OK_TO_SEND = "OK_TO_SEND";
	private static final String CLIENT_FILE_EXISTS = "CLIENT_FILE_EXISTS";
	private static final String FILE_INFO = "FILE_INFO";
	
	private static final Set<String> OPTIONS = Set.of(
		    "-e", "-r", "-c", "-d",
		    "-ce", "-rd",
		    "-a", "-v",
		    "-ae", "-rv",
		    "-ace", "-rdv"
		);
	
	private static final Set<String> SERVER_OPTIONS = Set.of(
		    "-e", "-r",
		    "-ce", "-rd",
		    "-ae", "-rv",
		    "-ace", "-rdv"
		);

	private static String isOption(String currentFlag, String fallback) {
	    if (OPTIONS.contains(currentFlag)) {
	        return currentFlag;
	    }
	    return fallback;
	}
	public Socket sock;
    public ObjectOutputStream objOut;
    public ObjectInputStream objIn;
    
    public String username;
    public String password;
    public String receiver;
	
	
	public static mySaude client = new mySaude();
		
	public static void main(String[] args) throws IllegalArgumentException{

		try {
			System.out.println("cliente> A iniciar...");
			Map<String, String> flags = argsMapping(args);
	        
	        System.out.println("flags: "+ flags);
	        
	        
	        String option = inicialize(flags);
	        
            if(option == null) {
                throw new IllegalArgumentException("There is no option");
            }
            
            
            //sends only server type of operation
            if (SERVER_OPTIONS.contains(option)) {
            	if (client.objOut == null) {
                    throw new IllegalArgumentException("A opção " + option + " requer ligação ao servidor (-s).");
                }
            	
            	client.objOut.writeObject(option);
                client.objOut.flush();
            }
            
            switchCase(option, flags.get(option));
	
            //
            // ELIMINAR
            //
            // ELIMINAR
            // ELIMINAR
            //
            // ELIMINAR
            // ELIMINAR
            //
            // ELIMINAR
	        System.out.println(flags);
	 
	    }catch(Exception e){
	        	System.out.println("\n\nIt seems like your option has an error:\n\n" + e.getMessage());
	        	return;
	     } finally {
	         client.closeClientResources();
	     }

    }
    
    private static void switchCase(String option, String value) throws EOFException {
    	switch (option) {
	
	        // 2A. Transferência de Ficheiros
	        case "-e":
	            System.out.println("-e: Envia ficheiros para o servidor.");
	            if(client.receiver == null) {
	            	throw new EOFException("There is no -t (receiver).");
	            }
	            client.sendFiles(value, client.receiver);
	            break;
	        case "-r":
	            System.out.println("-r: Recebe ficheiros do servidor.");
	            client.receiveFiles(value);
	            break;
	
	        // 2B. Criptografia
	        case "-c":
	            System.out.println("-c: Cifra ficheiros localmente (AES + RSA).");
				client.encryptFiles(value, client.receiver);
	            break;
	        case "-d":
	            System.out.println("-d: Decifra ficheiros usando a chave local.");
				client.decryptFiles(value);
	            break;
	
	        // 2C. Assinatura Digital
	        case "-a":
	            System.out.println("-a: Assina ficheiros localmente.");
	            break;
	        case "-v":
	            System.out.println("-v: Valida a assinatura de ficheiros.");
	            break;
	
	        // 2D. Operações Combinadas
	        case "-ce":
	            System.out.println("-ce: Cifra e envia ficheiros para o servidor.");
	            break;
	        case "-rd":
	            System.out.println("-rd: Recebe e decifra ficheiros do servidor.");
	            break;
	        case "-ae":
	            System.out.println("-ae: Assina e envia ficheiros.");
	            break;
	        case "-rv":
	            System.out.println("-rv: Recebe ficheiros e valida assinatura.");
	            break;
	        case "-ace":
	            System.out.println("-ace: Assina, cifra e envia ficheiros (Envelope Seguro).");
	            break;
	        case "-rdv":
	            System.out.println("-rdv: Recebe, decifra e valida assinatura de ficheiros.");
	            break;
	
			default:
	            System.out.println("Unknown flag: " + option);
    	}
	}
    
    private static Map<String, String> argsMapping(String[] args) throws IllegalArgumentException {
	    Map<String, String> flags = new LinkedHashMap<>();
	    String currentFlag = "";
	    String optionKey = "";

	    for (int i = 0; i < args.length; i++) {
	        String arg = args[i];

	        if (arg.startsWith("-")) {
	            currentFlag = arg;
	            
	            if(i + 1 >= args.length) {
	                throw new IllegalArgumentException("There is an empty flag: " + arg);
	            }
	            
	            if (args[i + 1].startsWith("-")) {
	                throw new IllegalArgumentException("Can't put 2 flags in a row: " + currentFlag + " and " + args[i + 1]);
	            }

	            flags.put(currentFlag, args[i + 1]);

	            optionKey = isOption(currentFlag, optionKey);

	            i++; // skip value
	        } else {
	            flags.put(currentFlag, flags.get(currentFlag) + ";" + arg);
	        }
	    }

	    return flags;
	}
	
	public static String inicialize(Map<String, String> flags) throws ConnectException {
		String option = null;
        for (String key : flags.keySet()) {
        	switch (key) {
	            // 1. Flags de Conexão e Identificação
	            case "-s": client.startClient(flags.get("-s").split(":"));
	                break;
	            case "-u":
	                System.out.println("-u: Identifica o utilizador que executa o comando.");
	                client.username = flags.get("-u");
	                break;
	            case "-p":
	                System.out.println("-p: Password para aceder à keystore local do utilizador.");
	                client.password = flags.get("-p");
	                break;
	            case "-t":
	                System.out.println("-t: Define o destinatário ou o autor da operação.");
	                client.receiver = flags.get("-t");
	                break;
	            default:
	            	//if not any of the others its the option!
	            	if(option != null) {
	            		throw new IllegalArgumentException("There can only be one option!\nWhat was flagged:\n\t " + option + "\n\t " + key);
	            	}
	        		option = key;
            	
        	}
        }
		return option;
	}

	public void startClient(String[] address) throws ConnectException {
	    System.out.println("Servidor>-s: A definir o endereço IP e o porto do servidor.");

	    int port;
	    String ip;

	    try {
	        if (address.length != 2) {
	            throw new IllegalArgumentException("O endereço do servidor deve estar no formato IP:porto.");
	        }

	        ip = address[0];
	        port = Integer.parseInt(address[1]);

	        if (port < 1 || port > 65535) {
	            throw new IllegalArgumentException("Invalid port! Must be 1-65535.");
	        }

	    } catch (NumberFormatException e) {
	        throw new IllegalArgumentException("Port must be a number!");
	    }

	    try {
	        client.sock = new Socket(ip, port);
	        client.objOut = new ObjectOutputStream(client.sock.getOutputStream());
	        client.objIn = new ObjectInputStream(client.sock.getInputStream());

	        System.out.println("Connected to server at " + ip + ":" + port);

	    } catch (ConnectException e) {
	        throw new ConnectException("\tConnection refused. Make sure the mySaudeServer is running at the specified address and port.");
	    } catch (IOException e) {
	        throw new RuntimeException("Erro ao ligar ao servidor: " + e.getMessage(), e);
	    }
	}
	
	
	public void sendFiles(String filePaths, String receiver) {
	    if (client.sock == null || client.objOut == null || client.objIn == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        return;
	    }

	    String[] paths = filePaths.split(";");

	    try {
	        // fase 1: enviar metadados iniciais
	        client.objOut.writeInt(paths.length);
	        client.objOut.flush();

	        client.objOut.writeUTF(receiver);
	        client.objOut.flush();
	    	

	        // esperar resposta do servidor sobre a diretoria
	        String serverResponse = (String) client.objIn.readObject();

	        if (serverResponse.equals(NO_DIRECTORY)) {
	            System.out.println("Erro: diretoria do utilizador '" + receiver + "' não existe no servidor.");
	            return;
	        }

	        if (!serverResponse.equals(OK)) {
	            System.out.println("Erro: resposta inválida do servidor.");
	            return;
	        }

	        // enviar ficheiros
	        for (String path : paths) {
	        	File file = new File(path.trim());
	        	
	            if (!file.exists() || !file.isFile()) {
	                System.out.println("Erro: ficheiro não existe do lado do cliente: " + path.trim());

	                client.objOut.writeObject(FILE_NOT_FOUND_FLAG);
	                client.objOut.writeObject(path.trim());
	                client.objOut.flush();
	                continue;
	            }

	            // enviar metadados do ficheiro
	            client.objOut.writeObject(file.getName());
	            client.objOut.writeLong(file.length());
	            client.objOut.flush();

	            // esperar confirmação do servidor
	            String fileResponse = (String) client.objIn.readObject();

	            if (fileResponse.equals(SERVER_FILE_EXISTS)) {
	                System.out.println("Erro: ficheiro já existe no servidor: " + file.getName());
	                continue;
	            }

	            if (!fileResponse.equals(OK_TO_SEND)) {
	                System.out.println("Erro: resposta inválida do servidor para o ficheiro " + file.getName());
	                continue;
	            }

	            // enviar conteúdo por blocos
	            try (FileInputStream fis = new FileInputStream(file)) {
	                byte[] buffer = new byte[8192];
	                int bytesRead;

	                while ((bytesRead = fis.read(buffer)) != -1) {
	                    client.objOut.write(buffer, 0, bytesRead);
	                }
	                client.objOut.flush();
	            }

	            System.out.println("Ficheiro enviado com sucesso: " + file.getName());
	        }

	    } catch (IOException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public void receiveFiles(String filePaths) {
	    if (client.sock == null || client.objOut == null || client.objIn == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        return;
	    }

	    String[] requestedFiles = filePaths.split(";");

	    try {
	        client.objOut.writeInt(requestedFiles.length);
	        client.objOut.flush();

	        client.objOut.writeUTF(client.username);
	        client.objOut.flush();

	        for (String requested : requestedFiles) {
	            String fileName = requested.trim();

	            // enviar pedido de UM ficheiro
	            client.objOut.writeObject(fileName);
	            client.objOut.flush();

	            // receber estado desse ficheiro
	            String status = (String) client.objIn.readObject();

	            if (status.equals(NO_DIRECTORY)) {
	                System.out.println("Erro: diretoria do utilizador '" + client.username + "' não existe no servidor.");
	                return;
	            }

	            if (status.equals(FILE_NOT_FOUND_FLAG)) {
	                System.out.println("Erro: ficheiro não existe no servidor: " + fileName);
	                continue;
	            }

	            if (!status.equals(FILE_INFO)) {
	                System.out.println("Erro: resposta inválida do servidor para o ficheiro " + fileName);
	                continue;
	            }

	            long fileSize = client.objIn.readLong();

	            File outFile = new File("../eu/" + fileName);
	            File parentDir = outFile.getParentFile();

	            if (!parentDir.exists()) {
	                parentDir.mkdirs();
	            }

	            if (outFile.exists()) {
	                System.out.println("Erro: ficheiro já existe do lado do cliente: " + fileName);
	                client.objOut.writeObject(CLIENT_FILE_EXISTS);
	                client.objOut.flush();
	                continue;
	            }

	            client.objOut.writeObject(OK_TO_SEND);
	            client.objOut.flush();

	            try (FileOutputStream fos = new FileOutputStream(outFile)) {
	                byte[] buffer = new byte[8192];
	                long remaining = fileSize;

	                while (remaining > 0) {
	                    int bytesRead = client.objIn.read(buffer, 0, (int)Math.min(buffer.length, remaining));

	                    if (bytesRead == -1) {
	                        throw new EOFException("Fim inesperado ao receber ficheiro " + fileName);
	                    }

	                    fos.write(buffer, 0, bytesRead);
	                    remaining -= bytesRead;
	                }
	            }

	            System.out.println("Ficheiro recebido com sucesso: " + fileName);
	        }

	    } catch (IOException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	public void encryptFiles(String filePaths, String targetUser) {
	String[] paths = filePaths.split(";");
	try {
		KeyStore ks = KeyStore.getInstance("JKS");
		try (FileInputStream fis = new FileInputStream("keystore/keystore." + this.username)) {
			ks.load(fis, this.password.toCharArray());
		}

		Certificate cert = ks.getCertificate(targetUser);
		if (cert == null) {
			System.err.println("Error: Certificate for " + targetUser + " not found.");
			return;
		}
		PublicKey publicKey = cert.getPublicKey();

		for (String path : paths) {
			File inputFile = new File(path.trim());
			if (!inputFile.exists()) {
				System.err.println("ERRO: O ficheiro '" + path.trim() + "' não foi encontrado!");
				System.err.println("-> O Java está a procurar exatamente neste caminho: " + inputFile.getAbsolutePath());
				continue;
			}

			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			SecretKey aesKey = keyGen.generateKey();

			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
			
			try (FileInputStream fis = new FileInputStream(inputFile);
				 FileOutputStream fos = new FileOutputStream(path + ".encrypted")) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = fis.read(buffer)) > 0) {
					fos.write(aesCipher.update(buffer, 0, read));
				}
				fos.write(aesCipher.doFinal());
			}

			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = rsaCipher.wrap(aesKey);

			try (FileOutputStream fos = new FileOutputStream(path + ".key." + targetUser)) {
				fos.write(wrappedKey);
			}
			System.out.println("File encrypted: " + path + ".encrypted");
		}
	} catch (Exception e) {
		System.err.println("Encryption error: " + e.getMessage());
	}
	}

	public void decryptFiles(String filePaths) {
	String[] paths = filePaths.split(";");
	try {
		KeyStore ks = KeyStore.getInstance("JKS");
		try (FileInputStream fis = new FileInputStream("keystore/keystore." + this.username)) {
			ks.load(fis, this.password.toCharArray());
		}

		PrivateKey privateKey = (PrivateKey) ks.getKey(this.username, this.password.toCharArray());

		for (String path : paths) {
			String baseName = path.replace(".encrypted", "");
			File keyFile = new File(baseName + ".key." + this.username);
			
			if (!keyFile.exists()) {
				System.err.println("Error: Key file not found for " + path);
				continue;
			}

			byte[] wrappedKey = new byte[(int) keyFile.length()];
			try (FileInputStream fis = new FileInputStream(keyFile)) {
				fis.read(wrappedKey);
			}

			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.UNWRAP_MODE, privateKey);
			SecretKey aesKey = (SecretKey) rsaCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.DECRYPT_MODE, aesKey);

			try (FileInputStream fis = new FileInputStream(path);
				FileOutputStream fos = new FileOutputStream(baseName.replace(".pdf", "_decrypted.pdf"))){
				byte[] buffer = new byte[8192];
				int read;
				while ((read = fis.read(buffer)) > 0) {
					fos.write(aesCipher.update(buffer, 0, read));
				}
				fos.write(aesCipher.doFinal());
			}
			System.out.println("File decrypted successfully.");
		}
	} catch (Exception e) {
		System.err.println("Decryption error: " + e.getMessage());
	}

}
	
	private void closeClientResources() {
	    try {
	        if (objIn != null) {
	            objIn.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Erro ao fechar ObjectInputStream: " + e.getMessage());
	    }

	    try {
	        if (objOut != null) {
	            objOut.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Erro ao fechar ObjectOutputStream: " + e.getMessage());
	    }

	    try {
	        if (sock != null && !sock.isClosed()) {
	            sock.close();
	        }
	    } catch (IOException e) {
	        System.err.println("Erro ao fechar socket: " + e.getMessage());
	    }
	}
}