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
	        mySaude client = new mySaude();
	        Map<String, String> flags = argsMapping(args);
	        
	        System.out.printf("flags: ", flags);
	        
	        
	        String option = inicialize(flags);
	        
            if(option == null) {
                throw new IllegalArgumentException("There is no option");
            }
            //sends type of operation to server
            client.objOut.writeUTF(option);
            
            switchCase(option, flags.get(option));
	
	        System.out.println(flags);
	 
	    }catch(Exception e){
	        	System.out.println("\n\nIt seems like your option has an error:\n\n" + e.getMessage());
	        	return;
	     }

    }
    
    private static void switchCase(String option, String value) {
    	switch (option) {
	
	        // 2A. Transferência de Ficheiros
	        case "-e":
	            System.out.println("-e: Envia ficheiros para o servidor.");
	            client.sendFiles(value);
	            break;
	        case "-r":
	            System.out.println("-r: Recebe ficheiros do servidor.");
	            break;
	
	        // 2B. Criptografia
	        case "-c":
	            System.out.println("-c: Cifra ficheiros localmente (AES + RSA).");
	            break;
	        case "-d":
	            System.out.println("-d: Decifra ficheiros usando a chave local.");
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
	            	if (option != null) {
	                    throw new IllegalArgumentException(
	                        "There can only be one option!\nWhat was flagged:\n\t "
	                        + option + "\n\t " + key
	                    );
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
	
	
	public void sendFiles(String filePaths) {
	    if (client.sock == null || client.objOut == null) {
	        System.out.println("Socket is not connected. Call startClient first.");
	        return;
	    }

	    // Split the paths by some delimiter (e.g., ";" or ",")
	    String[] files = filePaths.split(";"); // adjust delimiter as needed

	    try {
	        DataOutputStream dataOut = new DataOutputStream(client.sock.getOutputStream());

	        // First, send the number of files
	        dataOut.writeInt(files.length);

	        for (String path : files) {
	            File file = new File(path.trim());
	            if (!file.exists()) {
	                System.out.println("File does not exist: " + path);
	                continue;
	            }

	            // Send file name length and name
	            dataOut.writeUTF(file.getName());

	            // Send file length
	            dataOut.writeLong(file.length());

	            // Send file content
	            FileInputStream fis = new FileInputStream(file);
	            byte[] buffer = new byte[8192];
	            int read;
	            while ((read = fis.read(buffer)) > 0) {
	                dataOut.write(buffer, 0, read);
	            }
	            fis.close();

	            System.out.println("File sent successfully: " + path);
	        }

	        dataOut.flush();
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
}