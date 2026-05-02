/***************************************************************************
*   Seguranca Informatica
*	Projeto 2:
*		- Guilherme Soares
*		- Vitória Correia
*		- Duarte Soares
*
*	mySaude.java
*
***************************************************************************/

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;

import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.Signature;


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
	

	private static final Set<String> NEEDS_RECEIVER = Set.of(
	    "-e", "-c", "-ce", "-v", "-ae", "-rv", "-ace", "-rdv"
	);

	private static final Set<String> NEEDS_SERVER = Set.of(
	    "-e", "-r","-c" , "-d",  "-ce", "-rd", "-ae", "-rv", "-ace", "-rdv"
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
			System.out.println("cliente> Inicializing...");
			
			
			Map<String, String> flags = argsMapping(args);
	       
	        
	        String option = inicialize(flags);
	        
            if(option == null) {
                throw new IllegalArgumentException("There is no option");
            }
            validateRequiredFlags(option);
            
            //sends only server type of operation
            if (SERVER_OPTIONS.contains(option)) {
            	if (client.objOut == null) {
                    throw new IllegalArgumentException("The option " + option + " requires connection to the server (-s).");
                }
            	
            	client.objOut.writeObject(option);
                client.objOut.flush();
            }
            
            switchCase(option, flags.get(option));
	 
	    }catch(Exception e){
	        	System.out.println("\n\nIt seems like your option has an ERROR:\n\n" + e.getMessage());
	        	return;
	     } finally {
	         client.closeClientResources();
	     }

    }
	
	private static void require(boolean condition, String message) {
	    if (!condition) {
	        throw new IllegalArgumentException(message);
	    }
	}
	
	private static void validateRequiredFlags(String option) {
	    String base = "The option " + option + " requires ";

	    // -u -p| user and password is always required
	    require(client.username != null, base + "-u.");
	    require(client.password != null, base + "-p.");

	    
	    // -t |receiver required
	    if (NEEDS_RECEIVER.contains(option)) {
	        require(client.receiver != null, base + "-t.");
	    }

	    // -s | server connection required
	    if (NEEDS_SERVER.contains(option)) {
	        require(client.objOut != null && client.objIn != null,
	                base + "connection to server (-s).");
	    }
	}
	
    private static void switchCase(String option, String value) throws EOFException {
    	String filesToSend;
    	switch (option) {
	        // 2A. Transferência de Ficheiros
	        case "-e":
	            if(client.receiver == null) {
	            	throw new EOFException("There is no -t (receiver).");
	            }
	            client.sendFiles(value, client.receiver);
	            break;
	        case "-r":
	            client.receiveFiles(value);
	            break;
	
	        // 2B. Criptografia
	        case "-c":
				client.encryptFiles(value, client.receiver);
	            break;
	        case "-d":
				client.decryptFiles(value);
	            break;
	
	        // 2C. Assinatura Digital
	        case "-a":
				client.signFiles(value);
	            break;
	        case "-v":
				if(client.receiver == null) {
	            	throw new EOFException("The parameter -t is missing with the username of who signed.");
	            }
	            client.verifySignatures(value, client.receiver);
	            break;
	
	        // 2D. Operações Combinadas
	        case "-ce":
	            if(client.receiver == null) {
	                throw new EOFException("There is no -t (receiver).");
	            }

	            filesToSend = client.encryptFiles(value, client.receiver);

	            if (filesToSend.isEmpty()) {
	                System.out.println("No files to send.");
	                break;
	            }

	            client.sendFiles(filesToSend, client.receiver);
	            break;

	        case "-rd":

	            String filesToDecrypt = client.receiveFiles(value);

	            client.decryptFiles(filesToDecrypt);
	            break;
	        case "-ae":
	        	filesToSend = client.signFiles(value);
		    	
		        client.sendFiles(filesToSend, client.receiver);
		        break;
	        case "-rv":
	            String filesToVerify  = client.receiveFiles(value);
	            
	            client.verifySignatures(filesToVerify, client.receiver);
	            break;
	        case "-ace":
	        	client.signEncryptSend(value, client.receiver);
	            break;
	        case "-rdv":
	        	client.receiveDecryptVerify(value, client.receiver);
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

        // 1. Primeiro descobrir a opção
        for (String key : flags.keySet()) {
            if (OPTIONS.contains(key)) {
                if (option != null) {
                    throw new IllegalArgumentException(
                        "There can only be one option!\nWhat was flagged:\n\t " + option + "\n\t " + key
                    );
                }
                option = key;
            }
        }

        // 2. Guardar username/password/receiver
        if (flags.containsKey("-u")) {
            client.username = flags.get("-u");
        }

        if (flags.containsKey("-p")) {
            client.password = flags.get("-p");
        }

        if (flags.containsKey("-t")) {
            client.receiver = flags.get("-t");
        }

        // 3. Só configurar TLS e ligar se a opção precisar de servidor
        if (NEEDS_SERVER.contains(option)) {
            if (!flags.containsKey("-s")) {
                throw new IllegalArgumentException("A opção " + option + " requer -s endereço:porto.");
            }

            configureClientTLS();
            client.startClient(flags.get("-s").split(":"));
        }

        return option;
    }
	
	private static String getBasePath() {
	    return System.getProperty("user.home")
	            + File.separator + "mySaude";	            
	}

	private static void configureClientTLS() {
		String trustStorePath = getBasePath() + File.separator + "keystore" + File.separator + "truststore.client";

	    File trustStoreFile = new File(trustStorePath);


	    if (!trustStoreFile.exists()) {
	        throw new IllegalArgumentException(
	            "ERROR TLS: clients truststore was not found at: " + trustStorePath +
	            "\nPlace truststore.client in there and try again."
	        );
	    }

	    String trustStorePassword = readPassword("Password da truststore do cliente: ");

	    System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
	    System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
	    System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

	    System.out.println("TLS> truststore used: " + trustStoreFile.getAbsolutePath());
	}
	
	private static String readPassword(String prompt) {
	    if (System.console() != null) {
	        return new String(System.console().readPassword(prompt));
	    }

	    System.out.print(prompt);
	    try {
	        byte[] buffer = new byte[128];
	        int len = System.in.read(buffer);
	        return new String(buffer, 0, len).trim();
	    } catch (IOException e) {
	        throw new RuntimeException("Error reading the password.", e);
	    }
	}

	public void startClient(String[] address) throws ConnectException {
	    System.out.println("-s: Defining servers Ip and port.");

	    int port;
	    String ip;

	    try {
	        if (address.length != 2) {
	            throw new IllegalArgumentException("The server address must be in the format IP:port.");
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
	    	SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();

	        SSLSocket sslSock = (SSLSocket) sf.createSocket(ip, port);

	        /*
	         * Força o handshake TLS imediatamente.
	         * Se a truststore não tiver o certificado do servidor,
	         * ou se o caminho estiver errado, o erro aparece aqui.
	         */
	        sslSock.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
	        sslSock.startHandshake();

	        client.sock = sslSock;

	        SSLSession session = sslSock.getSession();
	        System.out.println("active TLS: " + session.getCipherSuite());
	        System.out.println("TLS server: " + session.getPeerHost());
	    	
	        client.objOut = new ObjectOutputStream(client.sock.getOutputStream());
	        client.objIn = new ObjectInputStream(client.sock.getInputStream());

	        System.out.println("Connected to server at " + ip + ":" + port);
	        

	    } catch (SSLHandshakeException e) {
	    	throw new RuntimeException(
	    	        "TLS ERROR: Could not verify the server certificate.\n" +
	    	        "Confirm if truststore.client contains the server certificate.\n" +
	    	        "Also confirm if the certificate alias is as expected.\n" +
	    	        "Detail: " + e.getMessage(), e
	    	    );

	    } catch (ConnectException e) {
	        throw new ConnectException("\tConnection refused. Make sure the mySaudeServer is running at the specified address and port.");
	    } catch (IOException e) {
	        throw new RuntimeException("Failed to connect with the server: " + e.getMessage(), e);
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
	            System.out.println("ERROR: user directory '" + receiver + "' doesnt exist.");
	            return;
	        }

	        if (!serverResponse.equals(OK)) {
	            System.out.println("ERROR: invalid answer from the server.");
	            return;
	        }

	        // enviar ficheiros
	        for (String path : paths) {
	        	File file = new File(path.trim());
	        	
	            if (!file.exists() || !file.isFile()) {
	                System.out.println("ERROR: you dont have the file: " + path.trim());

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
	                System.out.println("ERROR: The file " + file.getName() + " or any of its variations, already exists on the serverr");
	                continue;
	            }

	            if (!fileResponse.equals(OK_TO_SEND)) {
	                System.out.println("ERROR: invalid response from the server to the file " + file.getName());
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

	            System.out.println("File sent with success: " + file.getName());
	        }

	    } catch (IOException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	
	public String receiveFiles(String filePaths) {
	    if (client.sock == null || client.objOut == null || client.objIn == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        return "";
	    }

	    String[] requestedFiles = filePaths.split(";");
	    String receivedFiles = "";

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
	                System.out.println("ERROR: the directory of the user, '" + client.username + "', doesnt exist in the server.");
	                return receivedFiles;
	            }

	            if (status.equals(FILE_NOT_FOUND_FLAG)) {
	                System.out.println("ERROR: file doesnt exist on the server: " + fileName);
	                continue;
	            }

	            if (!status.equals(FILE_INFO)) {
	                System.out.println("ERRO: invalid response from the server to the file " + fileName);
	                continue;
	            }

	            long fileSize = client.objIn.readLong();
	            File outFile = new File("../eu/" + fileName);
	            File parentDir = outFile.getParentFile();

	            if (!parentDir.exists()) {
	                parentDir.mkdirs();
	            }
	            
	            //guardar so cifrados, recebidos
	            if (fileName.contains(".cifrado") || fileName.contains(".assinado") || fileName.contains(".envelope")) {
	                if (!receivedFiles.isEmpty()) {
	                    receivedFiles += ";";
	                }
	                receivedFiles += "../eu/" + fileName;
	            }

	            if (outFile.exists()) {
	                System.out.println("ERRO: the file already already exists on the client side: " + fileName);
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
	                        throw new EOFException("Unexpected end whle retrieving the file" + fileName);
	                    }

	                    fos.write(buffer, 0, bytesRead);
	                    remaining -= bytesRead;
	                }
	            }

	            System.out.println("File retrieved successfully: " + fileName);

	            
	        }

	    } catch (IOException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }

	    return receivedFiles;
	}
	
	private Certificate loadCertFromFile(File certFile) {
	    try (FileInputStream fis = new FileInputStream(certFile)) {
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        return cf.generateCertificate(fis);
	    } catch (Exception e) {
	        return null;
	    }
	}
	public Certificate receiveCert(KeyStore ks) throws NoSuchAlgorithmException, CertificateException {
	    if (client.sock == null || client.objOut == null || client.objIn == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        
	    }
	    
		try {
		    // send tag and targetUser
		    client.objOut.writeObject("GET_CERT");
		    client.objOut.flush();
		    
		    client.objOut.writeUTF(client.receiver);
		    client.objOut.flush();
		    
		    
		    // get file
		    String status = (String) client.objIn.readUTF();
		    
		    File certFile = new File("../keystore/" + client.receiver + ".cert");

		    if (status.equals(FILE_NOT_FOUND_FLAG)) {
                System.out.println("ERROR: Keystore in server does not exist!");
                return null;
            }
		    if (status.equals("NOT_FOUND")) {
                System.out.println("ERROR: that user is not the servers keystore!");
                return null;
            }
		    if (!status.equals(OK)) {
                System.out.println("ERROR: INVALID RESPONSE TRYING TO GET CERT." );
                return null;
            }
            
            if (certFile.exists()) {
                System.out.println("ERROR: The cert file for the user " + client.receiver + " already exists here...\n");
                client.objOut.writeUTF(CLIENT_FILE_EXISTS);
                client.objOut.flush();
            }else {
            	client.objOut.writeUTF(OK);
                client.objOut.flush();

                long fileSize = client.objIn.readLong();
                try (FileOutputStream fos = new FileOutputStream(certFile)) {
                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;

                    while (remaining > 0) {
                        int bytesRead = client.objIn.read(buffer, 0, (int)Math.min(buffer.length, remaining));

                        if (bytesRead == -1) {
                            throw new EOFException("Unexpected ending while receiving the cert\n");
                        }

                        fos.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }
                }

                System.out.println("The cert file for the user " + client.receiver + " was created sucessfully!\n");
    		    
            }
            System.out.println("Loading the cert...\n");
            Certificate cert = loadCertFromFile(certFile);
            ks.setCertificateEntry(client.receiver, cert);
	         // SAVE KEYSTORE TO FILE (THIS IS WHAT YOU ARE MISSING)
	            try (FileOutputStream fos =
	                     new FileOutputStream("../keystore/keystore." + client.username)) {
	                ks.store(fos, client.password.toCharArray());
	            }
            
            
    		return loadCertFromFile(certFile);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	    
	    
	    
	}
	
	public String encryptFiles(String filePaths, String targetUser) {
	    String[] paths = filePaths.split(";");
	    String result = "";

	    try {
	        KeyStore ks = KeyStore.getInstance("PKCS12");
	        try (FileInputStream fis = new FileInputStream("../keystore/keystore." + this.username)) {
	            ks.load(fis, this.password.toCharArray());
	        }
	        System.out.println("okok");

	        Certificate cert = ks.getCertificate(targetUser);
	        if (cert == null) {

	            System.out.println("ERROR(FIXABLE...): Certificate for " + targetUser + " is not in its keystore! ");
	            System.out.println("FIXING...: Getting cert from server......");
	            cert = receiveCert(ks);
	            
	        }
	        PublicKey publicKey = cert.getPublicKey();

	        for (String path : paths) {
	            String trimmed = path.trim();
	            File inputFile = new File(trimmed);

	            if (!inputFile.exists()) {
	                System.err.println("ERRO: O ficheiro '" + trimmed + "' não foi encontrado.");
	                continue;
	            }

	            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
	            keyGen.init(128);
	            SecretKey aesKey = keyGen.generateKey();

	            Cipher aesCipher = Cipher.getInstance("AES");
	            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
	            
	            String encryptedPath = trimmed + ".cifrado";
	            try (FileInputStream fis = new FileInputStream(inputFile);
	                 FileOutputStream fos = new FileOutputStream(encryptedPath)) {
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

	            String keyPath = trimmed + ".chave." + targetUser;
	            try (FileOutputStream fos = new FileOutputStream(keyPath)) {
	                fos.write(wrappedKey);
	            }

	            System.out.println("Ciphered file: " + encryptedPath);

	            // 🔥 adicionar ao resultado
	            if (!result.isEmpty()) {
	                result += ";";
	            }
	            result += encryptedPath + ";" + keyPath;
	        }

	    } catch (Exception e) {
	        System.err.println("Error: while trying to cypher: " + e.getMessage());
	    }

	    return result;
	}

	public void decryptFiles(String filePaths) {
	    String[] paths = filePaths.split(";");
	    try {
	        KeyStore ks = KeyStore.getInstance("PKCS12");
	        try (FileInputStream fis = new FileInputStream("../keystore/keystore." + this.username)) {
	            ks.load(fis, this.password.toCharArray());
	        }

	        PrivateKey privateKey = (PrivateKey) ks.getKey(this.username, this.password.toCharArray());

	        for (String path : paths) {
	            String baseName = path.replace(".cifrado", "");
	            baseName = baseName.replace(".envelope", "");
	            File keyFile = new File(baseName + ".chave." + this.username);
	            if (!keyFile.exists()) {
	                System.err.println("ERROR: The key-file not found for: " + path);
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
	                FileOutputStream fos = new FileOutputStream(baseName)){
	                byte[] buffer = new byte[8192];
	                int read;
	                while ((read = fis.read(buffer)) > 0) {
	                    fos.write(aesCipher.update(buffer, 0, read));
	                }
	                fos.write(aesCipher.doFinal());
	            }
	            System.out.println("File deCyphered successfully.");
	        }
	    } catch (Exception e) {
	        System.err.println("ERROR: while decyphering: " + e.getMessage());
	    }
	}


	public String signFiles(String filePaths) {
	    String[] paths = filePaths.split(";");
	    StringBuilder result = new StringBuilder();

	    try {
	        KeyStore ks = KeyStore.getInstance("PKCS12");
	        try (FileInputStream fis = new FileInputStream("../keystore/keystore." + this.username)) {
	            ks.load(fis, this.password.toCharArray());
	        }

	        PrivateKey privateKey = (PrivateKey) ks.getKey(this.username, this.password.toCharArray());

	        for (String path : paths) {
	            String trimmedPath = path.trim();
	            File inputFile = new File(trimmedPath);

	            if (!inputFile.exists()) {
	                System.err.println("ERROR: the file '" + trimmedPath + "' was not found!");
	                continue;
	            }

	            Signature signature = Signature.getInstance("SHA256withRSA");
	            signature.initSign(privateKey);

	            try (FileInputStream fis = new FileInputStream(inputFile)) {
	                byte[] buffer = new byte[8192];
	                int read;
	                while ((read = fis.read(buffer)) > 0) {
	                    signature.update(buffer, 0, read);
	                }
	            }

	            byte[] digitalSignature = signature.sign();

	            String sigFileName = trimmedPath + ".assinatura." + this.username;
	            try (FileOutputStream fos = new FileOutputStream(sigFileName)) {
	                fos.write(digitalSignature);
	            }

	            System.out.println("File was signed: " + sigFileName);

	            if (result.length() > 0) result.append(";");
	            result.append(sigFileName).append(";").append(trimmedPath);
	        }

	    } catch (Exception e) {
	        System.err.println("ERROR trying to sign: " + e.getMessage());
	    }

	    return result.toString();
	}

	public void verifySignatures(String filePaths, String targetUser) {
		String[] paths = filePaths.split(";");
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			try (FileInputStream fis = new FileInputStream("../keystore/keystore." + this.username)) {
				ks.load(fis, this.password.toCharArray());
			}

			Certificate cert = ks.getCertificate(targetUser);
			if (cert == null) {
				System.err.println("ERRO: Cert of the user, '" + targetUser + "', was not found in the keystore.");
				return;
			}
			PublicKey publicKey = cert.getPublicKey();

			for (String path : paths) {
				File inputFile = new File(path.trim());
				File sigFile = new File(path.trim().replace(".assinado", "") + ".assinatura." + targetUser);

				if (!inputFile.exists() || !sigFile.exists()) {
					System.err.println("ERROR: The File or the Signature was not found (" + path.trim() + ")");
					continue;
				}

				byte[] sigBytes = new byte[(int) sigFile.length()];
				try (FileInputStream fis = new FileInputStream(sigFile)) {
					fis.read(sigBytes);
				}

				Signature signature = Signature.getInstance("SHA256withRSA");
				signature.initVerify(publicKey);

				try (FileInputStream fis = new FileInputStream(inputFile)) {
					byte[] buffer = new byte[8192];
					int read;
					while ((read = fis.read(buffer)) > 0) {
						signature.update(buffer, 0, read);
					}
				}

				boolean isCorrect = signature.verify(sigBytes);
				if (isCorrect) {
					System.out.println("Signature VALID for the file: " + path.trim());
				} else {
					System.out.println("Signature INVALID for the file: " + path.trim());
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR trying to validate the signature: " + e.getMessage());
		}
	}
	
	public void signEncryptSend(String filePaths, String targetUser) {
	    String[] paths = filePaths.split(";");
	    StringBuilder filesToSend = new StringBuilder();

	    for (String path : paths) {
	        String trimmedPath = path.trim();

	        // 1) assinar
	        String signedResult = client.signFiles(trimmedPath);
	        if (signedResult == null || signedResult.isEmpty()) {
	            continue;
	        }

	        // signFiles devolve: assinatura;ficheiroOriginal
	        String[] signedParts = signedResult.split(";");
	        String signatureFile = signedParts[0].trim();
	        String originalFile = signedParts[1].trim();

	        // 2) cifrar apenas o ficheiro original
	        String encryptedResult = client.encryptFiles(originalFile, targetUser);
	        if (encryptedResult == null || encryptedResult.isEmpty()) {
	            continue;
	        }

	        // encryptFiles devolve: envelope;chave
	        String[] encryptedParts = encryptedResult.split(";");
	        String encryptedFile = encryptedParts[0].trim();
	        String keyFile = encryptedParts[1].trim();

	        // 3) montar ordem: assinatura ; pdfCifrado ; chave
	        if (filesToSend.length() > 0) {
	            filesToSend.append(";");
	        }

	        filesToSend.append(signatureFile)
	                   .append(";")
	                   .append(encryptedFile)
	                   .append(";")
	                   .append(keyFile);
	    }

	    if (filesToSend.length() == 0) {
	        System.out.println("No file to send.");
	        return;
	    }

	    client.sendFiles(filesToSend.toString(), targetUser);
	}
	
	public void receiveDecryptVerify(String filePaths, String signedUser) {
	    String[] originalFiles = filePaths.split(";");
	    StringBuilder filesToRequest = new StringBuilder();

	    for (String file : originalFiles) {
	        String base = file.trim();

	        if (filesToRequest.length() > 0) {
	            filesToRequest.append(";");
	        }

	        filesToRequest.append(base).append(".envelope")
	                      .append(";")
	                      .append(base).append(".chave.").append(this.username)
	                      .append(";")
	                      .append(base).append(".assinatura.").append(signedUser);
	    }

	    String received = client.receiveFiles(filesToRequest.toString());

	    if (received == null || received.isEmpty()) {
	        System.out.println("No file to send.");
	        return;
	    }
	    client.decryptFiles(received);

	    StringBuilder filesToVerify = new StringBuilder();
	    String[] receivedPaths = received.split(";");

	    for (String path : receivedPaths) {
	        String trimmed = path.trim();

	        if (trimmed.endsWith(".envelope")) {
	            String originalFile = trimmed.replace(".envelope", "");

	            if (filesToVerify.length() > 0) {
	                filesToVerify.append(";");
	            }
	            filesToVerify.append(originalFile);
	        }
	    }

	    client.verifySignatures(filesToVerify.toString(), signedUser);
	}
	
	private void closeClientResources() {
	    try {
	        if (objIn != null) {
	            objIn.close();
	        }
	    } catch (IOException e) {
	        System.err.println("ERROR: closing ObjectInputStream: " + e.getMessage());
	    }

	    try {
	        if (objOut != null) {
	            objOut.close();
	        }
	    } catch (IOException e) {
	        System.err.println("ERRO: closing ObjectInputStream: " + e.getMessage());
	    }

	    try {
	        if (sock != null && !sock.isClosed()) {
	            sock.close();
	        }
	    } catch (IOException e) {
	        System.err.println("ERRO: closing the socket: " + e.getMessage());
	    }
	}
}