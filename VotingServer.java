
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class VotingServer {
    private ServerSocket serverSocket;
    private final int port = 5000;
    private Map<String, Boolean> votedCPFs = new ConcurrentHashMap<>();
    private Map<Integer, Integer> voteCount = new ConcurrentHashMap<>();
    private String question;
    private List<String> options;
    private boolean isVotingOpen = true;

    public VotingServer() {
        loadElectionData();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado na porta " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadElectionData() {
        question = "Qual é seu candidato preferido?";
        options = Arrays.asList("Candidato A", "Candidato B", "Candidato C");
        for (int i = 0; i < options.size(); i++) {
            voteCount.put(i, 0);
        }
    }

    public void start() {
        while (isVotingOpen) {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateReport(ObjectOutputStream out) {
        try {
            // Preparar dados para relatório
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("question", question);
            reportData.put("options", options);
            reportData.put("voteCount", new HashMap<>(voteCount));
            
            // Determinar vencedor
            int winnerIndex = voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
            reportData.put("winner", winnerIndex != -1 ? options.get(winnerIndex) : "Sem vencedor");
            reportData.put("voterList", new ArrayList<>(votedCPFs.keySet()));

            out.writeObject(reportData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                while (true) {
                    Object request = in.readObject();
                    if (request instanceof String) {
                        String command = (String) request;
                        
                        switch (command) {
                            case "GET_ELECTION_DATA":
                                out.writeObject(question);
                                out.writeObject(options);
                                break;
                                
                            case "GET_RESULTS":
                                out.writeObject(getPartialResults());
                                break;
                                
                            case "GENERATE_REPORT":
                                generateReport(out);
                                break;
                                
                            default:
                                break;
                        }
                    } else if (request instanceof Vote) {
                        Vote vote = (Vote) request;
                        if (validateVote(vote)) {
                            processVote(vote);
                            out.writeObject("Voto registrado com sucesso!");
                        } else {
                            out.writeObject("Erro: CPF inválido ou já votou!");
                        }
                    }
                }
            } catch (EOFException e) {
                // Cliente desconectou
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        

        private boolean validateVote(Vote vote) {
            return vote.getCpf().length() == 11 && 
                   !votedCPFs.containsKey(vote.getCpf());
        }

        private void processVote(Vote vote) {
            votedCPFs.put(vote.getCpf(), true);
            voteCount.merge(vote.getOption(), 1, Integer::sum);
        }
    }
    
    public Map<Integer, Integer> getPartialResults() {
        return new HashMap<>(voteCount);
    }
    
}