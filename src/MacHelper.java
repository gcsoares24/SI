import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileWriter;

public class MacHelper {
    
    private static final String USERS_FILE = "../servidor/users.txt";
    private static final String MAC_FILE = "../servidor/mySaude.mac";

    // 1. Calcula o MAC do ficheiro atual usando a password inserida
    public static String calcularMac(String password) throws Exception {
        File f = new File(USERS_FILE);
        if (!f.exists()) return ""; 

        byte[] fileBytes = Files.readAllBytes(f.toPath());
        byte[] passBytes = password.getBytes();
        
        // Algoritmo HmacSHA256 exigido pelo enunciado
        SecretKeySpec key = new SecretKeySpec(passBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        
        byte[] macBytes = mac.doFinal(fileBytes);
        
        // Retornar em Base64 como exigido
        return Base64.getEncoder().encodeToString(macBytes);
    }

    // 2. Verifica se o MAC guardado bate certo com o ficheiro atual
    public static boolean verificarMac(String password) {
        try {
            File macFile = new File(MAC_FILE);
            File usersFile = new File(USERS_FILE);
            
            // Se nenhum dos dois existe, é a primeira vez que corremos o sistema (válido)
            if (!usersFile.exists() && !macFile.exists()) return true; 
            // Se os users existem mas o MAC desapareceu, há perigo de adulteração!
            if (usersFile.exists() && !macFile.exists()) return false; 
            
            String macGuardado = new String(Files.readAllBytes(macFile.toPath())).trim();
            String macCalculado = calcularMac(password);
            
            return macGuardado.equals(macCalculado);
        } catch (Exception e) {
            return false;
        }
    }

    // 3. Atualiza/Cria o ficheiro mySaude.mac
    public static void atualizarMac(String password) throws Exception {
        String novoMac = calcularMac(password);
        if (!novoMac.isEmpty()) {
            try (FileWriter writer = new FileWriter(MAC_FILE)) {
                writer.write(novoMac);
            }
        }
    }
}