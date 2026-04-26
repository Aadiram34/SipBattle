import javax.swing.*;
import java.awt.*;

public class BlockBattleDesign extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public BlockBattleDesign() {
        setTitle("Sip Battle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Full screen / maximized window
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new MenuScreen(this), "MENU");

        add(mainPanel);

        cardLayout.show(mainPanel, "MENU");
    }

    public void showGame(String playerName, String difficulty) {
        mainPanel.add(new GameScreen(this, playerName, difficulty), "GAME");
        cardLayout.show(mainPanel, "GAME");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}