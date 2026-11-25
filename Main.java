import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.Stack;

public class Main extends JFrame {
    private static final int SIZE = 3;
    private static final char EMPTY = ' ';
    private static final char HUMAN = 'X';
    private static final char AI = 'O';
    private static final String SAVE_FILE = "game_state.txt";

    private final char[][] board = new char[SIZE][SIZE];
    private final Stack<Move> history = new Stack<>();
    private final JButton[][] cells = new JButton[SIZE][SIZE];

    private int humanWins = 0;
    private int aiWins = 0;
    private int draws = 0;
    private boolean gameOver = false;
    private boolean aiThinking = false;
    private String difficulty = "Unbeatable";

    private JLabel statusLabel;
    private JLabel playerScoreLabel;
    private JLabel aiScoreLabel;
    private JLabel drawScoreLabel;
    private JComboBox<String> difficultyBox;
    private GlassPanel root;

    private final Color textColor = new Color(239, 244, 255);
    private final Color mutedColor = new Color(160, 174, 202);
    private final Color cyan = new Color(105, 226, 255);
    private final Color violet = new Color(190, 145, 255);

    public Main() {
        super("Tic-Tac-Toe using AI ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 820);
        setMinimumSize(new Dimension(780, 700));
        setLocationRelativeTo(null);
        setContentPane(buildInterface());
        newGame(false);
    }

    private JPanel buildInterface() {
        root = new GlassPanel();
        root.setLayout(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(24, 34, 22, 34));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 0, 8, 0));

        JLabel title = new NeonTitle("TIC TAC TOE");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel scorePanel = new JPanel(new GridLayout(1, 3, 12, 0));
        scorePanel.setOpaque(false);
        playerScoreLabel = scoreCard("YOUR SCORE", "X", "0", cyan);
        aiScoreLabel = scoreCard("AI SCORE", "O", "0", violet);
        drawScoreLabel = scoreCard("TOTAL DRAWS", "=", "0", mutedColor);
        scorePanel.add((JPanel) playerScoreLabel.getClientProperty("card"));
        scorePanel.add((JPanel) aiScoreLabel.getClientProperty("card"));
        scorePanel.add((JPanel) drawScoreLabel.getClientProperty("card"));
        scorePanel.setMaximumSize(new Dimension(620, 92));
        scorePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("YOUR TURN  •  CHOOSE A SQUARE", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(18, 0, 15, 0));

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        boardPanel.setOpaque(false);
        boardPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                final int row = r;
                final int col = c;
                GlassCell cell = new GlassCell();
                cell.setFont(new Font("SansSerif", Font.BOLD, 54));
                cell.setFocusPainted(false);
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                cell.addActionListener(e -> handleHumanMove(row, col));
                cells[r][c] = cell;
                boardPanel.add(cell);
            }
        }
        boardPanel.setPreferredSize(new Dimension(420, 420));
        boardPanel.setMaximumSize(new Dimension(420, 420));
        boardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        controls.setOpaque(false);
        JButton undoButton = actionButton("↶  UNDO", false);
        undoButton.addActionListener(e -> undoTurn());
        JButton newButton = actionButton("＋  NEW GAME", true);
        newButton.addActionListener(e -> newGame(true));
        JButton saveButton = actionButton("SAVE", false);
        saveButton.addActionListener(e -> saveGame());
        JButton loadButton = actionButton("LOAD", false);
        loadButton.addActionListener(e -> loadGame());
        controls.add(undoButton);
        controls.add(newButton);
        controls.add(saveButton);
        controls.add(loadButton);

        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        difficultyPanel.setOpaque(false);
        JLabel diffLabel = new JLabel("AI DIFFICULTY");
        diffLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        diffLabel.setForeground(mutedColor);
        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Unbeatable"});
        difficultyBox.setSelectedItem(difficulty);
        difficultyBox.setFont(new Font("SansSerif", Font.BOLD, 12));
        difficultyBox.setForeground(Color.WHITE);
        difficultyBox.setBackground(new Color(45, 37, 73));
        difficultyBox.setFocusable(false);
        difficultyBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 210, 255, 120), 1, true),
                new EmptyBorder(5, 9, 5, 9)));
        difficultyBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setForeground(Color.WHITE);
                label.setBackground(isSelected ? new Color(105, 75, 145) : new Color(45, 37, 73));
                label.setBorder(new EmptyBorder(6, 8, 6, 8));
                return label;
            }
        });
        difficultyBox.addActionListener(e -> difficulty = (String) difficultyBox.getSelectedItem());
        difficultyPanel.add(diffLabel);
        difficultyPanel.add(difficultyBox);

        center.add(Box.createVerticalGlue());
        center.add(scorePanel);
        center.add(statusLabel);
        center.add(boardPanel);
        center.add(Box.createVerticalStrut(18));
        center.add(controls);
        center.add(Box.createVerticalStrut(10));
        center.add(difficultyPanel);
        center.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("DSA CONCEPTS USED  •  2D ARRAY  •  STACK  •  MINIMAX  •  RECURSION  •  BACKTRACKING", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.BOLD, 10));
        footer.setForeground(new Color(255, 255, 255, 220));
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JLabel scoreCard(String heading, String symbol, String score, Color accent) {
        JPanel card = new GlassCard();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel symbolLabel = new JLabel(symbol);
        symbolLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        symbolLabel.setForeground(accent);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(heading);
        name.setFont(new Font("SansSerif", Font.BOLD, 9));
        name.setForeground(new Color(238, 241, 255));
        JLabel value = new JLabel(score);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setForeground(textColor);
        labels.add(name);
        labels.add(value);

        card.add(symbolLabel, BorderLayout.WEST);
        card.add(labels, BorderLayout.CENTER);
        JLabel result = value;
        result.putClientProperty("card", card);
        return result;
    }

    private JButton actionButton(String text, boolean primary) {
        JButton button = new GlassActionButton(text, primary);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(12, 17, 12, 17));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    private void handleHumanMove(int row, int col) {
        if (gameOver || aiThinking || board[row][col] != EMPTY) return;
        placeMove(row, col, HUMAN);
        if (finishIfNeeded()) return;

        aiThinking = true;
        statusLabel.setText("AI IS THINKING  •  CALCULATING MOVES...");
        setCellsEnabled(false);

        Timer timer = new Timer(420, e -> {
            ((Timer) e.getSource()).stop();
            int[] move = chooseAIMove();
            if (move != null) placeMove(move[0], move[1], AI);
            aiThinking = false;
            refreshBoard();
            if (!finishIfNeeded()) {
                statusLabel.setText("YOUR TURN  •  CHOOSE A SQUARE");
                setCellsEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void placeMove(int row, int col, char symbol) {
        board[row][col] = symbol;
        history.push(new Move(row, col, symbol));
        refreshBoard();
        saveSilently();
    }

    private void refreshBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                char value = board[r][c];
                cells[r][c].setText(value == EMPTY ? "" : String.valueOf(value));
                cells[r][c].setForeground(value == HUMAN ? cyan : violet);
                cells[r][c].setEnabled(value == EMPTY && !gameOver && !aiThinking);
            }
        }
        playerScoreLabel.setText(String.valueOf(humanWins));
        aiScoreLabel.setText(String.valueOf(aiWins));
        drawScoreLabel.setText(String.valueOf(draws));
        root.repaint();
    }

    private void setCellsEnabled(boolean enabled) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                cells[r][c].setEnabled(enabled && board[r][c] == EMPTY && !gameOver);
    }

    private boolean finishIfNeeded() {
        char winner = getWinner();
        if (winner == HUMAN) {
            humanWins++;
            gameOver = true;
            statusLabel.setText("YOU WIN  •  BEAUTIFULLY PLAYED!");
            refreshBoard();
            setCellsEnabled(false);
            JOptionPane.showMessageDialog(this, "You outplayed the AI. Nice move!", "Victory", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        if (winner == AI) {
            aiWins++;
            gameOver = true;
            statusLabel.setText("AI WINS  •  THE MACHINE GOT THIS ONE");
            refreshBoard();
            setCellsEnabled(false);
            JOptionPane.showMessageDialog(this, "The AI found the winning line. Try again!", "AI Wins", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        if (isBoardFull()) {
            draws++;
            gameOver = true;
            statusLabel.setText("IT'S A DRAW  •  WELL MATCHED");
            refreshBoard();
            setCellsEnabled(false);
            JOptionPane.showMessageDialog(this, "A perfectly balanced game.", "Draw", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    private int[] chooseAIMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        int moveCount = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == EMPTY) moveCount++;

        int targetDepth = "Easy".equals(difficulty) ? 1 : ("Medium".equals(difficulty) ? 3 : 9);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == EMPTY) {
                    board[r][c] = AI;
                    int score = minimax(0, false, targetDepth);
                    board[r][c] = EMPTY; // backtrack: restore simulated move
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{r, c};
                    }
                }
            }
        }
        if (bestMove == null && moveCount > 0) return new int[]{0, 0};
        return bestMove;
    }

    private int minimax(int depth, boolean maximizing, int maxDepth) {
        char winner = getWinner();
        if (winner == AI) return 10 - depth;
        if (winner == HUMAN) return depth - 10;
        if (isBoardFull() || depth >= maxDepth) return 0;

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == EMPTY) {
                        board[r][c] = AI;
                        best = Math.max(best, minimax(depth + 1, false, maxDepth));
                        board[r][c] = EMPTY; // backtracking
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == EMPTY) {
                        board[r][c] = HUMAN;
                        best = Math.min(best, minimax(depth + 1, true, maxDepth));
                        board[r][c] = EMPTY; // backtracking
                    }
                }
            }
            return best;
        }
    }

    private char getWinner() {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return board[i][0];
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return board[0][i];
        }
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return board[0][0];
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return board[0][2];
        return EMPTY;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == EMPTY) return false;
        return true;
    }

    private void undoTurn() {
        if (aiThinking) return;
        if (history.isEmpty()) {
            statusLabel.setText("NOTHING TO UNDO YET");
            return;
        }
        if (history.peek().symbol == AI) {
            Move aiMove = history.pop();
            board[aiMove.row][aiMove.col] = EMPTY;
        }
        if (!history.isEmpty() && history.peek().symbol == HUMAN) {
            Move humanMove = history.pop();
            board[humanMove.row][humanMove.col] = EMPTY;
        }
        gameOver = false;
        refreshBoard();
        statusLabel.setText("YOUR TURN  •  LAST TURN UNDONE");
        setCellsEnabled(true);
        saveSilently();
    }

    private void newGame(boolean ask) {
        if (ask && JOptionPane.showConfirmDialog(this, "Start a fresh game?", "New Game",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) board[r][c] = EMPTY;
        history.clear();
        gameOver = false;
        aiThinking = false;
        statusLabel.setText("YOUR TURN  •  CHOOSE A SQUARE");
        refreshBoard();
        setCellsEnabled(true);
        saveSilently();
    }

    private void saveGame() {
        try {
            writeSave();
            JOptionPane.showMessageDialog(this, "Game saved to " + SAVE_FILE, "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save game: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSilently() {
        try { writeSave(); } catch (IOException ignored) { }
    }

    private void writeSave() throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (int r = 0; r < SIZE; r++) out.println(new String(board[r]));
            out.println(history.size());
            for (Move move : history) out.println(move.row + " " + move.col + " " + move.symbol);
        }
    }

    private void loadGame() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No saved game found yet.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            char[][] loaded = new char[SIZE][SIZE];
            for (int r = 0; r < SIZE; r++) {
                String line = in.readLine();
                if (line == null || line.length() < SIZE) throw new IOException("Invalid save file.");
                for (int c = 0; c < SIZE; c++) {
                    char ch = line.charAt(c);
                    if (ch != EMPTY && ch != HUMAN && ch != AI) throw new IOException("Invalid board data.");
                    loaded[r][c] = ch;
                }
            }
            int count = Integer.parseInt(in.readLine());
            if (count < 0 || count > 9) throw new IOException("Invalid move history.");
            Stack<Move> loadedHistory = new Stack<>();
            for (int i = 0; i < count; i++) {
                String[] parts = in.readLine().trim().split("\\s+");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                char symbol = parts[2].charAt(0);
                if (row < 0 || row > 2 || col < 0 || col > 2 || (symbol != HUMAN && symbol != AI))
                    throw new IOException("Invalid move entry.");
                loadedHistory.push(new Move(row, col, symbol));
            }
            for (int r = 0; r < SIZE; r++) System.arraycopy(loaded[r], 0, board[r], 0, SIZE);
            history.clear();
            history.addAll(loadedHistory);
            gameOver = getWinner() != EMPTY || isBoardFull();
            aiThinking = false;
            refreshBoard();
            setCellsEnabled(!gameOver);
            statusLabel.setText(gameOver ? "GAME OVER  •  START A NEW GAME" : "GAME LOADED  •  YOUR TURN");
            JOptionPane.showMessageDialog(this, "Saved game loaded.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class Move {
        final int row, col;
        final char symbol;
        Move(int row, int col, char symbol) { this.row = row; this.col = col; this.symbol = symbol; }
    }

    private class GlassCell extends JButton {
        GlassCell() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setForeground(textColor);
            setToolTipText("Click to place your X");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            Color glass = getModel().isRollover()
                    ? new Color(255, 255, 255, 72)
                    : new Color(255, 255, 255, 34);
            if (!isEnabled()) glass = new Color(255, 255, 255, 25);

            g.setColor(new Color(20, 18, 42, 38));
            g.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 5, 28, 28));
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 58),
                    0, h, glass));
            g.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 5, 28, 28));
            g.setStroke(new BasicStroke(1.4f));
            g.setColor(new Color(255, 255, 255, 145));
            g.draw(new RoundRectangle2D.Float(0.8f, 0.8f, w - 2, h - 6, 28, 28));

            String value = getText();
            if (value != null && !value.isEmpty()) {
                g.setFont(getFont());
                FontMetrics fm = g.getFontMetrics();
                int x = (w - fm.stringWidth(value)) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent() - 2;
                g.setColor(getForeground());
                g.drawString(value, x, y);
            }
            g.dispose();
        }
    }

    private static class GlassCard extends JPanel {
        GlassCard() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 22));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            g.setColor(new Color(220, 235, 255, 55));
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 20, 20));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class NeonTitle extends JLabel {
        NeonTitle(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // Soft white neon halo, then crisp white lettering.
            g.setColor(new Color(255, 255, 255, 42));
            for (int i = 5; i >= 1; i--) {
                g.setColor(new Color(180, 220, 255, 10 + i * 5));
                g.drawString(getText(), x - i / 2, y);
                g.drawString(getText(), x + i / 2, y);
            }
            g.setColor(Color.WHITE);
            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

    private static class GlassActionButton extends JButton {
        private final boolean primary;

        GlassActionButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(10, 16, 10, 16));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            int alpha = primary ? 72 : 46;
            if (getModel().isPressed()) alpha = 105;
            else if (getModel().isRollover()) alpha = 88;

            g.setColor(new Color(255, 255, 255, alpha));
            g.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 2, 24, 24));
            g.setStroke(new BasicStroke(1.25f));
            g.setColor(new Color(255, 255, 255, primary ? 190 : 135));
            g.draw(new RoundRectangle2D.Float(0.7f, 0.7f, w - 2, h - 3, 24, 24));

            String label = getText();
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(label)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent() - 1;
            g.setColor(Color.WHITE);
            g.drawString(label, x, y);
            g.dispose();
        }
    }

    private static class GlassPanel extends JPanel {
        GlassPanel() {
            setOpaque(true);
            setBackground(new Color(22, 16, 38));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Rich, phone-wallpaper-inspired abstract gradient.
            g.setPaint(new GradientPaint(0, 0, new Color(18, 15, 42),
                    w, h, new Color(48, 18, 57)));
            g.fillRect(0, 0, w, h);

            // Large soft colour fields create depth behind the glass surfaces.
            g.setPaint(new GradientPaint(0, 0, new Color(255, 76, 145, 150),
                    w * 0.72f, h * 0.55f, new Color(255, 76, 145, 0)));
            g.fillOval(-w / 4, -h / 3, (int)(w * 0.95), (int)(h * 0.95));

            g.setPaint(new GradientPaint(w, 0, new Color(80, 225, 214, 125),
                    0, h, new Color(80, 225, 214, 0)));
            g.fillOval((int)(w * 0.48), -h / 3, (int)(w * 0.78), (int)(h * 0.92));

            g.setPaint(new GradientPaint(0, h, new Color(255, 156, 72, 145),
                    w, h * 0.35f, new Color(255, 156, 72, 0)));
            g.fillOval((int)(w * 0.20), (int)(h * 0.50), (int)(w * 0.95), (int)(h * 0.85));

            g.setPaint(new GradientPaint(w * 0.25f, h, new Color(137, 91, 255, 120),
                    w, 0, new Color(137, 91, 255, 0)));
            g.fillOval(-w / 5, (int)(h * 0.43), (int)(w * 0.85), (int)(h * 0.85));

            // Curved translucent ribbons, like abstract stock wallpaper artwork.
            g.setStroke(new BasicStroke(42f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 13));
            java.awt.geom.Path2D.Float ribbon1 = new java.awt.geom.Path2D.Float();
            ribbon1.moveTo(-80, h * 0.36);
            ribbon1.curveTo(w * 0.22, h * 0.02, w * 0.40, h * 0.75, w * 0.76, h * 0.34);
            ribbon1.curveTo(w * 0.92, h * 0.16, w * 1.04, h * 0.22, w + 80, h * 0.08);
            g.draw(ribbon1);

            g.setStroke(new BasicStroke(24f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 255, 255, 10));
            java.awt.geom.Path2D.Float ribbon2 = new java.awt.geom.Path2D.Float();
            ribbon2.moveTo(-50, h * 0.82);
            ribbon2.curveTo(w * 0.26, h * 0.60, w * 0.50, h * 1.02, w * 0.83, h * 0.70);
            ribbon2.curveTo(w * 0.94, h * 0.59, w * 1.02, h * 0.60, w + 40, h * 0.48);
            g.draw(ribbon2);

            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            Main game = new Main();
            game.setVisible(true);
        });
    }
}
