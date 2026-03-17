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
import java.util.Scanner;

public class mySaude {

    public static void main(String[] args) {
        System.out.println("cliente: main");
        mySaude client = new mySaude();
        client.startClient();
    }

    public void startClient() {
        Socket soc = null;

        try {
            soc = new Socket("localhost", 23456);

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