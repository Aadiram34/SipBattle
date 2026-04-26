import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MenuScreen extends JPanel {

    private final BlockBattleDesign app;
    private BufferedImage[] canImages;

    private JTextField nameField;
    private JComboBox<String> difficultyBox;
    private JButton startBtn;
    private JButton quitBtn;

    public MenuScreen(BlockBattleDesign app) {
        this.app = app;
        this.canImages = CanLoader.loadAll();

        setLayout(null);
        setBackground(new Color(30, 18, 8));

        buildControls();
    }

    private void buildControls() {
        nameField = new JTextField("Player 1");
        nameField.setFont(new Font("Monospaced", Font.BOLD, 18));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        add(nameField);

        difficultyBox = new JComboBox<>(new String[]{"Easy", "Normal", "Hard"});
        difficultyBox.setSelectedItem("Normal");
        difficultyBox.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(difficultyBox);

        startBtn = makeStyledButton("START GAME", new Color(230, 140, 0), 18);
        startBtn.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Player 1";

            String difficulty = (String) difficultyBox.getSelectedItem();
            app.showGame(playerName, difficulty);
        });
        add(startBtn);

        quitBtn = makeStyledButton("QUIT", new Color(150, 40, 40), 18);
        quitBtn.addActionListener(e -> System.exit(0));
        add(quitBtn);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                positionControls();
            }
        });
    }

    private Rectangle getVendingRect() {
        int vw = Math.min(900, getWidth() - 80);
        int vh = Math.min(880, getHeight() - 40);

        int vx = getWidth() / 2 - vw / 2;
        int vy = getHeight() / 2 - vh / 2;

        return new Rectangle(vx, vy, vw, vh);
    }

    private void positionControls() {
        Rectangle v = getVendingRect();

        int centerX = v.x + v.width / 2;

        int inputW = 360;
        int inputH = 38;

        int nameY = v.y + v.height - 185;
        int diffY = v.y + v.height - 125;
        int buttonY = v.y + v.height - 65;

        nameField.setBounds(centerX - inputW / 2, nameY, inputW, inputH);
        difficultyBox.setBounds(centerX - inputW / 2, diffY, inputW, inputH);

        int buttonW = 190;
        int buttonH = 48;
        int gap = 22;

        startBtn.setBounds(centerX - buttonW - gap / 2, buttonY, buttonW, buttonH);
        quitBtn.setBounds(centerX + gap / 2, buttonY, buttonW, buttonH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        positionControls();

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        drawBackground(g2);

        Rectangle v = getVendingRect();
        drawVendingMachine(g2, v.x, v.y, v.width, v.height);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(
                0, 0, new Color(55, 35, 12),
                getWidth(), getHeight(), new Color(25, 15, 8)
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawVendingMachine(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + 10, y + 10, w, h, 35, 35);

        g2.setPaint(new GradientPaint(
                x, y, new Color(45, 95, 195),
                x + w, y + h, new Color(20, 45, 130)
        ));
        g2.fillRoundRect(x, y, w, h, 35, 35);

        drawLEDPanel(g2, x, y, w);
        drawCharacterShelves(g2, x, y, w, h);
        drawHowToPlay(g2, x, y, w, h);
        drawInputLabels(g2, x, y, w, h);
    }

    private void drawLEDPanel(Graphics2D g2, int x, int y, int w) {
        g2.setColor(new Color(5, 5, 20));
        g2.fillRoundRect(x + 30, y + 20, w - 60, 100, 16, 16);

        g2.setColor(new Color(80, 160, 255));
        g2.setFont(new Font("Arial Black", Font.BOLD, 38));
        drawCentered(g2, "SIP", x + w / 2, y + 63);

        g2.setColor(new Color(255, 185, 0));
        g2.setFont(new Font("Arial Black", Font.BOLD, 38));
        drawCentered(g2, "BATTLE", x + w / 2, y + 106);
    }

    private void drawCharacterShelves(Graphics2D g2, int x, int y, int w, int h) {
        int shelfX = x + 35;
        int shelfY = y + 140;
        int shelfW = w - 300;
        int shelfH = 430;

        g2.setColor(new Color(10, 20, 60));
        g2.fillRoundRect(shelfX, shelfY, shelfW, shelfH, 15, 15);

        int canW = 82;
        int canH = 110;

        int topY = shelfY + 35;
        int bottomY = shelfY + 235;

        int topGap = shelfW / 4;

        drawCanInSlot(g2, 0, shelfX + topGap, topY, canW, canH, "Blue Bull");
        drawCanInSlot(g2, 2, shelfX + topGap * 2, topY, canW, canH, "Latorade");
        drawCanInSlot(g2, 4, shelfX + topGap * 3, topY, canW, canH, "Kelvin");

        int bottomGap = shelfW / 3;

        drawCanInSlot(g2, 1, shelfX + bottomGap, bottomY, canW, canH, "Moonbucks");
        drawCanInSlot(g2, 3, shelfX + bottomGap * 2, bottomY, canW, canH, "PhD Pepper");
    }

    private void drawCanInSlot(Graphics2D g2, int index, int centerX, int y, int boxW, int boxH, String name) {
        int boxX = centerX - boxW / 2;

        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillRoundRect(boxX - 8, y - 8, boxW + 16, boxH + 34, 12, 12);

        if (canImages != null && index >= 0 && index < canImages.length && canImages[index] != null) {
            drawImageFit(g2, canImages[index], boxX + 4, y + 4, boxW - 8, boxH - 8);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        drawCentered(g2, name, centerX, y + boxH + 22);
    }

    private void drawHowToPlay(Graphics2D g2, int x, int y, int w, int h) {
        int rulesX = x + w - 250;
        int rulesY = y + 140;
        int rulesW = 215;
        int rulesH = 430;

        g2.setColor(new Color(35, 60, 35));
        g2.fillRoundRect(rulesX, rulesY, rulesW, rulesH, 15, 15);

        g2.setColor(new Color(20, 40, 20));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(rulesX, rulesY, rulesW, rulesH, 15, 15);

        g2.setColor(new Color(230, 245, 230));
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        drawCentered(g2, "HOW TO PLAY", rulesX + rulesW / 2, rulesY + 35);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String[] rules = {
                "1. Pick a drink.",
                "2. Start at B8.",
                "3. Computer starts I1.",
                "4. Move up to 2 spaces.",
                "5. Move horizontal,",
                "   vertical, or diagonal.",
                "6. Drinks auto attack",
                "   after moving.",
                "",
                "Blue Bull: vertical",
                "Moonbucks: horizontal",
                "Latorade: L-shape",
                "PhD Pepper: range 1",
                "Kelvin: diagonal",
                "",
                "Eliminate all 5",
                "enemy drinks to win!"
        };

        int textY = rulesY + 70;

        for (String line : rules) {
            g2.drawString(line, rulesX + 18, textY);
            textY += 20;
        }
    }

    private void drawInputLabels(Graphics2D g2, int x, int y, int w, int h) {
        int centerX = x + w / 2;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 15));

        drawCentered(g2, "PLAYER NAME", centerX, y + h - 190);
        drawCentered(g2, "DIFFICULTY", centerX, y + h - 130);
    }

    private void drawImageFit(Graphics2D g2, BufferedImage img, int boxX, int boxY, int boxW, int boxH) {
        double imgRatio = img.getWidth() / (double) img.getHeight();
        double boxRatio = boxW / (double) boxH;

        int drawW;
        int drawH;

        if (imgRatio > boxRatio) {
            drawW = boxW;
            drawH = (int) (boxW / imgRatio);
        } else {
            drawH = boxH;
            drawW = (int) (boxH * imgRatio);
        }

        int drawX = boxX + (boxW - drawW) / 2;
        int drawY = boxY + (boxH - drawH) / 2;

        g2.drawImage(img, drawX, drawY, drawW, drawH, null);
    }

    private JButton makeStyledButton(String text, Color color, int fontSize) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setPaint(new GradientPaint(
                        0, 0, color.brighter(),
                        0, getHeight(), color.darker()
                ));

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial Black", Font.BOLD, fontSize));

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        getText(),
                        getWidth() / 2 - fm.stringWidth(getText()) / 2,
                        getHeight() / 2 + fm.getAscent() / 2 - 3
                );
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}