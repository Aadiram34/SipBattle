import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameScreen extends JPanel {

    private final BlockBattleDesign app;
    private final int ROWS = 8, COLS = 10;
    private final CharacterPiece[][] board = new CharacterPiece[ROWS][COLS];

    private final Player human;
    private final Player computer = new Player("COMPUTER", false);
    private final String difficulty;
    private final BufferedImage[] canImages;

    private CharacterPiece selectedCharacter;
    private boolean playerTurn = true;
    private boolean computerTurnRunning = false;
    private boolean humanHasLeftStart = false;
    private boolean computerHasLeftStart = false;
    private String statusText;

    private String winnerBannerText = null;
    private int bannerX = -700;
    private Timer bannerTimer;

    private int boardX, boardY, cell;
    private int centerX;

    public GameScreen(BlockBattleDesign app, String playerName, String difficulty) {
        this.app = app;
        this.difficulty = difficulty;
        this.human = new Player(playerName, true);
        this.canImages = CanLoader.loadAll();
        this.statusText = "Choose one character from the left side.";

        setLayout(null);
        setBackground(new Color(40, 60, 30));

        setupCharacters();
        setupButtons();
        setupMouse();
    }

    private void updateLayoutSizes() {
        cell = Math.min(getWidth() / 20, getHeight() / 13);

        if (cell < 45) cell = 45;
        if (cell > 70) cell = 70;

        int boardW = COLS * cell;
        centerX = getWidth() / 2;

        boardX = centerX - boardW / 2;
        boardY = 165;
    }

    private void setupCharacters() {
        human.add(new CharacterPiece("Blue Bull", "🐂", "B", AttackType.VERTICAL));
        human.add(new CharacterPiece("Moonbucks", "☕", "M", AttackType.HORIZONTAL));
        human.add(new CharacterPiece("Latorade", "⚡", "L", AttackType.L_SHAPE));
        human.add(new CharacterPiece("PhD Pepper", "🥤", "P", AttackType.THREE_STEP));
        human.add(new CharacterPiece("Kelvin", "❄", "K", AttackType.DIAGONAL));

        computer.add(new CharacterPiece("Blue Bull", "🐂", "b", AttackType.VERTICAL));
        computer.add(new CharacterPiece("Moonbucks", "☕", "m", AttackType.HORIZONTAL));
        computer.add(new CharacterPiece("Latorade", "⚡", "l", AttackType.L_SHAPE));
        computer.add(new CharacterPiece("PhD Pepper", "🥤", "p", AttackType.THREE_STEP));
        computer.add(new CharacterPiece("Kelvin", "❄", "k", AttackType.DIAGONAL));
    }

    private void setupButtons() {
        JButton backBtn = makeStyledButton("← Back", new Color(100, 65, 30), 14);
        backBtn.addActionListener(e -> app.showMenu());
        add(backBtn);

        JButton rulesBtn = makeStyledButton("? Rules", new Color(100, 70, 35), 15);
        rulesBtn.addActionListener(e -> showRules());
        add(rulesBtn);
    }

    private void positionButtons() {
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;

                if (b.getText().contains("Back")) {
                    b.setBounds(20, 20, 100, 45);
                }

                if (b.getText().contains("Rules")) {
                    b.setBounds(getWidth() - 200, getHeight() - 75, 170, 50);
                }
            }
        }
    }

    private void setupMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!playerTurn || computerTurnRunning || winnerBannerText != null) return;

                updateLayoutSizes();

                int index = getHumanSlotIndex(e.getX(), e.getY());

                if (index >= 0) {
                    selectHumanCharacter(index);
                    return;
                }

                int col = (e.getX() - boardX) / cell;
                int row = (e.getY() - boardY) / cell;

                if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                    cellClicked(row, col);
                }
            }
        });
    }

    private int getHumanSlotIndex(int x, int y) {
        int sx = boardX - 190;
        int sy = boardY + 20;
        int sw = 150;
        int sh = 82;

        if (x < sx || x > sx + sw) return -1;

        for (int i = 0; i < 5; i++) {
            int top = sy + i * 95;

            if (y >= top && y <= top + sh) {
                return i;
            }
        }

        return -1;
    }

    private void selectHumanCharacter(int index) {
        if (human.active != null) {
            statusText = "Move your active character first.";
            repaint();
            return;
        }

        CharacterPiece ch = human.characters.get(index);

        if (ch.eliminated || ch.deployed) return;

        selectedCharacter = ch;
        statusText = "Selected " + ch.name + ". Click B8 to deploy.";
        repaint();
    }

    private void cellClicked(int row, int col) {
        if (!isPlayableSquare(row, col)) {
            statusText = "You can only play on red squares.";
            repaint();
            return;
        }

        if (human.active == null) {
            placeHumanCharacter(row, col);
            return;
        }

        if (board[row][col] == null && canMoveTo(human.active, row, col)) {
            move(human.active, row, col);
            humanHasLeftStart = true;

            autoKillIfInRange(human.active);
            repaint();

            if (!checkWinner()) {
                startComputerTurn();
            }
        } else {
            statusText = "Invalid move. Choose a highlighted square.";
            repaint();
        }
    }

    private void placeHumanCharacter(int row, int col) {
        if (selectedCharacter == null) {
            statusText = "Click a character on the left first.";
            repaint();
            return;
        }

        if (!isHumanStart(row, col)) {
            statusText = human.name + " must start at B8.";
            repaint();
            return;
        }

        if (board[row][col] != null) {
            statusText = "That square is already occupied.";
            repaint();
            return;
        }

        place(selectedCharacter, human, row, col);
        selectedCharacter = null;
        deployComputerIfNeeded();

        statusText = "Your turn. Move your character.";
        repaint();
    }

    private void startComputerTurn() {
        playerTurn = false;
        computerTurnRunning = true;
        statusText = "Computer thinking... Difficulty: " + difficulty;
        repaint();

        Timer timer = new Timer(700, e -> {
            ((Timer) e.getSource()).stop();
            computerTurn();
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void computerTurn() {
        if (checkWinner()) {
            computerTurnRunning = false;
            return;
        }

        deployComputerIfNeeded();

        if (computer.active == null) {
            endComputerTurn("Computer has no character. Your turn.");
            return;
        }

        CharacterPiece ai = computer.active;
        int[] bestMove = findBestComputerMove(ai);

        if (bestMove != null) {
            move(ai, bestMove[0], bestMove[1]);
            computerHasLeftStart = true;

            boolean killed = autoKillIfInRange(ai);

            endComputerTurn(killed ? "Computer moved and auto-killed your character!" : "Computer moved. Your turn.");
        } else {
            endComputerTurn("Computer has no valid move. Your turn.");
        }
    }

    private void endComputerTurn(String message) {
        computerTurnRunning = false;
        playerTurn = true;

        if (!checkWinner()) {
            statusText = message;
        }

        repaint();
    }

    private boolean isPlayableSquare(int row, int col) {
        return (row >= 1 && row <= 6 && col >= 2 && col <= 7)
                || isComputerStart(row, col)
                || isHumanStart(row, col);
    }

    private boolean isHumanStart(int row, int col) {
        return row == 7 && col == 1;
    }

    private boolean isComputerStart(int row, int col) {
        return row == 0 && col == 8;
    }

    private boolean isOpponentStart(CharacterPiece ch, int row, int col) {
        return (ch.owner == human && isComputerStart(row, col))
                || (ch.owner == computer && isHumanStart(row, col));
    }

    private void deployComputerIfNeeded() {
        if (computer.active != null) return;

        CharacterPiece ch = computer.nextAvailableDifferentFrom(human.active);

        if (ch == null) {
            ch = computer.nextAvailable();
        }

        if (ch != null && board[0][8] == null) {
            place(ch, computer, 0, 8);
        }
    }

    private boolean canMoveTo(CharacterPiece ch, int nR, int nC) {
        if (ch == null || nR < 0 || nR >= ROWS || nC < 0 || nC >= COLS) return false;

        if (!isPlayableSquare(nR, nC) || board[nR][nC] != null || isOpponentStart(ch, nR, nC)) {
            return false;
        }

        if (ch.owner == human && humanHasLeftStart && isHumanStart(nR, nC)) return false;
        if (ch.owner == computer && computerHasLeftStart && isComputerStart(nR, nC)) return false;

        int rd = Math.abs(nR - ch.row);
        int cd = Math.abs(nC - ch.col);

        if (rd == 0 && cd == 0) return false;

        return (rd == 0 && cd <= 2)
                || (cd == 0 && rd <= 2)
                || (rd == cd && rd <= 2);
    }

    private boolean autoKillIfInRange(CharacterPiece attacker) {
        Player enemy = attacker.owner == human ? computer : human;

        if (enemy.active != null && canAttack(attacker, enemy.active)) {
            String killedName = enemy.active.name;

            eliminate(enemy.active);

            statusText = attacker.name + " auto-killed " + killedName + "!";
            return true;
        }

        return false;
    }

    private boolean canAttack(CharacterPiece attacker, CharacterPiece target) {
        if (attacker == null || target == null) return false;

        int rowDiff = Math.abs(attacker.row - target.row);
        int colDiff = Math.abs(attacker.col - target.col);

        if (attacker.type == AttackType.VERTICAL) {
            return attacker.col == target.col && rowDiff > 0 && rowDiff <= 3;
        }

        if (attacker.type == AttackType.HORIZONTAL) {
            return attacker.row == target.row && colDiff > 0 && colDiff <= 3;
        }

        if (attacker.type == AttackType.L_SHAPE) {
            return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        }

        if (attacker.type == AttackType.THREE_STEP) {
            return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1);
        }

        if (attacker.type == AttackType.DIAGONAL) {
            return rowDiff == colDiff && rowDiff > 0 && rowDiff <= 2;
        }

        return false;
    }

    private void place(CharacterPiece ch, Player owner, int row, int col) {
        ch.owner = owner;
        ch.row = row;
        ch.col = col;
        ch.deployed = true;
        ch.eliminated = false;

        owner.active = ch;
        board[row][col] = ch;
    }

    private void move(CharacterPiece ch, int row, int col) {
        board[ch.row][ch.col] = null;

        ch.row = row;
        ch.col = col;

        board[row][col] = ch;
    }

    private void eliminate(CharacterPiece ch) {
        if (ch.row >= 0 && ch.col >= 0) {
            board[ch.row][ch.col] = null;
        }

        ch.eliminated = true;
        ch.deployed = false;

        if (ch.owner != null) {
            ch.owner.active = null;
        }

        ch.row = -1;
        ch.col = -1;
    }

    private double getSmartChance() {
        if ("Easy".equalsIgnoreCase(difficulty)) return 0.25;
        if ("Hard".equalsIgnoreCase(difficulty)) return 0.70;
        return 0.40;
    }

    private int[] findBestComputerMove(CharacterPiece ai) {
        if (human.active == null || Math.random() > getSmartChance()) {
            return findRandomComputerMove(ai);
        }

        int[] best = null;
        int[] risky = null;

        double bestScore = Double.MAX_VALUE;
        double riskyScore = Double.MAX_VALUE;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (canMoveTo(ai, r, c)) {
                    double score = Math.abs(r - human.active.row) + Math.abs(c - human.active.col);

                    if (wouldAttackFrom(ai, human.active, r, c)) {
                        score -= 10;
                    }

                    if (Math.abs(r - 3) + Math.abs(c - 4) <= 3) {
                        score -= 1;
                    }

                    if (!isDangerSquareForComputer(ai, r, c)) {
                        if (score < bestScore) {
                            bestScore = score;
                            best = new int[]{r, c};
                        }
                    } else {
                        if (score < riskyScore) {
                            riskyScore = score;
                            risky = new int[]{r, c};
                        }
                    }
                }
            }
        }

        return best != null ? best : (risky != null ? risky : findRandomComputerMove(ai));
    }

    private int[] findRandomComputerMove(CharacterPiece ai) {
        ArrayList<int[]> moves = new ArrayList<>();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (canMoveTo(ai, r, c)) {
                    moves.add(new int[]{r, c});
                }
            }
        }

        return moves.isEmpty() ? null : moves.get((int) (Math.random() * moves.size()));
    }

    private boolean wouldAttackFrom(CharacterPiece attacker, CharacterPiece target, int testRow, int testCol) {
        int oldRow = attacker.row;
        int oldCol = attacker.col;

        attacker.row = testRow;
        attacker.col = testCol;

        boolean result = canAttack(attacker, target);

        attacker.row = oldRow;
        attacker.col = oldCol;

        return result;
    }

    private boolean isDangerSquareForComputer(CharacterPiece computerPiece, int row, int col) {
        if (human.active == null) return false;

        int oldRow = computerPiece.row;
        int oldCol = computerPiece.col;

        computerPiece.row = row;
        computerPiece.col = col;

        boolean result = canAttack(human.active, computerPiece);

        computerPiece.row = oldRow;
        computerPiece.col = oldCol;

        return result;
    }

    private boolean checkWinner() {
        if (!human.hasCharactersLeft()) {
            startWinnerBanner("COMPUTER WINS!");
            statusText = "Game Over!";
            return true;
        }

        if (!computer.hasCharactersLeft()) {
            startWinnerBanner(human.name.toUpperCase() + " WINS!");
            statusText = "Game Over!";
            return true;
        }

        return false;
    }

    private void startWinnerBanner(String text) {
        if (winnerBannerText != null) return;

        winnerBannerText = text;
        bannerX = -700;

        if (bannerTimer != null) {
            bannerTimer.stop();
        }

        bannerTimer = new Timer(20, e -> {
            bannerX += 7;

            if (bannerX > getWidth()) {
                bannerTimer.stop();
                showPlayAgainDialog();
            }

            repaint();
        });

        bannerTimer.start();
    }

    private void showPlayAgainDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sip Battle", true);

        dialog.setSize(520, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(35, 20, 10));

        JLabel title = new JLabel("🎉 GAME OVER 🎉", SwingConstants.CENTER);
        title.setBounds(20, 20, 480, 50);
        title.setFont(new Font("Arial Black", Font.BOLD, 34));
        title.setForeground(new Color(255, 220, 90));
        panel.add(title);

        JLabel msg = new JLabel("Would you like to play again?", SwingConstants.CENTER);
        msg.setBounds(35, 95, 450, 55);
        msg.setFont(new Font("Arial Black", Font.BOLD, 24));
        msg.setForeground(new Color(255, 220, 90));
        panel.add(msg);

        JButton yesBtn = makeDialogButton("YES", new Color(40, 160, 70));
        yesBtn.setBounds(135, 205, 110, 42);
        yesBtn.addActionListener(e -> {
            dialog.dispose();
            app.showMenu();
        });
        panel.add(yesBtn);

        JButton noBtn = makeDialogButton("NO", new Color(180, 50, 40));
        noBtn.setBounds(275, 205, 110, 42);
        noBtn.addActionListener(e -> System.exit(0));
        panel.add(noBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JButton makeDialogButton(String text, Color color) {
        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial Black", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 220, 90), 2));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        updateLayoutSizes();
        positionButtons();

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        drawBackground(g2);
        drawTopBar(g2);
        drawWoodFrame(g2);
        drawBoard(g2);
        drawSidebars(g2);
        drawStatus(g2);
        drawWinnerBanner(g2);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(
                0, 0, new Color(55, 35, 12),
                getWidth(), getHeight(), new Color(30, 18, 8)
        ));

        g2.fillRect(0, 0, getWidth(), getHeight());

        int boardXSmall = boardX - 205;
        int boardYSmall = 80;

        g2.setColor(new Color(35, 60, 35));
        g2.fillRoundRect(boardXSmall, boardYSmall, 170, 90, 12, 12);

        g2.setColor(new Color(25, 45, 25));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boardXSmall, boardYSmall, 170, 90, 12, 12);

        g2.setColor(new Color(220, 235, 220));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));

        drawCentered(g2, "Click a drink", boardXSmall + 85, boardYSmall + 30);
        drawCentered(g2, "then B8", boardXSmall + 85, boardYSmall + 55);
        drawCentered(g2, "to deploy", boardXSmall + 85, boardYSmall + 78);
    }

    private void drawTopBar(Graphics2D g2) {
        int barW = Math.min(920, getWidth() - 420);
        int barH = 90;
        int barX = centerX - barW / 2;
        int barY = 10;

        g2.setColor(GameConstants.WOOD_DARK);
        g2.fillRoundRect(barX, barY, barW, barH, 14, 14);

        int centerPanelW = barW - 360;

        g2.setPaint(new GradientPaint(
                barX + 180, barY,
                GameConstants.GREEN_HEADER.brighter(),
                barX + 180, barY + barH,
                GameConstants.GREEN_HEADER.darker()
        ));

        g2.fillRoundRect(barX + 180, barY + 5, centerPanelW, 80, 12, 12);

        g2.setColor(GameConstants.P1_BLUE);
        g2.fillRoundRect(barX + 150, barY, 130, 90, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawCentered(g2, human.name, barX + 215, barY + 80);

        g2.setColor(new Color(80, 80, 80));
        g2.fillRoundRect(barX + barW - 280, barY, 130, 90, 10, 10);

        g2.setColor(Color.WHITE);
        drawCentered(g2, "Computer", barX + barW - 215, barY + 80);

        g2.setColor(new Color(255, 230, 150));
        g2.setFont(new Font("Serif", Font.BOLD, 30));
        drawCentered(g2, playerTurn ? human.name + "'s Turn" : "Computer's Turn", centerX, barY + 55);
    }

    private void drawWoodFrame(Graphics2D g2) {
        int fx = boardX - 35;
        int fy = boardY - 60;
        int fw = COLS * cell + 70;
        int fh = ROWS * cell + 90;

        g2.setPaint(new GradientPaint(
                fx, fy, GameConstants.WOOD_LIGHT,
                fx + fw, fy + fh, GameConstants.WOOD_DARK
        ));

        g2.fillRoundRect(fx, fy, fw, fh, 20, 20);

        g2.setColor(GameConstants.WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(fx, fy, fw, fh, 20, 20);

        g2.setColor(new Color(80, 45, 20));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));

        for (int c = 0; c < COLS; c++) {
            drawCentered(g2, String.valueOf((char) ('A' + c)), boardX + c * cell + cell / 2, boardY - 10);
        }

        for (int r = 0; r < ROWS; r++) {
            drawCentered(g2, String.valueOf(r + 1), boardX - 20, boardY + r * cell + cell / 2 + 5);
        }
    }

    private void drawBoard(Graphics2D g2) {
        g2.setColor(GameConstants.BOARD_TAN);
        g2.fillRect(boardX, boardY, COLS * cell, ROWS * cell);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = boardX + c * cell;
                int y = boardY + r * cell;

                if (isPlayableSquare(r, c)) {
                    g2.setColor(new Color(200, 65, 65, 150));
                    g2.fillRect(x, y, cell, cell);
                } else if ((r + c) % 2 == 0) {
                    g2.setColor(new Color(200, 165, 105, 60));
                    g2.fillRect(x, y, cell, cell);
                }

                if (human.active != null && playerTurn && canMoveTo(human.active, r, c)) {
                    g2.setColor(new Color(255, 230, 120, 155));
                    g2.fillRect(x + 2, y + 2, cell - 4, cell - 4);
                }

                g2.setColor(GameConstants.BOARD_LINE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRect(x, y, cell, cell);

                if (isHumanStart(r, c) || isComputerStart(r, c)) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    drawCentered(g2, isHumanStart(r, c) ? "B8 START" : "I1 START", x + cell / 2, y + 14);
                }
            }
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != null) {
                    drawPiece(g2, board[r][c], boardX + c * cell + 4, boardY + r * cell + 4, cell - 8);
                }
            }
        }
    }

    private void drawPiece(Graphics2D g2, CharacterPiece piece, int x, int y, int size) {
        int idx = indexOf(piece.name);

        if (piece.owner == computer) {
            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillRoundRect(x, y, size, size, 8, 8);
        }

        if (idx >= 0 && canImages[idx] != null) {
            drawImageFit(g2, canImages[idx], x + 4, y + 2, size - 8, size - 4);
        } else {
            g2.setColor(piece.owner == human ? GameConstants.P1_BLUE : GameConstants.P2_RED);
            g2.fillRoundRect(x, y, size, size, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
            drawCentered(g2, piece.emoji, x + size / 2, y + 35);
        }
    }

    private void drawSidebars(Graphics2D g2) {
        drawSidebar(g2, boardX - 220, boardY - 15, 170, human, true);
        drawSidebar(g2, boardX + COLS * cell + 50, boardY - 15, 170, computer, false);
    }

    private void drawSidebar(Graphics2D g2, int x, int y, int w, Player owner, boolean isHuman) {
        g2.setColor(isHuman ? GameConstants.P1_BLUE : GameConstants.P2_RED);
        g2.fillRoundRect(x + 5, y, w - 10, 25, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        drawCentered(g2, isHuman ? human.name : "Computer", x + w / 2, y + 18);

        for (int i = 0; i < 5; i++) {
            drawCharSlot(g2, x + 10, y + 35 + i * 95, w - 20, owner.characters.get(i), i, isHuman);
        }
    }

    private void drawCharSlot(Graphics2D g2, int x, int y, int w, CharacterPiece ch, int index, boolean isHuman) {
        int h = 82;
        boolean selected = ch == selectedCharacter;

        g2.setPaint(new GradientPaint(
                x, y, selected ? new Color(160, 100, 30) : new Color(100, 65, 25),
                x, y + h, new Color(70, 40, 15)
        ));

        g2.fillRoundRect(x, y, w, h, 10, 10);

        g2.setColor(selected ? GameConstants.GOLD : GameConstants.WOOD_DARK);
        g2.setStroke(new BasicStroke(selected ? 4 : 2));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        if (canImages[index] != null) {
            drawImageFit(g2, canImages[index], x + 8, y + 10, 42, 55);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(ch.name, x + 58, y + 22);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        String state = ch.eliminated
                ? "OUT"
                : (ch.deployed ? "ACTIVE" : (isHuman ? "READY" : "WAIT"));

        g2.drawString(state, x + 58, y + 42);
        g2.drawString(attackName(ch.type), x + 58, y + 60);
    }

    private void drawStatus(Graphics2D g2) {
        int statusW = COLS * cell + 70;
        int statusX = centerX - statusW / 2;
        int statusY = boardY + ROWS * cell + 45;

        g2.setColor(new Color(45, 30, 18, 220));
        g2.fillRoundRect(statusX, statusY, statusW, 55, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        drawCentered(g2, statusText, centerX, statusY + 35);
    }

    private void drawWinnerBanner(Graphics2D g2) {
        if (winnerBannerText == null) return;

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(bannerX, getHeight() / 2 - 50, 700, 95, 30, 30);

        g2.setColor(new Color(255, 215, 0));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(bannerX, getHeight() / 2 - 50, 700, 95, 30, 30);

        g2.setFont(new Font("Arial Black", Font.BOLD, 42));
        FontMetrics fm = g2.getFontMetrics();

        int tx = bannerX + 350 - fm.stringWidth(winnerBannerText) / 2;

        g2.setColor(new Color(80, 35, 0));
        g2.drawString(winnerBannerText, tx + 3, getHeight() / 2 + 13);

        g2.setColor(new Color(255, 230, 90));
        g2.drawString(winnerBannerText, tx, getHeight() / 2 + 10);
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

    private int indexOf(String name) {
        for (int i = 0; i < GameConstants.CHAR_NAMES.length; i++) {
            if (GameConstants.CHAR_NAMES[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    private String attackName(AttackType type) {
        if (type == AttackType.VERTICAL) return "Vertical 3";
        if (type == AttackType.HORIZONTAL) return "Horizontal 3";
        if (type == AttackType.L_SHAPE) return "L-shape";
        if (type == AttackType.THREE_STEP) return "Range 1";
        return "Diagonal 2";
    }

    private void showRules() {
        JOptionPane.showMessageDialog(
                this,
                "SIP BATTLE RULES\n\n" +
                        "• Choose one character from the left.\n" +
                        "• Human starts at B8. Computer starts at I1.\n" +
                        "• After a piece leaves its start, it cannot return.\n" +
                        "• Move up to 2 spaces horizontally, vertically, or diagonally.\n" +
                        "• After moving, your piece automatically kills an enemy if its attack reaches.\n\n" +
                        "Blue Bull: vertical range 3\n" +
                        "Moonbucks: horizontal range 3\n" +
                        "Latorade: L-shape, 2 then 1\n" +
                        "PhD Pepper: up/down/left/right range 1\n" +
                        "Kelvin: diagonal range 2\n\n" +
                        "Eliminate all 5 enemy pieces to win.",
                "Rules",
                JOptionPane.PLAIN_MESSAGE
        );
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

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        getText(),
                        getWidth() / 2 - fm.stringWidth(getText()) / 2,
                        getHeight() / 2 + fm.getAscent() / 2 - 2
                );
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
    }
}