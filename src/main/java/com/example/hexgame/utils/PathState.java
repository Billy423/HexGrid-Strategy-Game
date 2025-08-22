package com.example.hexgame.utils;

import com.example.hexgame.algorithms.BFSStrategy;
import com.example.hexgame.algorithms.PathfindingStrategy;
import com.example.hexgame.model.Tile;

import java.util.*;

public class PathState {
    private final Tile[][] grid;
    private final int ROWNUM, COLNUM;
    private final Map<String, Map<Tile, List<Tile>>> pathCache = new HashMap<>();
    private final Map<Tile, List<Tile>> neighborCache = new HashMap<>();
    public PathfindingStrategy activeStrategy;

    public PathState(Tile[][] grid, int rowNum, int colNum) {
        this.grid = grid;
        this.ROWNUM = rowNum;
        this.COLNUM = colNum;
        this.activeStrategy = new BFSStrategy();
        if (grid[0][0] != null) {
            precomputeNeighbors();
        }
    }

    public void setStrategy(PathfindingStrategy strategy) {
        this.activeStrategy = strategy;
        pathCache.clear();
    }

    private void precomputeNeighbors() {
        for (int i = 0; i < ROWNUM; i++) {
            for (int j = 0; j < COLNUM; j++) {
                Tile tile = grid[i][j];
                neighborCache.put(tile, computeNeighbors(tile));
            }
        }
    }

    private List<Tile> computeNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>(6);
        int parity = tile.i % 2;
        int[][] dirs = parity == 0 ?
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1}} :
                new int[][]{{-1,0},{1,0},{0,-1},{0,1},{-1,1},{1,1}};

        for (int[] d : dirs) {
            int ni = tile.i + d[0];
            int nj = tile.j + d[1];
            if (ni >= 0 && ni < ROWNUM && nj >= 0 && nj < COLNUM) {
                neighbors.add(grid[ni][nj]);
            }
        }
        return neighbors;
    }

    public List<Tile> getNeighbors(Tile tile) {
        if (tile == null) return new ArrayList<>();
        return neighborCache.getOrDefault(tile, new ArrayList<>());
    }

    public void updatePath(Tile start) {
        if (start == null) return;

        String strategyKey = activeStrategy.getClass().getSimpleName();
        if (!pathCache.containsKey(strategyKey)) {
            pathCache.put(strategyKey, new HashMap<>());
        }

        Map<Tile, List<Tile>> strategyCache = pathCache.get(strategyKey);

        if (!strategyCache.containsKey(start) || gridChanged(start)) {
            strategyCache.put(start, activeStrategy.findPath(grid, start, ROWNUM, COLNUM).path);
        }
    }

    private boolean gridChanged(Tile start) {
        for (Tile neighbor : getNeighbors(start)) {
            if (neighbor.stateChanged) return true;
        }
        return false;
    }

    public List<Tile> getPath(Tile start) {
        String strategyKey = activeStrategy.getClass().getSimpleName();
        if (pathCache.containsKey(strategyKey)) {
            return pathCache.get(strategyKey).getOrDefault(start, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}