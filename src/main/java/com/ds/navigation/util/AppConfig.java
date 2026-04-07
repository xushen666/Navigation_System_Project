package com.ds.navigation.util;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final int mapWidth;
    private final int mapHeight;
    private final int mapVertexCount;
    private final long mapSeed;
    private final double extraEdgeProbability;
    private final int shortRoadCapacity;
    private final int mediumRoadCapacity;
    private final int longRoadCapacity;
    private final double trafficAlpha;
    private final double trafficC;
    private final int trafficSpawnPerTick;
    private final int trafficMaxVehicles;
    private final double defaultZoom;
    private final int simulationIntervalMillis;

    private AppConfig(Properties properties) {
        this.mapWidth = intValue(properties, "map.width", 10000);
        this.mapHeight = intValue(properties, "map.height", 10000);
        this.mapVertexCount = intValue(properties, "map.vertexCount", 10000);
        this.mapSeed = longValue(properties, "map.seed", 20260405L);
        this.extraEdgeProbability = doubleValue(properties, "map.extraEdgeProbability", 0.28);
        this.shortRoadCapacity = intValue(properties, "map.capacity.short", 45);
        this.mediumRoadCapacity = intValue(properties, "map.capacity.medium", 70);
        this.longRoadCapacity = intValue(properties, "map.capacity.long", 95);
        this.trafficAlpha = doubleValue(properties, "traffic.alpha", 0.8);
        this.trafficC = doubleValue(properties, "traffic.c", 0.05);
        this.trafficSpawnPerTick = intValue(properties, "traffic.spawnPerTick", 2);
        this.trafficMaxVehicles = intValue(properties, "traffic.maxVehicles", 180);
        this.defaultZoom = doubleValue(properties, "ui.defaultZoom", 1.0);
        this.simulationIntervalMillis = intValue(properties, "ui.simulationIntervalMillis", 200);
    }

    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception ignored) {
        }
        return new AppConfig(properties);
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapVertexCount() {
        return mapVertexCount;
    }

    public long getMapSeed() {
        return mapSeed;
    }

    public double getExtraEdgeProbability() {
        return extraEdgeProbability;
    }

    public int getShortRoadCapacity() {
        return shortRoadCapacity;
    }

    public int getMediumRoadCapacity() {
        return mediumRoadCapacity;
    }

    public int getLongRoadCapacity() {
        return longRoadCapacity;
    }

    public double getTrafficAlpha() {
        return trafficAlpha;
    }

    public double getTrafficC() {
        return trafficC;
    }

    public int getTrafficSpawnPerTick() {
        return trafficSpawnPerTick;
    }

    public int getTrafficMaxVehicles() {
        return trafficMaxVehicles;
    }

    public double getDefaultZoom() {
        return defaultZoom;
    }

    public int getSimulationIntervalMillis() {
        return simulationIntervalMillis;
    }

    private static int intValue(Properties properties, String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    private static long longValue(Properties properties, String key, long defaultValue) {
        return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    private static double doubleValue(Properties properties, String key, double defaultValue) {
        return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
    }
}
