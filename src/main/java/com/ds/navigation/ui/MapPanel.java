package com.ds.navigation.ui;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.service.TrafficSimulationService.VehicleRenderState;
import com.ds.navigation.service.ViewportService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.JPanel;

public class MapPanel extends JPanel {
    public interface MapClickListener {
        void onMapClicked(double worldX, double worldY);
    }

    private static final Color EDGE_DEFAULT = new Color(205, 210, 220);
    private static final Color EDGE_QUERY = new Color(39, 100, 194);
    private static final Color EDGE_DISTANCE = new Color(30, 153, 73);
    private static final Color EDGE_TIME = new Color(190, 63, 41);
    private static final Color POINT_DEFAULT = new Color(70, 76, 87);
    private static final Color POINT_QUERY = new Color(39, 100, 194);
    private static final Color POINT_START = new Color(31, 152, 86);
    private static final Color POINT_END = new Color(210, 120, 10);

    private final ViewportService viewportService;
    private Graph graph;
    private final Set<Integer> queryVertexIds = new HashSet<>();
    private final Set<Integer> queryEdgeIds = new HashSet<>();
    private final Set<Integer> distancePathVertexIds = new HashSet<>();
    private final Set<Integer> timePathVertexIds = new HashSet<>();
    private final Set<Integer> distancePathEdgeIds = new HashSet<>();
    private final Set<Integer> timePathEdgeIds = new HashSet<>();
    private Integer selectedStartId;
    private Integer selectedEndId;
    private boolean showTraffic;
    private List<VehicleRenderState> trafficVehicles = List.of();
    private double centerX;
    private double centerY;
    private double scale = 0.05;
    private Point dragPoint;
    private Point hoverPoint;
    private MapClickListener mapClickListener;

    public MapPanel(ViewportService viewportService) {
        this.viewportService = viewportService;
        setBackground(Color.WHITE);
        setToolTipText("");
        installMouseHandlers();
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        clearHighlights();
        resetView();
    }

    public void highlightVertices(Collection<Integer> vertexIds) {
        queryVertexIds.clear();
        queryVertexIds.addAll(vertexIds);
        repaint();
    }

    public void highlightEdges(Collection<Integer> edgeIds) {
        queryEdgeIds.clear();
        queryEdgeIds.addAll(edgeIds);
        repaint();
    }

    public void setDistancePathEdges(Collection<Integer> edgeIds) {
        distancePathEdgeIds.clear();
        distancePathEdgeIds.addAll(edgeIds);
        repaint();
    }

    public void setDistancePathVertices(Collection<Integer> vertexIds) {
        distancePathVertexIds.clear();
        distancePathVertexIds.addAll(vertexIds);
        repaint();
    }

    public void setTimePathEdges(Collection<Integer> edgeIds) {
        timePathEdgeIds.clear();
        timePathEdgeIds.addAll(edgeIds);
        repaint();
    }

    public void setTimePathVertices(Collection<Integer> vertexIds) {
        timePathVertexIds.clear();
        timePathVertexIds.addAll(vertexIds);
        repaint();
    }

    public void clearHighlights() {
        queryVertexIds.clear();
        queryEdgeIds.clear();
        distancePathVertexIds.clear();
        timePathVertexIds.clear();
        distancePathEdgeIds.clear();
        timePathEdgeIds.clear();
        repaint();
    }

    public void setSelectedVertices(Integer startId, Integer endId) {
        this.selectedStartId = startId;
        this.selectedEndId = endId;
        repaint();
    }

    public void setTrafficMode(boolean showTraffic) {
        this.showTraffic = showTraffic;
        repaint();
    }

    public void setTrafficVehicles(List<VehicleRenderState> trafficVehicles) {
        this.trafficVehicles = List.copyOf(trafficVehicles);
        repaint();
    }

    public void setMapClickListener(MapClickListener mapClickListener) {
        this.mapClickListener = mapClickListener;
    }

    public void resetView() {
        if (graph == null || graph.vertexCount() == 0) {
            return;
        }
        Rectangle2D bounds = graph.getBounds();
        centerX = bounds.getCenterX();
        centerY = bounds.getCenterY();
        int w = Math.max(getWidth(), 900);
        int h = Math.max(getHeight(), 700);
        scale = Math.min(w / (bounds.getWidth() * 1.1), h / (bounds.getHeight() * 1.1));
        if (!Double.isFinite(scale) || scale <= 0) {
            scale = 0.05;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (graph == null || graph.vertexCount() == 0) {
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("暂无地图数据，请先生成或加载地图。", 30, 30);
            g2.dispose();
            return;
        }

        Rectangle2D worldRect = getWorldViewport();
        List<Vertex> visibleVertices = viewportService.getVisibleVertices(graph, worldRect, getWidth(), getHeight(), scale);
        Set<Integer> visibleIds = new HashSet<>();
        for (Vertex vertex : visibleVertices) {
            visibleIds.add(vertex.getId());
        }

        for (Edge edge : graph.getEdges()) {
            boolean highlighted = queryEdgeIds.contains(edge.getId())
                    || distancePathEdgeIds.contains(edge.getId())
                    || timePathEdgeIds.contains(edge.getId());
            if (!highlighted && (!visibleIds.contains(edge.getFromId()) || !visibleIds.contains(edge.getToId()))) {
                continue;
            }
            Vertex from = graph.getVertex(edge.getFromId());
            Vertex to = graph.getVertex(edge.getToId());
            Color color = EDGE_DEFAULT;
            float stroke = 1.0f;
            if (showTraffic && !highlighted) {
                color = trafficColor(edge.getOccupancyRatio());
                stroke = 1.2f + (float) Math.min(4.0, edge.getOccupancyRatio() * 4.0);
            }
            if (queryEdgeIds.contains(edge.getId())) {
                color = EDGE_QUERY;
                stroke = 1.8f;
            }
            if (distancePathEdgeIds.contains(edge.getId())) {
                color = EDGE_DISTANCE;
                stroke = 2.8f;
            }
            if (timePathEdgeIds.contains(edge.getId())) {
                color = EDGE_TIME;
                stroke = 3.0f;
            }
            g2.setColor(color);
            g2.setStroke(new BasicStroke(stroke));
            g2.draw(new Line2D.Double(
                    worldToScreenX(from.getX()),
                    worldToScreenY(from.getY()),
                    worldToScreenX(to.getX()),
                    worldToScreenY(to.getY())));
        }

        for (Vertex vertex : visibleVertices) {
            int radius = 4;
            Color color = POINT_DEFAULT;
            if (queryVertexIds.contains(vertex.getId())) {
                color = POINT_QUERY;
                radius = 6;
            }
            if (selectedStartId != null && selectedStartId == vertex.getId()) {
                color = POINT_START;
                radius = 7;
            }
            if (selectedEndId != null && selectedEndId == vertex.getId()) {
                color = POINT_END;
                radius = 7;
            }
            if (distancePathVertexIds.contains(vertex.getId())) {
                color = EDGE_DISTANCE;
                radius = 6;
            }
            if (timePathVertexIds.contains(vertex.getId())) {
                color = EDGE_TIME;
                radius = 6;
            }
            if (selectedStartId != null && selectedStartId == vertex.getId()) {
                color = POINT_START;
                radius = 7;
            }
            if (selectedEndId != null && selectedEndId == vertex.getId()) {
                color = POINT_END;
                radius = 7;
            }
            double x = worldToScreenX(vertex.getX()) - radius / 2.0;
            double y = worldToScreenY(vertex.getY()) - radius / 2.0;
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y, radius, radius));
        }

        if (showTraffic) {
            drawVehicles(g2);
        }
        drawCoordinateOverlay(g2);
        g2.dispose();
    }

    private void drawVehicles(Graphics2D g2) {
        g2.setColor(new Color(36, 96, 220, 220));
        for (VehicleRenderState state : trafficVehicles) {
            Vertex from = graph.getVertex(state.fromId());
            Vertex to = graph.getVertex(state.toId());
            if (from == null || to == null) {
                continue;
            }
            double x = from.getX() + (to.getX() - from.getX()) * state.progress();
            double y = from.getY() + (to.getY() - from.getY()) * state.progress();
            double sx = worldToScreenX(x) - 3.0;
            double sy = worldToScreenY(y) - 3.0;
            g2.fill(new Ellipse2D.Double(sx, sy, 6, 6));
        }
    }

    private void installMouseHandlers() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragPoint == null) {
                    return;
                }
                int dx = e.getX() - dragPoint.x;
                int dy = e.getY() - dragPoint.y;
                centerX -= dx / scale;
                centerY += dy / scale;
                dragPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPoint = e.getPoint();
                updateHoverTooltip(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragPoint = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (graph == null || mapClickListener == null || e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                mapClickListener.onMapClicked(screenToWorldX(e.getX()), screenToWorldY(e.getY()));
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (graph == null) {
                    return;
                }
                double beforeX = screenToWorldX(e.getX());
                double beforeY = screenToWorldY(e.getY());
                double factor = e.getPreciseWheelRotation() < 0 ? 1.12 : 0.9;
                scale *= Math.pow(factor, Math.abs(e.getPreciseWheelRotation()));
                scale = clamp(scale, 0.01, 40.0);
                double afterX = screenToWorldX(e.getX());
                double afterY = screenToWorldY(e.getY());
                centerX += beforeX - afterX;
                centerY += beforeY - afterY;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverPoint = null;
                setToolTipText(null);
                repaint();
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private void updateHoverTooltip(int screenX, int screenY) {
        if (graph == null) {
            setToolTipText(null);
            return;
        }
        Edge edge = findNearestEdgeAtScreen(screenX, screenY, 8.0);
        if (edge == null) {
            setToolTipText(null);
            return;
        }
        setToolTipText(String.format(
                Locale.US,
                "道路 %d | 长度: %.1f | 容量: %d 辆 | 当前车辆: %d 辆 | 负载比: %.2f",
                edge.getId(),
                edge.getLength(),
                edge.getCapacity(),
                edge.getCurrentVehicles(),
                edge.getOccupancyRatio()));
    }

    private Edge findNearestEdgeAtScreen(int screenX, int screenY, double maxDistance) {
        Edge nearest = null;
        double bestDistanceSq = maxDistance * maxDistance;
        for (Edge edge : graph.getEdges()) {
            Vertex from = graph.getVertex(edge.getFromId());
            Vertex to = graph.getVertex(edge.getToId());
            if (from == null || to == null) {
                continue;
            }
            double x1 = worldToScreenX(from.getX());
            double y1 = worldToScreenY(from.getY());
            double x2 = worldToScreenX(to.getX());
            double y2 = worldToScreenY(to.getY());
            double distanceSq = Line2D.ptSegDistSq(x1, y1, x2, y2, screenX, screenY);
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                nearest = edge;
            }
        }
        return nearest;
    }

    private void drawCoordinateOverlay(Graphics2D g2) {
        String mouseText;
        if (hoverPoint == null) {
            mouseText = "鼠标坐标: (-, -)";
        } else {
            mouseText = String.format(Locale.US, "鼠标坐标: (%.1f, %.1f)",
                    screenToWorldX(hoverPoint.x), screenToWorldY(hoverPoint.y));
        }
        String centerText = String.format(Locale.US, "视图中心: (%.1f, %.1f)", centerX, centerY);
        String scaleText = String.format(Locale.US, "缩放倍率: %.3f", scale);

        FontMetrics metrics = g2.getFontMetrics();
        int width = Math.max(metrics.stringWidth(mouseText),
                Math.max(metrics.stringWidth(centerText), metrics.stringWidth(scaleText))) + 20;
        int height = metrics.getHeight() * 3 + 18;
        int x = 12;
        int y = 12;

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(x, y, width, height, 12, 12);
        g2.setColor(new Color(90, 90, 90, 180));
        g2.drawRoundRect(x, y, width, height, 12, 12);
        g2.setColor(new Color(40, 40, 40));
        int textY = y + metrics.getAscent() + 8;
        g2.drawString(mouseText, x + 10, textY);
        g2.drawString(centerText, x + 10, textY + metrics.getHeight());
        g2.drawString(scaleText, x + 10, textY + metrics.getHeight() * 2);
    }

    private Rectangle2D getWorldViewport() {
        double width = getWidth() / scale;
        double height = getHeight() / scale;
        return new Rectangle2D.Double(centerX - width / 2.0, centerY - height / 2.0, width, height);
    }

    private double worldToScreenX(double worldX) {
        return (worldX - centerX) * scale + getWidth() / 2.0;
    }

    private double worldToScreenY(double worldY) {
        return getHeight() / 2.0 - (worldY - centerY) * scale;
    }

    private double screenToWorldX(double screenX) {
        return (screenX - getWidth() / 2.0) / scale + centerX;
    }

    private double screenToWorldY(double screenY) {
        return (getHeight() / 2.0 - screenY) / scale + centerY;
    }

    private Color trafficColor(double ratio) {
        if (ratio <= 0.5) {
            return new Color(46, 170, 70);
        }
        if (ratio <= 0.9) {
            return new Color(236, 196, 35);
        }
        return new Color(214, 54, 46);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
