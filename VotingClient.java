
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class VotingClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {

            // Receber a pergunta e opções
            String question = (String) inputStream.readObject();
            String[] options = (String[]) inputStream.readObject();

            // Exibir a pergunta e opções de voto
            System.out.println(question);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }

            // Obter o CPF
            System.out.print("Digite seu CPF (apenas números): ");
            String cpf = scanner.nextLine();
            outputStream.writeObject(cpf);
            outputStream.flush();

            // Validar CPF e voto
            String serverResponse = (String) inputStream.readObject();
            if (!serverResponse.equals("CPF inválido ou já votou. Tente novamente.")) {
                System.out.print("Escolha seu voto (número da opção): ");
                int voteOption = scanner.nextInt();
                outputStream.writeObject(options[voteOption - 1]);
                outputStream.flush();
                
                // Receber confirmação
                serverResponse = (String) inputStream.readObject();
                System.out.println(serverResponse);
            } else {
                System.out.println(serverResponse);
            }
        } finally {
            socket.close();
        }
    }
}

