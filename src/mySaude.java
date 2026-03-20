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
	
	private static final Set<String> OPTIONS = Set.of(
		    "-e", "-r", "-c", "-d",
		    "-ce", "-rd",
		    "-a", "-v",
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
	        
	        System.out.printf("flags: ", flags);
	        
	        
	        String option = inicialize(flags);
	        
            if(option == null) {
                throw new IllegalArgumentException("There is no option");
            }
			//sends type of operation to server
            client.objOut.writeObject(option);
            client.objOut.flush();
            
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

	    int port = 1024; // default
	    String ip = "localhost"; // default

	    try {
	        ip = address[0];
	        port = Integer.parseInt(address[1]);

	        if (port < 1 || port > 65535) {
	            System.out.println("Invalid port! Must be 1-65535.");
	            return;
	        }

	    } catch (NumberFormatException e) {
	        System.out.println("Port must be a number!");
	        return;
	    }

	    try {
	        // Only create socket and object streams here
	        client.sock = new Socket(ip, port);
	        client.objOut = new ObjectOutputStream(client.sock.getOutputStream());
	        client.objIn = new ObjectInputStream(client.sock.getInputStream());

	        System.out.println("Connected to server at " + ip + ":" + port);

	    } catch (ConnectException e) {
	        throw new ConnectException("\tConnection refused. Make sure the mySaudeServer is running at the specified address and port.");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public void sendFiles(String filePaths, String receiver) {
	    if (client.sock == null || client.objOut == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        return;
	    }

	    String[] paths = filePaths.split(";");

	    try {
	        client.objOut.writeInt(paths.length);
	        client.objOut.flush();
	        
	        client.objOut.writeUTF(receiver);
	        client.objOut.flush();
	    	

	        
	        for (String path : paths) {
	            File file = new File(path.trim());
	            if (!file.exists()) {
	                System.out.println("Skipping missing file: " + path);
	                continue; // skip this file
	            }

	            // Envia o nome do ficheiro
	            client.objOut.writeObject(file.getName());
	            // Lê o conteúdo do ficheiro
				FileInputStream fis = new FileInputStream(file);
	            byte[] fileBytes = fis.readAllBytes();
	            fis.close();

	            // Envia o conteúdo do ficheiro como byte[]
				// Envia o conteúdo do ficheiro como byte[]
	            client.objOut.writeObject(fileBytes);
	            client.objOut.flush();

	            System.out.println("File sent successfully: " + path);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public void receiveFiles(String filePaths) {
	    try {
	        DataOutputStream dataOut = new DataOutputStream(client.sock.getOutputStream());
	        DataInputStream dataIn = new DataInputStream(client.sock.getInputStream());

	    	String[] requestedFiles = filePaths.split(";");

	        // 🔹 enviar lista de ficheiros pedidos
	        dataOut.writeInt(requestedFiles.length);
	        for (String f : requestedFiles) {
	            dataOut.writeUTF(f);
	        }
	        dataOut.flush();
			
			// 🔹 receber ficheiros
	        int numFiles = dataIn.readInt();

	        for (int i = 0; i < numFiles; i++) {

	            String fileName = dataIn.readUTF();
	            long fileSize = dataIn.readLong();

	            FileOutputStream fos = new FileOutputStream(fileName);

	            byte[] buffer = new byte[8192];
	            long remaining = fileSize;
	            int read;

	            while (remaining > 0 &&
	                   (read = dataIn.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {

	                fos.write(buffer, 0, read);
	                remaining -= read;
	            }

	            fos.close();
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public void encryptFiles(String filePaths, String targetUser) {
	String[] paths = filePaths.split(";");
	try {
		KeyStore ks = KeyStore.getInstance("JKS");
		try (FileInputStream fis = new FileInputStream("keystore." + this.username)) {
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
			if (!inputFile.exists()) continue;

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
		try (FileInputStream fis = new FileInputStream("keystore." + this.username)) {
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
				 FileOutputStream fos = new FileOutputStream(baseName + ".decrypted")) {
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
}