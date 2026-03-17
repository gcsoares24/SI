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
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    flags.put(args[i], args[i + 1]);
                    i++;
                } else {
                    flags.put(args[i], "true");
                }
            }
        }

        System.out.println(flags);
 
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

        client.startClient();
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