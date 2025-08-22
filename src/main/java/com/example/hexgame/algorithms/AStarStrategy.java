import java.util.*;

public class AStarStrategy implements PathfindingStrategy {

    public PathfindingResult findPath(Tile[][] grid, Tile start, int rowNum, int colNum) {
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Tile, Integer> bestCost = new HashMap<>();
        Map<Tile, Tile> parent = new HashMap<>();
        List<Tile> visitedOrder = new ArrayList<>();

        bestCost.put(start, 0);
        open.add(new Node(start, 0, heuristic(start, rowNum, colNum)));

        Tile end = null;
        while (!open.isEmpty()) {
            Node curr = open.poll();

            if (bestCost.get(curr.tile) < curr.g) continue;

            visitedOrder.add(curr.tile);
            if (StrategyUtils.isAtBorder(curr.tile, rowNum, colNum)) {
                end = curr.tile;
                break;
            }

            for (Tile neighbor : StrategyUtils.getNeighbors(curr.tile, grid, rowNum, colNum)) {
                if (neighbor.isBlocked) continue;

                int newCost = curr.g + 1;
                if (newCost < bestCost.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    bestCost.put(neighbor, newCost);
                    int h = heuristic(neighbor, rowNum, colNum);
                    open.add(new Node(neighbor, newCost, h));
                    parent.put(neighbor, curr.tile);
                }
            }
        }

        List<Tile> path = StrategyUtils.reconstructPath(end, parent);
        return new PathfindingResult(visitedOrder, path);
    }

    private int heuristic(Tile t, int rowNum, int colNum) {
        int toTop = t.i;
        int toBottom = rowNum - 1 - t.i;
        int toLeft = t.j;
        int toRight = colNum - 1 - t.j;
        return Math.min(Math.min(toTop, toBottom), Math.min(toLeft, toRight));
    }

    private static class Node {
        Tile tile;
        int g, f;

        Node(Tile t, int g, int h) {
            tile = t;
            this.g = g;
            this.f = g + h;
        }
    }
}