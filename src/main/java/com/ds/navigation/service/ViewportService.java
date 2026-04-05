package com.ds.navigation.service;

import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewportService {
    public List<Vertex> getVisibleVertices(
            Graph graph,
            Rectangle2D worldRect,
            int panelWidth,
            int panelHeight,
            double scale) {
        if (graph == null) {
            return List.of();
        }
        int cellPixel = scale >= 0.5 ? 0 : scale >= 0.15 ? 24 : 40;
        if (cellPixel == 0) {
            List<Vertex> result = new ArrayList<>();
            for (Vertex vertex : graph.getVertices()) {
                if (worldRect.contains(vertex.getX(), vertex.getY())) {
                    result.add(vertex);
                }
            }
            return result;
        }
        Map<String, Vertex> sampled = new LinkedHashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            if (!worldRect.contains(vertex.getX(), vertex.getY())) {
                continue;
            }
            int px = (int) ((vertex.getX() - worldRect.getMinX()) / worldRect.getWidth() * panelWidth);
            int py = (int) ((worldRect.getMaxY() - vertex.getY()) / worldRect.getHeight() * panelHeight);
            int cellX = Math.max(0, px / cellPixel);
            int cellY = Math.max(0, py / cellPixel);
            sampled.putIfAbsent(cellX + "-" + cellY, vertex);
        }
        return new ArrayList<>(sampled.values());
    }
}
