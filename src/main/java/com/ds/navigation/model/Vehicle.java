package com.ds.navigation.model;

import java.util.List;

public class Vehicle {
    private final int id;
    private final List<Integer> pathVertexIds;
    private final List<Integer> pathEdgeIds;
    private int currentEdgeIndex;
    private double remainingTime;
    private double currentEdgeDuration;
    private boolean active;

    public Vehicle(int id, List<Integer> pathVertexIds, List<Integer> pathEdgeIds, double remainingTime) {
        this.id = id;
        this.pathVertexIds = List.copyOf(pathVertexIds);
        this.pathEdgeIds = List.copyOf(pathEdgeIds);
        this.currentEdgeIndex = 0;
        this.remainingTime = remainingTime;
        this.currentEdgeDuration = remainingTime;
        this.active = !pathEdgeIds.isEmpty();
    }

    public int getId() {
        return id;
    }

    public List<Integer> getPathVertexIds() {
        return pathVertexIds;
    }

    public List<Integer> getPathEdgeIds() {
        return pathEdgeIds;
    }

    public int getCurrentEdgeIndex() {
        return currentEdgeIndex;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public double getCurrentEdgeDuration() {
        return currentEdgeDuration;
    }

    public void setCurrentEdgeDuration(double currentEdgeDuration) {
        this.currentEdgeDuration = currentEdgeDuration;
    }

    public void advanceEdge() {
        currentEdgeIndex++;
    }

    public boolean isFinished() {
        return currentEdgeIndex >= pathEdgeIds.size();
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
