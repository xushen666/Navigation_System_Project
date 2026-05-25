package com.ds.navigation.ui;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.PathResult;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.repository.GraphFileRepository;
import com.ds.navigation.service.MapGeneratorService;
import com.ds.navigation.service.PathFinderService;
import com.ds.navigation.service.SpatialIndexService;
import com.ds.navigation.service.TrafficSimulationService;
import com.ds.navigation.service.ViewportService;
import com.ds.navigation.util.AppConfig;
import com.ds.navigation.util.InputValidator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileSystemView;

public class MainFrame extends JFrame {
    private final AppConfig config;
    private final GraphFileRepository repository;
    private final MapGeneratorService generator;
    private final SpatialIndexService spatialIndexService;
    private final PathFinderService pathFinderService;
    private final TrafficSimulationService trafficSimulationService;
    private final ControlPanel controlPanel;
    private final MapPanel mapPanel;
    private final StatusBar statusBar;
    private final Timer simulationTimer;
    private Graph graph;
    private Integer selectedStartId;
    private Integer selectedEndId;

    public MainFrame(
            AppConfig config,
            Graph initialGraph,
            GraphFileRepository repository,
            MapGeneratorService generator,
            SpatialIndexService spatialIndexService,
            PathFinderService pathFinderService,
            TrafficSimulationService trafficSimulationService,
            ViewportService viewportService) {
        super("导航系统课程设计");
        this.config = config;
        this.graph = initialGraph;
        this.repository = repository;
        this.generator = generator;
        this.spatialIndexService = spatialIndexService;
        this.pathFinderService = pathFinderService;
        this.trafficSimulationService = trafficSimulationService;
        this.controlPanel = new ControlPanel();
        this.mapPanel = new MapPanel(viewportService);
        this.statusBar = new StatusBar();
        this.simulationTimer = new Timer(config.getSimulationIntervalMillis(), event -> onSimulationTick());

        initUi();
        bindActions();
        setGraph(initialGraph, "地图已加载");
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1280, 820));
        setSize(1280, 820);
        getContentPane().setBackground(ThemeConstants.BG_LIGHT_GRAY);

        add(buildNavigationBar(), BorderLayout.NORTH);
        add(controlPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private void bindActions() {
        controlPanel.getGenerateButton().addActionListener(event -> generateAndSaveDefaultMap());
        controlPanel.getNearbyButton().addActionListener(event -> showNearbyVertices());
        controlPanel.getDistancePathButton().addActionListener(event -> showDistancePath());
        controlPanel.getTimePathButton().addActionListener(event -> showTimePath());
        controlPanel.getStartSimulationButton().addActionListener(event -> startSimulation());
        controlPanel.getPauseSimulationButton().addActionListener(event -> pauseSimulation());
        controlPanel.getResetSimulationButton().addActionListener(event -> resetSimulation());
        controlPanel.getClearButton().addActionListener(event -> clearHighlights());
        controlPanel.getResetViewButton().addActionListener(event -> mapPanel.resetView());
        mapPanel.setMapClickListener(this::selectNearestVertex);
    }

    private NavigationBar buildNavigationBar() {
        NavigationBar nav = new NavigationBar();

        nav.addMenu("文件", List.of(
                new NavigationBar.PopupItem("生成地图", e -> generateAndSaveDefaultMap()),
                new NavigationBar.PopupItem("加载地图", e -> chooseAndLoadGraph()),
                new NavigationBar.PopupItem("保存地图", e -> chooseAndSaveGraph()),
                new NavigationBar.PopupItem("退出", e -> dispose())));

        nav.addMenu("查询", List.of(
                new NavigationBar.PopupItem("附近100点", e -> showNearbyVertices()),
                new NavigationBar.PopupItem("最短路径", e -> showDistancePath()),
                new NavigationBar.PopupItem("路况最优路径", e -> showTimePath())));

        nav.addMenu("模拟", List.of(
                new NavigationBar.PopupItem("开始", e -> startSimulation()),
                new NavigationBar.PopupItem("暂停", e -> pauseSimulation()),
                new NavigationBar.PopupItem("重置", e -> resetSimulation())));

        nav.addMenu("视图", List.of(
                new NavigationBar.PopupItem("重置视图", e -> mapPanel.resetView()),
                new NavigationBar.PopupItem("清空高亮", e -> clearHighlights())));

        return nav;
    }

    private void setGraph(Graph graph, String message) {
        this.graph = graph;
        selectedStartId = null;
        selectedEndId = null;
        controlPanel.setSelectedStart("未选择");
        controlPanel.setSelectedEnd("未选择");
        mapPanel.setSelectedVertices(null, null);
        mapPanel.setGraph(graph);
        mapPanel.setTrafficVehicles(List.of());
        spatialIndexService.buildIndex(graph);
        pathFinderService.setGraph(graph);
        trafficSimulationService.setGraph(graph);
        statusBar.setMessage(message + "，顶点数：" + graph.vertexCount() + "，边数：" + graph.edgeCount());
        controlPanel.setResultText("当前地图已就绪。\n请点击地图选择 A 点和 B 点，或输入坐标执行附近查询。");
    }

    private void generateAndSaveDefaultMap() {
        try {
            Graph newGraph = generator.generateMap(config.getMapVertexCount(), System.nanoTime());
            Path defaultDir = Path.of("data", "output", "latest");
            repository.saveGraph(newGraph, defaultDir);
            setGraph(newGraph, "地图生成成功，并已保存到 data/output/latest");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void chooseAndLoadGraph() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择地图目录");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path dir = chooser.getSelectedFile().toPath();
                setGraph(repository.loadGraph(dir), "地图加载成功");
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    private void chooseAndSaveGraph() {
        if (graph == null) {
            showError(new IllegalStateException("当前没有可保存的地图"));
            return;
        }
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择保存目录");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path dir = chooser.getSelectedFile().toPath().resolve("map");
                repository.saveGraph(graph, dir);
                statusBar.setMessage("地图已保存到：" + dir);
            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    private void showNearbyVertices() {
        try {
            double x = InputValidator.parseDouble(controlPanel.getXField().getText(), "X 坐标");
            double y = InputValidator.parseDouble(controlPanel.getYField().getText(), "Y 坐标");
            Rectangle2D bounds = graph.getBounds();
            if (!bounds.contains(x, y)) {
                throw new IllegalArgumentException("坐标超出地图范围，请输入有效地图坐标");
            }
            List<Vertex> vertices = spatialIndexService.findKNearest(x, y, 100);
            Set<Integer> vertexIds = new LinkedHashSet<>();
            Set<Integer> edgeIds = new LinkedHashSet<>();
            StringBuilder builder = new StringBuilder();
            builder.append("查询点附近 100 个地点：\n");
            for (int i = 0; i < vertices.size(); i++) {
                Vertex vertex = vertices.get(i);
                vertexIds.add(vertex.getId());
                for (Edge edge : graph.getAdjacentEdges(vertex.getId())) {
                    edgeIds.add(edge.getId());
                }
                if (i < 12) {
                    builder.append(String.format("%d. %s (%s) [%.1f, %.1f]%n",
                            i + 1,
                            vertex.getName(),
                            vertex.getType(),
                            vertex.getX(),
                            vertex.getY()));
                }
            }
            builder.append("共查询到 ").append(vertices.size()).append(" 个地点。");
            mapPanel.highlightVertices(vertexIds);
            mapPanel.highlightEdges(edgeIds);
            mapPanel.setDistancePathVertices(Set.of());
            mapPanel.setDistancePathEdges(Set.of());
            mapPanel.setTimePathVertices(Set.of());
            mapPanel.setTimePathEdges(Set.of());
            controlPanel.setResultText(builder.toString());
            statusBar.setMessage("已显示附近 100 个地点及其关联道路");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showDistancePath() {
        try {
            ensureSelectedEndpoints();
            PathResult result = pathFinderService.findShortestByDistance(selectedStartId, selectedEndId);
            mapPanel.setDistancePathVertices(result.getVertexIds());
            mapPanel.setDistancePathEdges(result.getEdgeIds());
            mapPanel.setTimePathVertices(Set.of());
            mapPanel.setTimePathEdges(Set.of());
            controlPanel.setResultText(String.format(
                    "距离最短路径%n顶点数：%d%n总距离：%.2f%n预计时间：%.2f%n路径：%s",
                    result.getVertexIds().size(),
                    result.getTotalDistance(),
                    result.getTotalTime(),
                    result.getVertexIds()));
            statusBar.setMessage("已计算距离最短路径");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showTimePath() {
        try {
            ensureSelectedEndpoints();
            mapPanel.setTrafficMode(true);
            PathResult result = pathFinderService.findShortestByTime(selectedStartId, selectedEndId);
            mapPanel.setTimePathVertices(result.getVertexIds());
            mapPanel.setTimePathEdges(result.getEdgeIds());
            mapPanel.setDistancePathVertices(Set.of());
            mapPanel.setDistancePathEdges(Set.of());
            controlPanel.setResultText(String.format(
                    "路况最优路径%n顶点数：%d%n总距离：%.2f%n预计时间：%.2f%n路径：%s",
                    result.getVertexIds().size(),
                    result.getTotalDistance(),
                    result.getTotalTime(),
                    result.getVertexIds()));
            statusBar.setMessage("已计算当前路况下的最优路径");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void startSimulation() {
        trafficSimulationService.start();
        mapPanel.setTrafficMode(true);
        mapPanel.setTrafficVehicles(trafficSimulationService.getVehicleRenderStates());
        simulationTimer.start();
        statusBar.setMessage("车流模拟已启动");
    }

    private void pauseSimulation() {
        trafficSimulationService.pause();
        simulationTimer.stop();
        statusBar.setMessage("车流模拟已暂停");
    }

    private void resetSimulation() {
        simulationTimer.stop();
        trafficSimulationService.reset();
        mapPanel.setTrafficMode(false);
        mapPanel.setTrafficVehicles(List.of());
        mapPanel.repaint();
        statusBar.setMessage("车流模拟已重置");
    }

    private void clearHighlights() {
        mapPanel.clearHighlights();
        controlPanel.setResultText("已清空当前高亮结果。");
        statusBar.setMessage("高亮已清空");
    }

    private void onSimulationTick() {
        trafficSimulationService.tick();
        mapPanel.setTrafficVehicles(trafficSimulationService.getVehicleRenderStates());
        mapPanel.repaint();
        statusBar.setMessage("模拟中，活跃车辆数：" + trafficSimulationService.getActiveVehicleCount());
    }

    private void selectNearestVertex(double worldX, double worldY) {
        try {
            Vertex nearest = spatialIndexService.findNearest(worldX, worldY);
            if (selectedStartId == null || selectedEndId != null) {
                selectedStartId = nearest.getId();
                selectedEndId = null;
                controlPanel.setSelectedStart(nearest.getName());
                controlPanel.setSelectedEnd("未选择");
                mapPanel.setDistancePathVertices(Set.of());
                mapPanel.setDistancePathEdges(Set.of());
                mapPanel.setTimePathVertices(Set.of());
                mapPanel.setTimePathEdges(Set.of());
            } else if (nearest.getId() != selectedStartId) {
                selectedEndId = nearest.getId();
                controlPanel.setSelectedEnd(nearest.getName());
            }
            mapPanel.setSelectedVertices(selectedStartId, selectedEndId);
            statusBar.setMessage("已选择地点：" + nearest.getName() + "（" + nearest.getType() + "）");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void ensureSelectedEndpoints() {
        if (selectedStartId == null || selectedEndId == null) {
            throw new IllegalStateException("请先在地图上依次选择 A 点和 B 点");
        }
    }

    private void showError(Exception ex) {
        statusBar.setMessage(ex.getMessage());
        JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
    }
}
