import java.util.*;

public class StrategyUtils {

    public static List<Tile> getNeighbors(Tile tile, Tile[][] grid, int rowNum, int colNum) {
        List<Tile> neighbors = new ArrayList<>();
        int[][] dirs = getDirections(tile.i);
        for (int[] d : dirs) {
            int ni = tile.i + d[0], nj = tile.j + d[1];
            if (ni >= 0 && ni < rowNum && nj >= 0 && nj < colNum) {
                neighbors.add(grid[ni][nj]);
            }
        }
        return neighbors;
    }

    public static int[][] getDirections(int row) {
        return (row % 2 == 0) ?
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1}} :
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,1},{1,1}};
    }

    public static boolean isAtBorder(Tile t, int rowNum, int colNum) {
        return t.i == 0 || t.i == rowNum - 1 || t.j == 0 || t.j == colNum - 1;
    }

    public static List<Tile> reconstructPath(Tile end, Map<Tile, Tile> parent) {
        List<Tile> path = new ArrayList<>();
        Tile current = end;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}
