public interface PathfindingStrategy {

    PathfindingResult findPath(Tile[][] grid, Tile start, int rowNum, int colNum);

}
