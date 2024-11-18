
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Iniciar o servidor em uma thread separada
        new Thread(() -> {
            VotingServer server = new VotingServer();
            server.start();
        }).start();

        // Iniciar a interface gráfica
        SwingUtilities.invokeLater(() -> {
            VotingApp app = new VotingApp();
            app.setVisible(true);
        });
    }
}