package com.ds.navigation.model;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private final int id;
    private final double x;
    private final double y;
    private final String name;
    private final String type;
    private final List<Integer> edgeIds = new ArrayList<>();

    public Vertex(int id, double x, double y, String name, String type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Integer> getEdgeIds() {
        return edgeIds;
    }

    public void addEdgeId(int edgeId) {
        edgeIds.add(edgeId);
    }
}
