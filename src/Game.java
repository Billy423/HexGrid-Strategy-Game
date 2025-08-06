import javax.swing.*;
import java.awt.*;
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
    private PathState pathState;

    // UI components
    private JComboBox<String> algoSelector;
    private JToggleButton visualizeToggle;
    private JButton visualizeBtn;
    private JLabel statusLabel;
    private JButton resetBtn;

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
        pathState = new PathState(grid, ROWNUM, COLNUM);

        controlPanel.add(algoSelector);
        controlPanel.add(visualizeToggle);
        controlPanel.add(visualizeBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(statusLabel);
        add(controlPanel, BorderLayout.NORTH);

        // Game grid
        JPanel gridPanel = createMap();
        add(gridPanel, BorderLayout.CENTER);


        // Strategy selection handler
        algoSelector.addActionListener(e -> {
            String choice = (String) algoSelector.getSelectedItem();
            switch (choice) {
                case "DFS": pathState.setStrategy(new DFSStrategy()); break;
                case "A*": pathState.setStrategy(new AStarStrategy()); break;
                default: pathState.setStrategy(new BFSStrategy());
            }
            pathState.updatePath(cat);
        });

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
        int clusterCount = 3;
        int obstaclesPerCluster = 5;
        int padding = 2;

        // Generate cluster centers
        List<Point> centers = new ArrayList<>();
        for (int i = 0; i < clusterCount; i++) {
            int r, c;
            do {
                r = padding + rand.nextInt(ROWNUM - 2 * padding);
                c = padding + rand.nextInt(COLNUM - 2 * padding);
            } while (isNearCat(r, c) || isNearExistingCluster(r, c, centers));
            centers.add(new Point(r, c));
        }

        // Place obstacles around centers
        for (Point center : centers) {
            placeCluster(center.x, center.y, obstaclesPerCluster, rand);
        }

        // Place additional obstacles if needed
        int placed = countObstacles();
        while (placed < 15) {
            int r = rand.nextInt(ROWNUM), c = rand.nextInt(COLNUM);
            if (!grid[r][c].isBlocked && !isNearCat(r, c)) {
                grid[r][c].setBlocked(true);
                placed++;
            }
        }
    }

    private void placeCluster(int centerR, int centerC, int count, Random rand) {
        int placed = 0;
        int attempts = 0;

        while (placed < count && attempts < 50) {
            int r = centerR + rand.nextInt(5) - 2;
            int c = centerC + rand.nextInt(5) - 2;

            if (r >= 0 && r < ROWNUM && c >= 0 && c < COLNUM &&
                    !grid[r][c].isBlocked && !isNearCat(r, c)) {
                grid[r][c].setBlocked(true);
                placed++;
            }
            attempts++;
        }
    }

    private boolean isNearExistingCluster(int r, int c, List<Point> centers) {
        for (Point center : centers) {
            if (Math.abs(r - center.x) < 4 && Math.abs(c - center.y) < 4) {
                return true;
            }
        }
        return false;
    }

    private boolean isNearCat(int i, int j) {
        int catDist = Math.max(Math.abs(i - cat.i), Math.abs(j - cat.j));
        return catDist <= 2;
    }

    private int countObstacles() {
        int count = 0;
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile.isBlocked) count++;
            }
        }
        return count;
    }

    private void handleTileClick(Tile tile) {
        if (gameOver) return;

        resetGridColors();

        if (!tile.isBlocked && tile != cat) {
            tile.setBlocked(true);
            tile.stateChanged = true;
            moves++;
            pathState.updatePath(cat);
            moveCatOneStep();
            statusLabel.setText("Moves: " + moves);

            if (visualizeToggle.isSelected()) {
                visualizePathfinding();
            }
        }
    }

    private void moveCatOneStep() {
        List<Tile> path = pathState.getPath(cat);

        if (path.isEmpty()) {
            gameOver = true;
            showGameOverPopup(true);
            return;
        }

        // Move to next position in path
        Tile nextStep = path.get(1);

        // Update cat position
        cat.setCat(false);
        cat = nextStep;
        cat.setCat(true);

        if (isAtBorder(cat)) {
            gameOver = true;
            showGameOverPopup(false);
        }
    }

    private void showGameOverPopup(boolean isWin) {
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
        if (visualizationTimer != null && visualizationTimer.isRunning()) {
            visualizationTimer.stop();
        }

        resetGridColors();
        setControlsEnabled(false);

        PathfindingResult result = pathState.activeStrategy.findPath(grid, cat, ROWNUM, COLNUM);

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
        if (visualizationTimer != null && visualizationTimer.isRunning()) {
            visualizationTimer.stop();
        }
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                grid[i][j].setBlocked(false);
                grid[i][j].resetColor();
            }
        }

        cat.setCat(false);
        cat = grid[ROWNUM/2][COLNUM/2];
        cat.setCat(true);

        placeObstacles();
        pathState.updatePath(cat);
        gameOver = false;
        moves = 0;
        statusLabel.setText("Moves: 0");
        setControlsEnabled(true);
    }

    private boolean isAtBorder(Tile t) {
        return t.i == 0 || t.i == ROWNUM - 1 || t.j == 0 || t.j == COLNUM - 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.pathState.updatePath(game.cat);
        });
    }
}