import java.io.*;
import java.net.*;
import java.util.Map;

public class VotingClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String question;
    private java.util.List<String> options;

    public boolean connect() {
        try {
            socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Solicitar dados da eleição
            out.writeObject("GET_ELECTION_DATA");
            question = (String) in.readObject();
            options = (java.util.List<String>) in.readObject();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<Integer, Integer> getPartialResults() {
        try {
            out.writeObject("GET_RESULTS");
            return (Map<Integer, Integer>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getQuestion() {
        return question;
    }

    public java.util.List<String> getOptions() {
        return options;
    }

    public String sendVote(String cpf, int option) {
        try {
            Vote vote = new Vote(cpf, option);
            out.writeObject(vote);
            return (String) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao enviar voto";
        }
    }
    
    public Map<String, Object> generateReport() {
        try {
            out.writeObject("GENERATE_REPORT");
            return (Map<String, Object>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}