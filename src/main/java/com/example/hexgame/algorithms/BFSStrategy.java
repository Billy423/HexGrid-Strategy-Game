import java.util.*;

public class BFSStrategy implements PathfindingStrategy {
    public PathfindingResult findPath(Tile[][] grid, Tile start, int rowNum, int colNum) {
        Queue<Tile> q = new LinkedList<>();
        boolean[][] visited = new boolean[rowNum][colNum];
        Map<Tile, Tile> parent = new HashMap<>();
        List<Tile> visitedOrder = new ArrayList<>();

        q.add(start);
        visited[start.i][start.j] = true;
        Tile end = null;

        while (!q.isEmpty()) {
            Tile cur = q.poll();
            visitedOrder.add(cur);

            if (StrategyUtils.isAtBorder(cur, rowNum, colNum)) {
                end = cur;
                break;
            }

            for (Tile neighbor : StrategyUtils.getNeighbors(cur, grid, rowNum, colNum)) {
                if (!neighbor.isBlocked && !visited[neighbor.i][neighbor.j]) {
                    visited[neighbor.i][neighbor.j] = true;
                    parent.put(neighbor, cur);
                    q.add(neighbor);
                }
            }
        }

        List<Tile> path = StrategyUtils.reconstructPath(end, parent);
        return new PathfindingResult(visitedOrder, path);
    }
}