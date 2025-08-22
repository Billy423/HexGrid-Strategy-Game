package com.example.hexgame.algorithms;

import com.example.hexgame.model.Tile;

public interface PathfindingStrategy {

    PathfindingResult findPath(Tile[][] grid, Tile start, int rowNum, int colNum);

}
