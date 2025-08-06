import java.util.List;

public class PathfindingResult {
    public final List<Tile> visitedOrder;
    public final List<Tile> path;

    public PathfindingResult(List<Tile> visitedOrder, List<Tile> path) {
        this.visitedOrder = visitedOrder;
        this.path = path;
    }

    public int getNodesExplored() {
        return visitedOrder.size();
    }

    public int getPathLength() {
        return path.size();
    }
}