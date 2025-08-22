package com.example.hexgame.algorithms;

import com.example.hexgame.model.Tile;
import com.example.hexgame.utils.StrategyUtils;

import java.util.*;

public class DFSStrategy implements PathfindingStrategy {
    public PathfindingResult findPath(Tile[][] grid, Tile start, int rowNum, int colNum) {
        Deque<Tile> stack = new ArrayDeque<>();
        boolean[][] visited = new boolean[rowNum][colNum];
        Map<Tile, Tile> parent = new HashMap<>();
        List<Tile> visitedOrder = new ArrayList<>();

        stack.push(start);
        visited[start.i][start.j] = true;
        Tile end = null;

        while (!stack.isEmpty()) {
            Tile cur = stack.pop();
            visitedOrder.add(cur);

            if (StrategyUtils.isAtBorder(cur, rowNum, colNum)) {
                end = cur;
                break;
            }

            for (Tile neighbor : StrategyUtils.getNeighbors(cur, grid, rowNum, colNum)) {
                if (!neighbor.isBlocked && !visited[neighbor.i][neighbor.j]) {
                    visited[neighbor.i][neighbor.j] = true;
                    parent.put(neighbor, cur);
                    stack.push(neighbor);
                }
            }
        }

        List<Tile> path = StrategyUtils.reconstructPath(end, parent);
        return new PathfindingResult(visitedOrder, path);
    }
}