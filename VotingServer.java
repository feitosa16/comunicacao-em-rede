package network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class VotingServer {
    private static final int PORT = 12345;
    private static Set<String> voters = new HashSet<>(); // Armazena CPFs votados
    private static Map<String, String> votes = new HashMap<>(); // Mapeia CPF para voto
    private static String question = "Qual a sua cor favorita?";
    private static String[] options = {"Azul", "Verde", "Vermelho"};
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor de votação iniciado...");
        
        ExecutorService executor = Executors.newCachedThreadPool(); // Para atendimento multithread
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
            executor.submit(new VotingClientHandler(clientSocket));
        }
    }

    // Classe para gerenciar a comunicação com cada cliente
    static class VotingClientHandler implements Runnable {
        private Socket clientSocket;

        public VotingClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                 ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {
                 
                // Enviar pergunta e opções
                outputStream.writeObject(question);
                outputStream.writeObject(options);
                outputStream.flush();

                // Receber CPF e validar
                String cpf = (String) inputStream.readObject();
                if (!isValidCPF(cpf) || voters.contains(cpf)) {
                    outputStream.writeObject("CPF inválido ou já votou. Tente novamente.");
                    outputStream.flush();
                    return;
                }
                voters.add(cpf);
                
                // Receber voto e armazenar
                String vote = (String) inputStream.readObject();
                votes.put(cpf, vote);

                // Confirmar voto
                outputStream.writeObject("Voto registrado com sucesso.");
                outputStream.flush();

                // Encerrar a conexão
                clientSocket.close();
                
                // Exibir resultados parciais (opcional)
                displayPartialResults();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean isValidCPF(String cpf) {
            return cpf != null && cpf.matches("\\d{11}");
        }

        private void displayPartialResults() {
            System.out.println("Resultados parciais:");
            Map<String, Integer> resultMap = new HashMap<>();
            for (String vote : votes.values()) {
                resultMap.put(vote, resultMap.getOrDefault(vote, 0) + 1);
            }
            for (String option : options) {
                System.out.println(option + ": " + resultMap.getOrDefault(option, 0) + " votos");
            }
        }
    }
}

