import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.Timer;

public class Game extends JFrame {
    final int ROWNUM = 11, COLNUM = 11;
    final int tileSize = 60;
    Tile[][] grid = new Tile[ROWNUM][COLNUM];
    Tile cat;
    boolean gameOver = false;
    int moves = 0;

    // UI components
    private final JComboBox<String> algoSelector;
    private final JButton visualizeBtn;
    private final JLabel statusLabel;

    public Game() {
        setTitle("Catch the Crazy Cat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Control panel
        JPanel controlPanel = new JPanel();
        algoSelector = new JComboBox<>(new String[]{"BFS", "DFS", "A*"});
        visualizeBtn = new JButton("Visualize Pathfinding");
        statusLabel = new JLabel("Moves: 0");

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetGame());

        controlPanel.add(algoSelector);
        controlPanel.add(visualizeBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(statusLabel);
        add(controlPanel, BorderLayout.NORTH);

        // Game grid
        JPanel gridPanel = createMap();
        add(gridPanel, BorderLayout.CENTER);

        // Event listeners
        visualizeBtn.addActionListener(e -> visualizePathfinding());

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
            if (StrategyUtils.isAtBorder(current, ROWNUM, COLNUM)) {
                exitTile = current;
                break;
            }
            for (Tile neighbor : StrategyUtils.getNeighbors(current, grid, ROWNUM, COLNUM)) {
                if (!neighbor.isBlocked && !parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (exitTile == null) {
            gameOver = true;
            statusLabel.setText("You win! Cat trapped in " + moves + " moves");
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

        if (StrategyUtils.isAtBorder(cat, ROWNUM, COLNUM)) {
            gameOver = true;
            statusLabel.setText("Cat escaped! You lose");
        }
    }

    private void visualizePathfinding() {
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                grid[i][j].resetColor();
            }
        }

        String choice = (String) algoSelector.getSelectedItem();
        PathfindingStrategy strat = switch (choice) {
            case "DFS" -> new DFSStrategy();
            case "A*" -> new AStarStrategy();
            default -> new BFSStrategy();
        };

        PathfindingResult result = strat.findPath(grid, cat, ROWNUM, COLNUM);
        visualizeBtn.setEnabled(false);

        Iterator<Tile> visitIt = result.visitedOrder.iterator();
        Iterator<Tile> pathIt = result.path.iterator();

        Timer timer = new Timer(100, null);
        timer.addActionListener(ev -> {
            if (visitIt.hasNext()) {
                visitIt.next().highlightExplored();
            } else if (pathIt.hasNext()) {
                pathIt.next().highlightPath();
            } else {
                timer.stop();
                visualizeBtn.setEnabled(true);
                statusLabel.setText("Moves: " + moves);
            }
            statusLabel.setText(String.format("Nodes: %d | Path: %d",
                    result.getNodesExplored(), result.getPathLength()));
        });
        timer.start();
    }

    private void resetGame() {
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
        gameOver = false;
        moves = 0;
        statusLabel.setText("Moves: 0");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::new);
    }
}