package com.ds.navigation.ui;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.service.TrafficSimulationService.VehicleRenderState;
import com.ds.navigation.service.ViewportService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import java.awt.geom.RoundRectangle2D;
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

    // Floating control button layout (bottom-right corner)
    private static final int FLOAT_BTN_SIZE = 34;
    private static final int FLOAT_BTN_GAP = 3;
    private static final int FLOAT_MARGIN = 16;
    private static final int FLOAT_TOTAL_H = FLOAT_BTN_SIZE * 4 + FLOAT_BTN_GAP * 3;
    private static final int FLOAT_PANEL_W = FLOAT_BTN_SIZE + 12;
    private static final int FLOAT_PANEL_H = FLOAT_TOTAL_H + 12;

    public MapPanel(ViewportService viewportService) {
        this.viewportService = viewportService;
        setBackground(ThemeConstants.BG_LIGHT_GRAY);
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
            g2.setColor(ThemeConstants.TEXT_SECONDARY);
            g2.setFont(ThemeConstants.FONT_14);
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

        drawEdges(g2, visibleIds);
        drawVertices(g2, visibleVertices);

        if (showTraffic) {
            drawVehicles(g2);
        }
        drawCoordinateOverlay(g2);
        drawFloatingControls(g2);
        g2.dispose();
    }

    private void drawEdges(Graphics2D g2, Set<Integer> visibleIds) {
        for (Edge edge : graph.getEdges()) {
            boolean highlighted = queryEdgeIds.contains(edge.getId())
                    || distancePathEdgeIds.contains(edge.getId())
                    || timePathEdgeIds.contains(edge.getId());
            if (!highlighted && (!visibleIds.contains(edge.getFromId()) || !visibleIds.contains(edge.getToId()))) {
                continue;
            }
            Vertex from = graph.getVertex(edge.getFromId());
            Vertex to = graph.getVertex(edge.getToId());
            Color color = ThemeConstants.ROAD_DEFAULT;
            float stroke = 1.0f;
            if (showTraffic && !highlighted) {
                color = trafficColor(edge.getOccupancyRatio());
                stroke = 1.2f + (float) Math.min(3.0, edge.getOccupancyRatio() * 3.0);
            }
            if (queryEdgeIds.contains(edge.getId())) {
                color = ThemeConstants.ROAD_QUERY;
                stroke = 1.8f;
            }
            boolean isDistance = distancePathEdgeIds.contains(edge.getId());
            boolean isTime = timePathEdgeIds.contains(edge.getId());
            if (isDistance || isTime) {
                color = isDistance ? ThemeConstants.ROAD_DISTANCE : ThemeConstants.ROAD_TIME;
                stroke = isDistance ? 2.6f : 2.8f;
                g2.setStroke(new BasicStroke(stroke + 4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                g2.draw(new Line2D.Double(
                        worldToScreenX(from.getX()), worldToScreenY(from.getY()),
                        worldToScreenX(to.getX()), worldToScreenY(to.getY())));
            }
            g2.setColor(color);
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(
                    worldToScreenX(from.getX()), worldToScreenY(from.getY()),
                    worldToScreenX(to.getX()), worldToScreenY(to.getY())));
        }
    }

    private void drawVertices(Graphics2D g2, List<Vertex> visibleVertices) {
        for (Vertex vertex : visibleVertices) {
            int radius = 3;
            Color color = ThemeConstants.POINT_DEFAULT;
            if (queryVertexIds.contains(vertex.getId())) {
                color = ThemeConstants.ROAD_QUERY;
                radius = 5;
            }
            if (distancePathVertexIds.contains(vertex.getId())) {
                color = ThemeConstants.ROAD_DISTANCE;
                radius = 5;
            }
            if (timePathVertexIds.contains(vertex.getId())) {
                color = ThemeConstants.ROAD_TIME;
                radius = 5;
            }
            boolean isStart = selectedStartId != null && selectedStartId == vertex.getId();
            boolean isEnd = selectedEndId != null && selectedEndId == vertex.getId();

            double sx = worldToScreenX(vertex.getX());
            double sy = worldToScreenY(vertex.getY());

            if (isStart || isEnd) {
                Color markerColor = isStart ? ThemeConstants.POINT_START : ThemeConstants.POINT_END;
                g2.setColor(markerColor);
                g2.fill(new Ellipse2D.Double(sx - 7, sy - 7, 14, 14));
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(sx - 3, sy - 3, 6, 6));
            } else {
                g2.setColor(color);
                g2.fill(new Ellipse2D.Double(sx - radius / 2.0, sy - radius / 2.0, radius, radius));
            }
        }
    }

    private void drawVehicles(Graphics2D g2) {
        g2.setColor(new Color(0x1A, 0x6B, 0xC0, 220));
        Font vehicleFont = new Font("Microsoft YaHei", Font.PLAIN, 10);
        g2.setFont(vehicleFont);
        for (VehicleRenderState state : trafficVehicles) {
            Vertex from = graph.getVertex(state.fromId());
            Vertex to = graph.getVertex(state.toId());
            if (from == null || to == null) {
                continue;
            }
            double x = from.getX() + (to.getX() - from.getX()) * state.progress();
            double y = from.getY() + (to.getY() - from.getY()) * state.progress();
            double sx = worldToScreenX(x);
            double sy = worldToScreenY(y);

            double dx = to.getX() - from.getX();
            double dy = to.getY() - from.getY();
            double angle = Math.atan2(dy, dx);

            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            xPoints[0] = (int) (sx + Math.cos(angle) * 5);
            yPoints[0] = (int) (sy - Math.sin(angle) * 5);
            xPoints[1] = (int) (sx + Math.cos(angle + 2.5) * 4);
            yPoints[1] = (int) (sy - Math.sin(angle + 2.5) * 4);
            xPoints[2] = (int) (sx + Math.cos(angle - 2.5) * 4);
            yPoints[2] = (int) (sy - Math.sin(angle - 2.5) * 4);

            g2.fillPolygon(xPoints, yPoints, 3);
        }
    }

    private void drawFloatingControls(Graphics2D g2) {
        int px = getWidth() - FLOAT_MARGIN - FLOAT_PANEL_W;
        int py = getHeight() - FLOAT_MARGIN - FLOAT_PANEL_H;

        g2.setColor(new Color(255, 255, 255, 225));
        g2.fill(new RoundRectangle2D.Double(px, py, FLOAT_PANEL_W, FLOAT_PANEL_H, 8, 8));
        g2.setColor(ThemeConstants.BORDER_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(px, py, FLOAT_PANEL_W, FLOAT_PANEL_H, 8, 8));

        int bx = px + 6;
        int by = py + 6;
        Font floatFont = new Font("Microsoft YaHei", Font.BOLD, 16);
        g2.setFont(floatFont);
        FontMetrics fm = g2.getFontMetrics();

        drawFloatButton(g2, bx, by, FLOAT_BTN_SIZE, "+");
        by += FLOAT_BTN_SIZE + FLOAT_BTN_GAP;
        drawFloatButton(g2, bx, by, FLOAT_BTN_SIZE, "-");
        by += FLOAT_BTN_SIZE + FLOAT_BTN_GAP;
        drawFloatButton(g2, bx, by, FLOAT_BTN_SIZE, "⟲");
        by += FLOAT_BTN_SIZE + FLOAT_BTN_GAP;
        drawFloatButton(g2, bx, by, FLOAT_BTN_SIZE, "✕");
    }

    private void drawFloatButton(Graphics2D g2, int x, int y, int size, String symbol) {
        g2.setColor(ThemeConstants.CARD_WHITE);
        g2.fill(new RoundRectangle2D.Double(x, y, size, size, 6, 6));
        g2.setColor(ThemeConstants.BORDER_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(x, y, size, size, 6, 6));

        g2.setColor(ThemeConstants.TEXT_PRIMARY);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(symbol);
        g2.drawString(symbol, x + (size - tw) / 2, y + (size + fm.getAscent()) / 2 - 2);
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
                if (graph == null || e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                int floatAction = hitTestFloatingButton(e.getX(), e.getY());
                if (floatAction >= 0) {
                    handleFloatAction(floatAction);
                    return;
                }
                if (mapClickListener != null) {
                    mapClickListener.onMapClicked(screenToWorldX(e.getX()), screenToWorldY(e.getY()));
                }
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

    private int hitTestFloatingButton(int screenX, int screenY) {
        int px = getWidth() - FLOAT_MARGIN - FLOAT_PANEL_W + 6;
        int py = getHeight() - FLOAT_MARGIN - FLOAT_PANEL_H + 6;
        for (int i = 0; i < 4; i++) {
            int by = py + i * (FLOAT_BTN_SIZE + FLOAT_BTN_GAP);
            if (screenX >= px && screenX <= px + FLOAT_BTN_SIZE
                    && screenY >= by && screenY <= by + FLOAT_BTN_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void handleFloatAction(int index) {
        double factor;
        switch (index) {
            case 0 -> { // zoom in
                scale *= 1.25;
                scale = clamp(scale, 0.01, 40.0);
                repaint();
            }
            case 1 -> { // zoom out
                scale *= 0.8;
                scale = clamp(scale, 0.01, 40.0);
                repaint();
            }
            case 2 -> resetView();  // reset view
            case 3 -> { // clear highlights
                clearHighlights();
                repaint();
            }
        }
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
                "<html>道路 %d &nbsp;|&nbsp; 长度: %.1f &nbsp;|&nbsp; 容量: %d &nbsp;|&nbsp; 当前: %d &nbsp;|&nbsp; 负载: %.2f</html>",
                edge.getId(), edge.getLength(), edge.getCapacity(),
                edge.getCurrentVehicles(), edge.getOccupancyRatio()));
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
            mouseText = "鼠标: (-, -)";
        } else {
            mouseText = String.format(Locale.US, "鼠标: (%.1f, %.1f)",
                    screenToWorldX(hoverPoint.x), screenToWorldY(hoverPoint.y));
        }
        String centerText = String.format(Locale.US, "中心: (%.1f, %.1f)", centerX, centerY);
        String scaleText = String.format(Locale.US, "缩放: %.3f", scale);

        g2.setFont(ThemeConstants.FONT_12);
        FontMetrics metrics = g2.getFontMetrics();
        int width = Math.max(metrics.stringWidth(mouseText),
                Math.max(metrics.stringWidth(centerText), metrics.stringWidth(scaleText))) + 18;
        int height = metrics.getHeight() * 3 + 16;
        int x = 10;
        int y = 10;

        g2.setColor(new Color(255, 255, 255, 210));
        g2.fill(new RoundRectangle2D.Double(x, y, width, height, 8, 8));
        g2.setColor(new Color(0xD0, 0xD0, 0xD0, 160));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Double(x, y, width, height, 8, 8));
        g2.setColor(ThemeConstants.TEXT_SECONDARY);
        int textY = y + metrics.getAscent() + 5;
        g2.drawString(mouseText, x + 9, textY);
        g2.drawString(centerText, x + 9, textY + metrics.getHeight());
        g2.drawString(scaleText, x + 9, textY + metrics.getHeight() * 2);
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
            return ThemeConstants.TRAFFIC_SMOOTH;
        }
        if (ratio <= 0.9) {
            return ThemeConstants.TRAFFIC_MODERATE;
        }
        return ThemeConstants.TRAFFIC_CONGESTED;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
