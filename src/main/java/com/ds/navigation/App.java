package com.ds.navigation;

import com.ds.navigation.model.Graph;
import com.ds.navigation.repository.GraphFileRepository;
import com.ds.navigation.service.MapGeneratorService;
import com.ds.navigation.service.PathFinderService;
import com.ds.navigation.service.SpatialIndexService;
import com.ds.navigation.service.TrafficSimulationService;
import com.ds.navigation.service.ViewportService;
import com.ds.navigation.ui.MainFrame;
import com.ds.navigation.util.AppConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.load();
        GraphFileRepository repository = new GraphFileRepository();
        MapGeneratorService generator = new MapGeneratorService(
                config.getMapWidth(),
                config.getMapHeight(),
                config.getShortRoadCapacity(),
                config.getMediumRoadCapacity(),
                config.getLongRoadCapacity(),
                config.getExtraEdgeProbability());
        SpatialIndexService spatialIndexService = new SpatialIndexService();
        PathFinderService pathFinderService = new PathFinderService(config.getTrafficAlpha(), config.getTrafficC());
        TrafficSimulationService trafficSimulationService = new TrafficSimulationService(
                pathFinderService,
                config.getTrafficAlpha(),
                config.getTrafficC(),
                config.getTrafficSpawnPerTick(),
                config.getTrafficMaxVehicles());
        ViewportService viewportService = new ViewportService();

        Path latestDir = Path.of("data", "output", "latest");
        Graph graph;
        try {
            if (Files.exists(latestDir.resolve("vertices.csv")) && Files.exists(latestDir.resolve("edges.csv"))) {
                graph = repository.loadGraph(latestDir);
            } else {
                graph = generator.generateMap(config.getMapVertexCount(), config.getMapSeed());
                repository.saveGraph(graph, latestDir);
            }
        } catch (Exception ex) {
            graph = generator.generateMap(config.getMapVertexCount(), config.getMapSeed());
        }

        Graph finalGraph = graph;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(
                    config,
                    finalGraph,
                    repository,
                    generator,
                    spatialIndexService,
                    pathFinderService,
                    trafficSimulationService,
                    viewportService);
            frame.setVisible(true);
        });
    }
}
