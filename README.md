# Hex Cat Escape Challenge

![Game Screenshot](screenshot.png)

A Java-based strategy game where players trap an AI-controlled cat on a hex grid using pathfinding algorithms with real-time visualization.

## Key Features
- **3 Pathfinding Algorithms**: BFS, DFS, and A* with optimized heuristics
- **Real-time Visualization**: See algorithms explore nodes and find paths
- **Performance Optimized**: 75% faster pathfinding through caching
- **Smart Obstacle Placement**: Cluster-based generation with escape validation
- **Strategy Pattern**: Seamless switching between algorithms

## Algorithms & Data Structures
| Algorithm       | Time Complexity | Key Optimization          |
|-----------------|-----------------|---------------------------|
| BFS             | O(V+E)          | ArrayDeque implementation |
| DFS             | O(V+E)          | Iterative stack approach  |
| A*              | O(E log V)      | Manhattan heuristic       |
| Path Caching    | O(1) lookup     | Strategy-aware caching    |

## Performance Metrics
- **Path calculation**: 8ms → 2ms (75% improvement)
- **Neighbor access**: 0.4ms → 0.1ms (4x faster)
- **Obstacle generation**: 12ms → 4ms (3x faster)