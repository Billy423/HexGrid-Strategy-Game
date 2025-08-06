import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import java.util.List;

public class Game extends JFrame {
    final int ROWNUM = 11, COLNUM = 11;
    final int tileSize = 60;
    Tile[][] grid = new Tile[ROWNUM][COLNUM];
    Tile cat;
    boolean gameOver = false;
    int moves = 0;
    private Timer visualizationTimer;

    // UI components
    private final JComboBox<String> algoSelector;
    private final JToggleButton visualizeToggle;
    private final JButton visualizeBtn;
    private final JLabel statusLabel;
    private final JButton resetBtn;

    public Game() {
        setTitle("Hex Cat Escape Challenge");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Control panel
        JPanel controlPanel = new JPanel();
        algoSelector = new JComboBox<>(new String[]{"BFS", "DFS", "A*"});
        visualizeToggle = new JToggleButton("Auto Visualize");
        visualizeBtn = new JButton("Visualize Now");
        statusLabel = new JLabel("Moves: 0");
        resetBtn = new JButton("Reset Game");

        controlPanel.add(algoSelector);
        controlPanel.add(visualizeToggle);
        controlPanel.add(visualizeBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(statusLabel);
        add(controlPanel, BorderLayout.NORTH);

        // Game grid
        JPanel gridPanel = createMap();
        add(gridPanel, BorderLayout.CENTER);

        // Event listeners
        visualizeToggle.addActionListener(e -> {
            if (visualizeToggle.isSelected() && !gameOver) {
                visualizePathfinding();
            }
        });

        visualizeBtn.addActionListener(e -> visualizePathfinding());
        resetBtn.addActionListener(e -> resetGame());

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createMap() {
        JPanel gridPanel = new JPanel(new GridLayout(ROWNUM, 1, 0, 0));
        for (int i = 0; i < ROWNUM; i++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            if (i % 2 != 0) rowPanel.add(Box.createHorizontalStrut(tileSize/2));
            for (int j = 0; j < COLNUM; j++) {
                Tile tile = new Tile(i, j);
                grid[i][j] = tile;
                tile.setPreferredSize(new Dimension(tileSize, tileSize));
                tile.addActionListener(ev -> handleTileClick(tile));
                rowPanel.add(tile);
            }
            gridPanel.add(rowPanel);
        }
        cat = grid[ROWNUM/2][COLNUM/2];
        cat.setCat(true);
        placeObstacles();
        return gridPanel;
    }

    private void placeObstacles() {
        Random rand = new Random();
        int count = 0;
        while (count < 15) {
            int r = rand.nextInt(ROWNUM), c = rand.nextInt(COLNUM);
            boolean nearCat = Math.abs(r - ROWNUM/2) <= 2 &&
                    Math.abs(c - COLNUM/2) <= 2;
            if (!grid[r][c].isBlocked && grid[r][c] != cat && !nearCat) {
                grid[r][c].setBlocked(true);
                count++;
            }
        }
    }

    private void handleTileClick(Tile tile) {
        if (gameOver) return;
        if (!tile.isBlocked && tile != cat) {
            tile.setBlocked(true);
            moves++;
            moveCatOneStep();
            statusLabel.setText("Moves: " + moves);

            // Auto-visualize if toggle is on
            if (visualizeToggle.isSelected()) {
                visualizePathfinding();
            }
        }
    }

    private void moveCatOneStep() {
        Queue<Tile> queue = new LinkedList<>();
        Map<Tile, Tile> parent = new HashMap<>();
        queue.add(cat);
        parent.put(cat, null);
        Tile exitTile = null;

        while (!queue.isEmpty()) {
            Tile current = queue.poll();
            if (isAtBorder(current)) {
                exitTile = current;
                break;
            }
            for (Tile neighbor : getNeighbors(current)) {
                if (!neighbor.isBlocked && !parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Game over conditions
        if (exitTile == null) {
            gameOver = true;
            showGameOverPopup(true); // Win
            return;
        }

        // Find first step toward escape
        Tile nextStep = exitTile;
        while (parent.get(nextStep) != cat) {
            nextStep = parent.get(nextStep);
        }

        // Move cat
        cat.setCat(false);
        cat = nextStep;
        cat.setCat(true);

        if (isAtBorder(cat)) {
            gameOver = true;
            showGameOverPopup(false); // Lose
        }
    }

    private void showGameOverPopup(boolean isWin) {
        // Stop any ongoing visualization
        if (visualizationTimer != null && visualizationTimer.isRunning()) {
            visualizationTimer.stop();
        }

        String message = isWin ?
                "You win! Cat trapped in " + moves + " moves" :
                "Cat escaped! You lose";

        int option = JOptionPane.showOptionDialog(
                this,
                message + "\nPlay again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Play Again", "Quit"},
                "Play Again"
        );

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void visualizePathfinding() {
        // Stop any previous visualization
        if (visualizationTimer != null && visualizationTimer.isRunning()) {
            visualizationTimer.stop();
        }

        resetGridColors();
        setControlsEnabled(false);

        String choice = (String) algoSelector.getSelectedItem();
        PathfindingStrategy strat;
        switch(choice) {
            case "DFS": strat = new DFSStrategy(); break;
            case "A*":  strat = new AStarStrategy(); break;
            default:    strat = new BFSStrategy();
        }

        PathfindingResult result = strat.findPath(grid, cat, ROWNUM, COLNUM);

        Iterator<Tile> visitIt = result.visitedOrder.iterator();
        Iterator<Tile> pathIt = result.path.iterator();

        visualizationTimer = new Timer(100, e -> {
            if (visitIt.hasNext()) {
                visitIt.next().highlightExplored();
            } else if (pathIt.hasNext()) {
                pathIt.next().highlightPath();
            } else {
                visualizationTimer.stop();
                setControlsEnabled(true);
                if (!gameOver) {
                    statusLabel.setText("Moves: " + moves);
                }
            }
            statusLabel.setText(String.format("Nodes: %d | Path: %d | Moves: %d",
                    result.getNodesExplored(), result.getPathLength(), moves));
        });
        visualizationTimer.start();
    }

    private void resetGridColors() {
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                grid[i][j].resetColor();
            }
        }
    }

    private void setControlsEnabled(boolean enabled) {
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                grid[i][j].setEnabled(enabled && !gameOver);
            }
        }
        algoSelector.setEnabled(enabled);
        visualizeToggle.setEnabled(enabled);
        visualizeBtn.setEnabled(enabled);
        resetBtn.setEnabled(enabled);
    }

    private void resetGame() {
        // Stop any running timers
        if (visualizationTimer != null && visualizationTimer.isRunning()) {
            visualizationTimer.stop();
        }

        // Reset board
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                grid[i][j].setBlocked(false);
                grid[i][j].resetColor();
            }
        }

        // Reset cat
        cat.setCat(false);
        cat = grid[ROWNUM/2][COLNUM/2];
        cat.setCat(true);

        // Reset game state
        placeObstacles();
        gameOver = false;
        moves = 0;
        statusLabel.setText("Moves: 0");
        setControlsEnabled(true);
    }

    private List<Tile> getNeighbors(Tile t) {
        List<Tile> neighbors = new ArrayList<>();
        int[][] dirs = getDirections(t.i);
        for (int[] d : dirs) {
            int ni = t.i + d[0], nj = t.j + d[1];
            if (ni >= 0 && ni < ROWNUM && nj >= 0 && nj < COLNUM) {
                neighbors.add(grid[ni][nj]);
            }
        }
        return neighbors;
    }

    private boolean isAtBorder(Tile t) {
        return t.i == 0 || t.i == ROWNUM - 1 || t.j == 0 || t.j == COLNUM - 1;
    }

    private int[][] getDirections(int row) {
        return (row % 2 == 0) ?
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1}} :
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,1},{1,1}};
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}