package com.ds.navigation.model;

import java.util.List;

public class PathResult {
    private final List<Integer> vertexIds;
    private final List<Integer> edgeIds;
    private final double totalDistance;
    private final double totalTime;

    public PathResult(List<Integer> vertexIds, List<Integer> edgeIds, double totalDistance, double totalTime) {
        this.vertexIds = List.copyOf(vertexIds);
        this.edgeIds = List.copyOf(edgeIds);
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public List<Integer> getVertexIds() {
        return vertexIds;
    }

    public List<Integer> getEdgeIds() {
        return edgeIds;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalTime() {
        return totalTime;
    }
}
