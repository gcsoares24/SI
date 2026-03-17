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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class mySaude {

	private int port;

    public static void main(String[] args) {
        System.out.println("cliente> A iniciar...");
        mySaude client = new mySaude();
        
        Map<String, String> flags = new HashMap<>();
        
        boolean inFlag = false;
        String currentFlag = "";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("-")) {
            	currentFlag = arg;
            	flags.put(arg, args[i + 1]);
                i++;
            }else {
            	flags.put(currentFlag, flags.get(currentFlag) + ";" + arg);
            }
        }

        client.startClient();
        
        // Flags
        for (String key : flags.keySet()) {
            switch (key) {
                // 1. Flags de Conexão e Identificação
                case "-s":
                    System.out.println("Servidor>-s: A definir o endereço IP e o porto do servidor.");
                    try {
                        int port = Integer.parseInt(args[0]);

                        if (port < 1 || port > 65535) {
                            System.out.println("Invalid port! Must be 1-65535.");
                            return;
                        }

                        client.port = port;

                    } catch (NumberFormatException e) {
                        System.out.println("Port must be a number!");
                        return;
                    }
                    break;
                case "-u":
                    System.out.println("-u: Identifica o utilizador que executa o comando.");
                    break;
                case "-p":
                    System.out.println("-p: Password para aceder à keystore local do utilizador.");
                    break;
                case "-t":
                    System.out.println("-t: Define o destinatário ou o autor da operação.");
                    break;

                // 2A. Transferência de Ficheiros
                case "-e":
                    System.out.println("-e: Envia ficheiros para o servidor.");
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
                    System.out.println("Unknown flag: " + key);
            }
        }

        System.out.println(flags);
 
       

    }

    public void startClient() {
        Socket soc = null;

        try {
            soc = new Socket("localhost", port);

            ObjectOutputStream outStream = new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(soc.getInputStream());

            Scanner sc = new Scanner(System.in);

            System.out.print("Username: ");
            String user = sc.nextLine();

            System.out.print("Password: ");
            String passwd = sc.nextLine();

            //Send user and pass
            outStream.writeObject(user);
            outStream.writeObject(passwd);
            outStream.flush();

            // receive the response
            Boolean response = (Boolean) inStream.readObject();

            if (response) {
                System.out.println("Login aceite.");
            } else {
                System.out.println("Login rejeitado.");
            }
         // After login success

            DataOutputStream dataOut =
                    new DataOutputStream(soc.getOutputStream());

            File file = new File("../pdfs/enviar.pdf");
            FileInputStream fis = new FileInputStream(file);

            dataOut.writeLong(file.length());

            byte[] buffer = new byte[8192];
            int read;

            while ((read = fis.read(buffer)) > 0) {
                dataOut.write(buffer, 0, read);
            }

            dataOut.flush();
            fis.close();
            

            outStream.close();
            inStream.close();
            soc.close();
            sc.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}