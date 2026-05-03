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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map; 
import java.util.Scanner;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec; 

public class criarUser {

	// MAC Logic Variables
	public static String macPassword; 
	private static final String USERS_FILE = "../servidor/users.txt";
	private static final String MAC_FILE = "../servidor/mySaude.mac";
	
	private static final Set<String> FUNCTIONS = Set.of(
		    "utente", "medico"
		);

	public static byte[] generateSalt() {
	    byte[] salt = new byte[16];
	    new SecureRandom().nextBytes(salt);
	    return salt;
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
	public static byte[] hashPassword(String password, byte[] salt) throws Exception {
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(salt);
	    return md.digest(password.getBytes());
	}
	
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
	public static void atualizarMac(String password) throws Exception {
	    String novoMac = calcularMac(password);
	    if (!novoMac.isEmpty()) {
	        try (FileWriter writer = new FileWriter(MAC_FILE)) {
	            writer.write(novoMac);
	        }
	    }
	}
	
	public static boolean userExists(File file, String username) throws IOException {

	    if (!file.exists()) {
	        return false; // no file = no users
	    }

	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String line;

	        while ((line = reader.readLine()) != null) {

	            if (line.isBlank()) continue; // skip empty lines

	            String[] parts = line.split(":");

	            if (parts.length < 1) continue; // avoid malformed lines

	            if (parts[0].equals(username)) {
	                return true;
	            }
	        }
	    }

	    return false;
	}
	public static void main(String[] args) throws Exception{
		try {
            // --- INÍCIO DA ALÍNEA B (Pedir e Validar MAC) ---
			
			String macPassword = readPassword("server> Enter MAC password to start: ");

			// Verifica a integridade antes de fazer o que quer que seja
			if (new File("../servidor/users.txt").exists()) {
                if (!verificarMac(macPassword)) { // <-- Alterado de MacHelper para mySaudeServer
                    System.out.println("FATAL error> The file users.txt was altered or the password for the MAC is wrong!");
                    return; // Pára imediatamente a execução
                }
            }
			// --- FIM DA ALÍNEA B ---

			System.out.println("system> creating user...");
			String keyStore = "../keystore/";
			Map<String, String> flags = argsMapping(args, keyStore);
			String userFile = keyStore + "keystore.users";
			
			//C.
			try (FileInputStream fis = new FileInputStream(flags.get("certFile"))) {

			    // A. Confidencialidade de passwords - gerar o user.txt
			    File usersDir = new File("../servidor/users.txt");

			    if (!usersDir.exists()) {
			        System.out.println("system> users.txt doesn't exist, creating users.txt...");
			        try {
			            usersDir.createNewFile();
			        } catch (IOException e) {
			            throw new IOException("Failed to create users.txt");
			        }
			    }
			    if (userExists(usersDir, flags.get("username"))) {
			        throw new IllegalArgumentException("User already exists in users.txt");
			    }
			    
		        byte[] salt = generateSalt();
		        byte[] hash = hashPassword(flags.get("password"), salt);

		        String saltBase64 = Base64.getEncoder().encodeToString(salt);
		        String hashBase64 = Base64.getEncoder().encodeToString(hash);

		        try (FileWriter writer = new FileWriter(usersDir, true)) {
		            writer.write(
		                flags.get("username") + ":" +
		                flags.get("function") + ":" +
		                saltBase64 + ":" +
		                hashBase64 + "\n"
		            );
		        }
	            System.out.println("system> user inserted into users.txt");

			    // 1. Ler o certificado 
			    CertificateFactory cf = CertificateFactory.getInstance("X.509");
			    Certificate cert = cf.generateCertificate(fis);
			    System.out.println("system> Cert read with success!");

			    // 2. Lógica da KeyStore
			    char[] ksPassword = readPassword("server> Define a the keyStore password to start: ").toCharArray();

			    java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
			    String ksPath = userFile; 

			    File ksFile = new File(ksPath);
			    if (ksFile.exists()) {
			        try (FileInputStream ksfis = new FileInputStream(ksFile)) {
			            ks.load(ksfis, ksPassword);
			        }
			    } else {
			        ks.load(null, ksPassword); // Inicializa se não existir
			    }

			    
			    if(ks.getCertificate(flags.get("username")) != null) {
			        throw new IllegalArgumentException(
				            "The certificate for user " + flags.get("username") + " already exists"
				        );
			    }
			    // 3. Adicionar o certificado 
			    // O alias tem de ser o username
			    ks.setCertificateEntry(flags.get("username"), cert);

			    // 4. Gravar a KeyStore no disco
			    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(ksPath)) {
			        ks.store(fos, ksPassword);
			        System.out.println("system> Cert added to keystore.users with success!");
			    }
			    
			    // 5. criar pasta do user
			    File userDir = new File("../servidor/" + flags.get("username"));

			    if (!userDir.exists()) {
			        boolean created = userDir.mkdirs(); // mkdirs() cria também pastas pai se necessário
			        if (created) {
			            System.out.println("system> Created a directory for the user: " + userDir.getPath());
			        } else {
			        	throw new IllegalArgumentException("error> it was not possible to create the users directory.");
			        }
			    } else {
			        System.out.println("system> The users directory alrady exists..");
			    }
			    
			} catch (Exception e) {
			    System.err.println("erro> Failed in manging the keystore: " + e.getMessage());
			    throw e;
			}
			


            // --- INÍCIO DA ALÍNEA B (Atualizar MAC) ---
            atualizarMac(macPassword); 
            System.out.println("system> The file mySaude.mac updated with success!");
            // --- FIM DA ALÍNEA B ---

		 } catch (Exception e) {
		        System.out.println("ERROR: " + e.getMessage());
		    }
		}
	
	public static Map<String, String> argsMapping(String[] args, String keyStore) {
	    Map<String, String> map = new HashMap<>();

	    if (args.length < 5) {
	        throw new IllegalArgumentException(
	            "Missing arguments.\nUsage: criarUser <username> <funcao> <password> -f <cert_file>"
	        );
	    }
	    String username = args[0];
	    if(username.contains(":")) {
            System.err.println("error> The username should not contain ':'.");
	    }
	    String function = args[1];
	    if(!FUNCTIONS.contains(function)) {
            System.err.println("erro> The function should be: 'medico' or 'utente'.");
            System.exit(-1); 
	    }
	    String password = args[2];

	    if (!args[3].equals("-f")) {
	        throw new IllegalArgumentException("Expected -f flag");
	    }

	    String certFile = keyStore + args[4];

	    map.put("username", username);
	    
	    map.put("function", function);
	    
	    map.put("password", password);
	    map.put("certFile", certFile);

	    //
	    if (!certFile.contains(username)) {
	        throw new IllegalArgumentException("certFile is not from the correct user");
	    }

	    return map;
	}
}
