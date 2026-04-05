package com.ds.navigation.service;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.util.GeometryUtils;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MapGeneratorService {
    private static final String[] TYPES = {"普通地点", "加油站", "餐馆", "停车场", "维修点"};

    private final int mapWidth;
    private final int mapHeight;
    private final int shortRoadCapacity;
    private final int mediumRoadCapacity;
    private final int longRoadCapacity;
    private final double extraEdgeProbability;

    public MapGeneratorService(
            int mapWidth,
            int mapHeight,
            int shortRoadCapacity,
            int mediumRoadCapacity,
            int longRoadCapacity,
            double extraEdgeProbability) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.shortRoadCapacity = shortRoadCapacity;
        this.mediumRoadCapacity = mediumRoadCapacity;
        this.longRoadCapacity = longRoadCapacity;
        this.extraEdgeProbability = extraEdgeProbability;
    }

    public Graph generateMap(int vertexCount, long seed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("顶点数量必须大于 0");
        }
        Graph graph = new Graph();
        Random random = new Random(seed);

        int rows = (int) Math.ceil(Math.sqrt(vertexCount));
        int cols = (int) Math.ceil(vertexCount / (double) rows);
        double cellWidth = mapWidth / (double) cols;
        double cellHeight = mapHeight / (double) rows;
        double jitterRatio = 0.22;
        int[][] gridIds = new int[rows][cols];

        int vertexId = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (vertexId >= vertexCount) {
                    gridIds[row][col] = -1;
                    continue;
                }
                double centerX = (col + 0.5) * cellWidth;
                double centerY = (row + 0.5) * cellHeight;
                double x = centerX + (random.nextDouble() * 2 - 1) * cellWidth * jitterRatio;
                double y = centerY + (random.nextDouble() * 2 - 1) * cellHeight * jitterRatio;
                Vertex vertex = new Vertex(
                        vertexId,
                        clamp(x, 0, mapWidth),
                        clamp(y, 0, mapHeight),
                        String.format("P%05d", vertexId),
                        TYPES[random.nextInt(TYPES.length)]);
                graph.addVertex(vertex);
                gridIds[row][col] = vertexId;
                vertexId++;
            }
        }

        int edgeId = 0;
        Set<String> edgeKeys = new HashSet<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int current = gridIds[row][col];
                if (current < 0) {
                    continue;
                }
                if (col + 1 < cols && gridIds[row][col + 1] >= 0) {
                    edgeId = addEdge(graph, edgeKeys, edgeId, current, gridIds[row][col + 1], random);
                }
                if (row + 1 < rows && gridIds[row + 1][col] >= 0) {
                    edgeId = addEdge(graph, edgeKeys, edgeId, current, gridIds[row + 1][col], random);
                }
                if (row + 1 < rows && col + 1 < cols
                        && gridIds[row + 1][col] >= 0
                        && gridIds[row][col + 1] >= 0
                        && gridIds[row + 1][col + 1] >= 0
                        && random.nextDouble() < extraEdgeProbability) {
                    if (random.nextBoolean()) {
                        edgeId = addEdge(graph, edgeKeys, edgeId, current, gridIds[row + 1][col + 1], random);
                    } else {
                        edgeId = addEdge(graph, edgeKeys, edgeId, gridIds[row][col + 1], gridIds[row + 1][col], random);
                    }
                }
            }
        }
        return graph;
    }

    private int addEdge(Graph graph, Set<String> edgeKeys, int edgeId, int fromId, int toId, Random random) {
        int a = Math.min(fromId, toId);
        int b = Math.max(fromId, toId);
        String key = a + "-" + b;
        if (!edgeKeys.add(key)) {
            return edgeId;
        }
        Vertex from = graph.getVertex(fromId);
        Vertex to = graph.getVertex(toId);
        double length = GeometryUtils.distance(from.getX(), from.getY(), to.getX(), to.getY());
        int capacity = resolveCapacityByLength(length, random);
        graph.addEdge(new Edge(edgeId, fromId, toId, length, capacity));
        return edgeId + 1;
    }

    private int resolveCapacityByLength(double length, Random random) {
        if (length < 110) {
            return shortRoadCapacity + random.nextInt(8);
        }
        if (length < 155) {
            return mediumRoadCapacity + random.nextInt(10);
        }
        return longRoadCapacity + random.nextInt(12);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
