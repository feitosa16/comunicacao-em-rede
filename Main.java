
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Initiate server
        new Thread(() -> {
            VotingServer server = new VotingServer();
            server.start();
        }).start();

        // Initiate graphical interface
        SwingUtilities.invokeLater(() -> {
            VotingApp app = new VotingApp();
            app.setVisible(true);
        });
    }
}
