package com.ds.navigation.model;

public class Edge {
    private final int id;
    private final int fromId;
    private final int toId;
    private final double length;
    private final int capacity;
    private int currentVehicles;

    public Edge(int id, int fromId, int toId, double length, int capacity) {
        this(id, fromId, toId, length, capacity, 0);
    }

    public Edge(int id, int fromId, int toId, double length, int capacity, int currentVehicles) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.length = length;
        this.capacity = capacity;
        this.currentVehicles = currentVehicles;
    }

    public int getId() {
        return id;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public double getLength() {
        return length;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentVehicles() {
        return currentVehicles;
    }

    public void setCurrentVehicles(int currentVehicles) {
        this.currentVehicles = Math.max(0, currentVehicles);
    }

    public void incrementVehicles() {
        currentVehicles++;
    }

    public void decrementVehicles() {
        if (currentVehicles > 0) {
            currentVehicles--;
        }
    }

    public int getOther(int vertexId) {
        if (vertexId == fromId) {
            return toId;
        }
        if (vertexId == toId) {
            return fromId;
        }
        throw new IllegalArgumentException("顶点不属于该边");
    }

    public double getOccupancyRatio() {
        if (capacity <= 0) {
            return 0.0;
        }
        return currentVehicles / (double) capacity;
    }

    public double getTravelTime(double alpha, double c) {
        double ratio = getOccupancyRatio();
        double factor = ratio <= alpha ? 1.0 : 1.0 + Math.exp(ratio - alpha);
        return c * length * factor;
    }
}
