package com.ds.navigation.service;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.PathResult;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.util.GeometryUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class PathFinderService {
    private Graph graph;
    private final double alpha;
    private final double c;

    public PathFinderService(double alpha, double c) {
        this.alpha = alpha;
        this.c = c;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public PathResult findShortestByDistance(int startId, int endId) {
        ensureGraph();
        return findPath(startId, endId, false);
    }

    public PathResult findShortestByTime(int startId, int endId) {
        ensureGraph();
        return findPath(startId, endId, true);
    }

    private PathResult findPath(int startId, int endId, boolean useTravelTime) {
        if (startId == endId) {
            return new PathResult(List.of(startId), List.of(), 0.0, 0.0);
        }
        Vertex start = graph.getVertex(startId);
        Vertex goal = graph.getVertex(endId);
        if (start == null || goal == null) {
            throw new IllegalArgumentException("起点或终点不存在");
        }

        PriorityQueue<SearchNode> openSet = new PriorityQueue<>();
        Map<Integer, Double> gScore = new HashMap<>();
        Map<Integer, Integer> previousVertex = new HashMap<>();
        Map<Integer, Integer> previousEdge = new HashMap<>();

        gScore.put(startId, 0.0);
        openSet.offer(new SearchNode(startId, heuristic(start, goal, useTravelTime)));

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            if (current.vertexId() == endId) {
                return reconstructPath(previousVertex, previousEdge, endId);
            }
            for (Edge edge : graph.getAdjacentEdges(current.vertexId())) {
                int neighborId = edge.getOther(current.vertexId());
                double weight = useTravelTime ? edge.getTravelTime(alpha, c) : edge.getLength();
                double tentative = gScore.get(current.vertexId()) + weight;
                if (tentative < gScore.getOrDefault(neighborId, Double.POSITIVE_INFINITY)) {
                    previousVertex.put(neighborId, current.vertexId());
                    previousEdge.put(neighborId, edge.getId());
                    gScore.put(neighborId, tentative);
                    double fScore = tentative + heuristic(graph.getVertex(neighborId), goal, useTravelTime);
                    openSet.offer(new SearchNode(neighborId, fScore));
                }
            }
        }
        throw new IllegalStateException("未找到可达路径");
    }

    private PathResult reconstructPath(Map<Integer, Integer> previousVertex, Map<Integer, Integer> previousEdge, int endId) {
        List<Integer> vertexIds = new ArrayList<>();
        List<Integer> edgeIds = new ArrayList<>();
        int current = endId;
        vertexIds.add(current);
        while (previousVertex.containsKey(current)) {
            edgeIds.add(previousEdge.get(current));
            current = previousVertex.get(current);
            vertexIds.add(current);
        }
        Collections.reverse(vertexIds);
        Collections.reverse(edgeIds);

        double totalDistance = 0.0;
        double totalTime = 0.0;
        for (int edgeId : edgeIds) {
            Edge edge = graph.getEdge(edgeId);
            totalDistance += edge.getLength();
            totalTime += edge.getTravelTime(alpha, c);
        }
        return new PathResult(vertexIds, edgeIds, totalDistance, totalTime);
    }

    private double heuristic(Vertex current, Vertex goal, boolean useTravelTime) {
        double distance = GeometryUtils.distance(current.getX(), current.getY(), goal.getX(), goal.getY());
        return useTravelTime ? distance * c : distance;
    }

    private void ensureGraph() {
        if (graph == null) {
            throw new IllegalStateException("图数据尚未加载");
        }
    }

    private record SearchNode(int vertexId, double fScore) implements Comparable<SearchNode> {
        @Override
        public int compareTo(SearchNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}
