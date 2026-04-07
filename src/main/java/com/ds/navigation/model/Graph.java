package com.ds.navigation.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<Integer, Vertex> vertices = new LinkedHashMap<>();
    private final Map<Integer, Edge> edges = new LinkedHashMap<>();
    private final Map<Integer, List<Edge>> adjacency = new HashMap<>();

    public void addVertex(Vertex vertex) {
        vertices.put(vertex.getId(), vertex);
        adjacency.computeIfAbsent(vertex.getId(), ignored -> new ArrayList<>());
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getId(), edge);
        adjacency.computeIfAbsent(edge.getFromId(), ignored -> new ArrayList<>()).add(edge);
        adjacency.computeIfAbsent(edge.getToId(), ignored -> new ArrayList<>()).add(edge);
        Vertex from = vertices.get(edge.getFromId());
        Vertex to = vertices.get(edge.getToId());
        if (from != null) {
            from.addEdgeId(edge.getId());
        }
        if (to != null) {
            to.addEdgeId(edge.getId());
        }
    }

    public Vertex getVertex(int id) {
        return vertices.get(id);
    }

    public Edge getEdge(int id) {
        return edges.get(id);
    }

    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    public List<Edge> getAdjacentEdges(int vertexId) {
        return adjacency.getOrDefault(vertexId, List.of());
    }

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount() {
        return edges.size();
    }

    public void clearTraffic() {
        for (Edge edge : edges.values()) {
            edge.setCurrentVehicles(0);
        }
    }

    public Rectangle2D getBounds() {
        if (vertices.isEmpty()) {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Vertex vertex : vertices.values()) {
            minX = Math.min(minX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            maxX = Math.max(maxX, vertex.getX());
            maxY = Math.max(maxY, vertex.getY());
        }
        return new Rectangle2D.Double(minX, minY, Math.max(1.0, maxX - minX), Math.max(1.0, maxY - minY));
    }
}
