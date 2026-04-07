package com.ds.navigation.service;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.PathResult;
import com.ds.navigation.model.Vehicle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TrafficSimulationService {
    private final PathFinderService pathFinderService;
    private final double alpha;
    private final double c;
    private final int spawnPerTick;
    private final int maxVehicles;
    private final Random random = new Random();
    private final List<Vehicle> activeVehicles = new ArrayList<>();
    private Graph graph;
    private boolean running;
    private int nextVehicleId;

    public TrafficSimulationService(
            PathFinderService pathFinderService,
            double alpha,
            double c,
            int spawnPerTick,
            int maxVehicles) {
        this.pathFinderService = pathFinderService;
        this.alpha = alpha;
        this.c = c;
        this.spawnPerTick = spawnPerTick;
        this.maxVehicles = maxVehicles;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        reset();
    }

    public void start() {
        running = true;
    }

    public void pause() {
        running = false;
    }

    public void reset() {
        running = false;
        activeVehicles.clear();
        nextVehicleId = 0;
        if (graph != null) {
            graph.clearTraffic();
        }
    }

    public void tick() {
        if (!running || graph == null || graph.vertexCount() == 0) {
            return;
        }
        if (activeVehicles.size() < maxVehicles) {
            for (int i = 0; i < spawnPerTick; i++) {
                spawnVehicle();
            }
        }
        Iterator<Vehicle> iterator = activeVehicles.iterator();
        while (iterator.hasNext()) {
            Vehicle vehicle = iterator.next();
            vehicle.setRemainingTime(vehicle.getRemainingTime() - 1.0);
            while (vehicle.isActive() && vehicle.getRemainingTime() <= 0) {
                int finishedEdgeId = vehicle.getPathEdgeIds().get(vehicle.getCurrentEdgeIndex());
                Edge finishedEdge = graph.getEdge(finishedEdgeId);
                finishedEdge.decrementVehicles();
                vehicle.advanceEdge();
                if (vehicle.isFinished()) {
                    vehicle.deactivate();
                    break;
                }
                int nextEdgeId = vehicle.getPathEdgeIds().get(vehicle.getCurrentEdgeIndex());
                Edge nextEdge = graph.getEdge(nextEdgeId);
                nextEdge.incrementVehicles();
                double nextDuration = Math.max(1.0, nextEdge.getTravelTime(alpha, c));
                vehicle.setCurrentEdgeDuration(nextDuration);
                vehicle.setRemainingTime(vehicle.getRemainingTime() + nextDuration);
            }
            if (!vehicle.isActive()) {
                iterator.remove();
            }
        }
    }

    public int getActiveVehicleCount() {
        return activeVehicles.size();
    }

    public List<VehicleRenderState> getVehicleRenderStates() {
        List<VehicleRenderState> states = new ArrayList<>();
        for (Vehicle vehicle : activeVehicles) {
            if (!vehicle.isActive() || vehicle.isFinished()) {
                continue;
            }
            int index = vehicle.getCurrentEdgeIndex();
            if (index + 1 >= vehicle.getPathVertexIds().size()) {
                continue;
            }
            double duration = Math.max(1.0, vehicle.getCurrentEdgeDuration());
            double progress = 1.0 - vehicle.getRemainingTime() / duration;
            progress = Math.max(0.0, Math.min(1.0, progress));
            states.add(new VehicleRenderState(
                    vehicle.getPathVertexIds().get(index),
                    vehicle.getPathVertexIds().get(index + 1),
                    progress));
        }
        return states;
    }

    private void spawnVehicle() {
        if (graph.vertexCount() < 2) {
            return;
        }
        int startId = random.nextInt(graph.vertexCount());
        int endId = random.nextInt(graph.vertexCount());
        while (startId == endId) {
            endId = random.nextInt(graph.vertexCount());
        }
        try {
            PathResult path = pathFinderService.findShortestByDistance(startId, endId);
            if (path.getEdgeIds().isEmpty()) {
                return;
            }
            Edge firstEdge = graph.getEdge(path.getEdgeIds().get(0));
            double duration = Math.max(1.0, firstEdge.getTravelTime(alpha, c));
            firstEdge.incrementVehicles();
            Vehicle vehicle = new Vehicle(
                    nextVehicleId++,
                    path.getVertexIds(),
                    path.getEdgeIds(),
                    duration);
            vehicle.setCurrentEdgeDuration(duration);
            activeVehicles.add(vehicle);
        } catch (Exception ignored) {
        }
    }

    public record VehicleRenderState(int fromId, int toId, double progress) {
    }
}
