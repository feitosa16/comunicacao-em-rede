

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.List;

public class VotingApp extends JFrame {
    private VotingClient client;
    private JTextField cpfField;
    private JComboBox<String> optionsCombo;
    private JButton connectButton;
    private JButton voteButton;
    private JButton disconnectButton;
    private JButton resultsButton;
    private JButton reportButton;
    private JButton endVotingButton;

    public VotingApp() {
        client = new VotingClient();
        setupUI();
    }

    private void setupUI() {
        setTitle("Sistema de Votação");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 1, 10, 10));

        // CPF Field
        JPanel cpfPanel = new JPanel();
        cpfField = new JTextField(15);
        cpfPanel.add(new JLabel("CPF:"));
        cpfPanel.add(cpfField);
        add(cpfPanel);

        // Connect Button
        connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> connect());
        add(connectButton);

        // Options Combo
        optionsCombo = new JComboBox<>();
        optionsCombo.setEnabled(false);
        add(optionsCombo);

        // Vote Button
        voteButton = new JButton("Confirmar Voto");
        voteButton.setEnabled(false);
        voteButton.addActionListener(e -> vote());
        add(voteButton);

        // Results Button
        resultsButton = new JButton("Ver Resultados Parciais");
        resultsButton.addActionListener(e -> showResults());
        add(resultsButton);

        // Report Button
        reportButton = new JButton("Gerar Relatório");
        reportButton.addActionListener(e -> generateReport());
        add(reportButton);

        // Disconnect Button
        disconnectButton = new JButton("Desconectar");
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnect());
        add(disconnectButton);

        setLocationRelativeTo(null);
    }

    private void connect() {
        if (client.connect()) {
            JOptionPane.showMessageDialog(this, "Conectado com sucesso!");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            voteButton.setEnabled(true);
            
            optionsCombo.setEnabled(true);
            optionsCombo.removeAllItems();
            for (String option : client.getOptions()) {
                optionsCombo.addItem(option);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao conectar!");
        }
    }

    private void vote() {
        String cpf = cpfField.getText();
        if (cpf.length() != 11 || !cpf.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "CPF inválido!");
            return;
        }

        int selectedOption = optionsCombo.getSelectedIndex();
        String response = client.sendVote(cpf, selectedOption);
        JOptionPane.showMessageDialog(this, response);
    }

    private void disconnect() {
        client.disconnect();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        voteButton.setEnabled(false);
        optionsCombo.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Desconectado do servidor!");
    }

    private void showResults() {
    	if (client == null) {
            JOptionPane.showMessageDialog(this, "Conecte-se primeiro ao servidor!");
            return;
        }

        Map<Integer, Integer> results = client.getPartialResults();
        if (results == null) {
            JOptionPane.showMessageDialog(this, "Erro ao obter resultados!");
            return;
        }

        // Create string with results
        StringBuilder resultText = new StringBuilder();
        resultText.append("Resultados Parciais:\n\n");
        
        List<String> options = client.getOptions();
        int totalVotes = results.values().stream().mapToInt(Integer::intValue).sum();
        
        for (int i = 0; i < options.size(); i++) {
            int votes = results.getOrDefault(i, 0);
            double percentage = totalVotes > 0 ? (votes * 100.0) / totalVotes : 0;
            
            resultText.append(String.format("%s: %d votos (%.1f%%)\n", 
                                          options.get(i), 
                                          votes, 
                                          percentage));
        }
        
        resultText.append(String.format("\nTotal de votos: %d", totalVotes));

       
        JDialog resultsDialog = new JDialog(this, "Resultados Parciais", true);
        resultsDialog.setLayout(new BorderLayout(10, 10));
        
        // Jtextarea for better layout
        JTextArea textArea = new JTextArea(resultText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        resultsDialog.add(scrollPane, BorderLayout.CENTER);
        
        
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> resultsDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        resultsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultsDialog.setSize(400, 300);
        resultsDialog.setLocationRelativeTo(this);
        resultsDialog.setVisible(true);
    }

    private void generateReport() {
    	if (client == null) {
            JOptionPane.showMessageDialog(this, "Conecte-se primeiro ao servidor!");
            return;
        }

        Map<String, Object> reportData = client.generateReport();
        if (reportData == null) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar relatório!");
            return;
        }

        try {
            // Create file for report
            File reportFile = new File("relatorio_votacao.txt");
            try (PrintWriter writer = new PrintWriter(reportFile)) {
                writer.println("Relatório Completo da Votação");
                writer.println("============================");
                writer.println("\nQuestão: " + reportData.get("question"));
                writer.println("\nOpções de Votação:");
                
                List<String> options = (List<String>) reportData.get("options");
                Map<Integer, Integer> voteCount = (Map<Integer, Integer>) reportData.get("voteCount");
                
                int totalVotes = voteCount.values().stream().mapToInt(Integer::intValue).sum();
                
                for (int i = 0; i < options.size(); i++) {
                    int votes = voteCount.getOrDefault(i, 0);
                    double percentage = totalVotes > 0 ? (votes * 100.0) / totalVotes : 0;
                    
                    writer.printf("%s: %d votos (%.1f%%)%n", 
                                  options.get(i), votes, percentage);
                }

                writer.println("\nTotal de votos: " + totalVotes);
                
                String winner = (String) reportData.get("winner");
                writer.println("\nVencedor: " + winner);

                writer.println("\nLista de Votantes:");
                List<String> voterList = (List<String>) reportData.get("voterList");
                for (String cpf : voterList) {
                    writer.println(cpf);
                }
            }

            
            JOptionPane.showMessageDialog(this, 
                "Relatório gerado com sucesso!\nArquivo: " + reportFile.getAbsolutePath(),
                "Relatório Gerado", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao gerar relatório: " + e.getMessage(),
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    }
