package com.ds.navigation.repository;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GraphFileRepository {
    public void saveGraph(Graph graph, Path dir) {
        try {
            Files.createDirectories(dir);
            List<String> vertexLines = new ArrayList<>();
            vertexLines.add("id,x,y,name,type");
            graph.getVertices().stream()
                    .sorted(Comparator.comparingInt(Vertex::getId))
                    .forEach(vertex -> vertexLines.add(String.format(Locale.US, "%d,%.4f,%.4f,%s,%s",
                            vertex.getId(),
                            vertex.getX(),
                            vertex.getY(),
                            vertex.getName(),
                            vertex.getType())));
            Files.write(dir.resolve("vertices.csv"), vertexLines, StandardCharsets.UTF_8);

            List<String> edgeLines = new ArrayList<>();
            edgeLines.add("id,from,to,length,capacity,currentVehicles");
            graph.getEdges().stream()
                    .sorted(Comparator.comparingInt(Edge::getId))
                    .forEach(edge -> edgeLines.add(String.format(Locale.US, "%d,%d,%d,%.4f,%d,%d",
                            edge.getId(),
                            edge.getFromId(),
                            edge.getToId(),
                            edge.getLength(),
                            edge.getCapacity(),
                            edge.getCurrentVehicles())));
            Files.write(dir.resolve("edges.csv"), edgeLines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("保存地图失败: " + ex.getMessage(), ex);
        }
    }

    public Graph loadGraph(Path dir) {
        try {
            List<String> vertexLines = Files.readAllLines(dir.resolve("vertices.csv"), StandardCharsets.UTF_8);
            List<String> edgeLines = Files.readAllLines(dir.resolve("edges.csv"), StandardCharsets.UTF_8);
            Graph graph = new Graph();
            for (int i = 1; i < vertexLines.size(); i++) {
                String[] parts = vertexLines.get(i).split(",", -1);
                if (parts.length < 5) {
                    continue;
                }
                graph.addVertex(new Vertex(
                        Integer.parseInt(parts[0].trim()),
                        Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim()),
                        parts[3].trim(),
                        parts[4].trim()));
            }
            for (int i = 1; i < edgeLines.size(); i++) {
                String[] parts = edgeLines.get(i).split(",", -1);
                if (parts.length < 6) {
                    continue;
                }
                graph.addEdge(new Edge(
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim()),
                        Double.parseDouble(parts[3].trim()),
                        Integer.parseInt(parts[4].trim()),
                        Integer.parseInt(parts[5].trim())));
            }
            return graph;
        } catch (IOException ex) {
            throw new IllegalStateException("读取地图失败: " + ex.getMessage(), ex);
        }
    }
}
