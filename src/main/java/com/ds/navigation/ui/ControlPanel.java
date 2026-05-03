package com.ds.navigation.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ControlPanel extends JPanel {
    private final GaodeTextField xField = new GaodeTextField(10);
    private final GaodeTextField yField = new GaodeTextField(10);
    private final JLabel startLabel = new JLabel("未选择");
    private final JLabel endLabel = new JLabel("未选择");
    private final GaodeButton generateButton = new GaodeButton("生成地图", GaodeButton.Style.PRIMARY_FILL);
    private final GaodeButton nearbyButton = new GaodeButton("附近100点", GaodeButton.Style.PRIMARY_FILL);
    private final GaodeButton distancePathButton = new GaodeButton("距离最短路径", GaodeButton.Style.PRIMARY_FILL);
    private final GaodeButton timePathButton = new GaodeButton("路况最优路径", GaodeButton.Style.PRIMARY_OUTLINE);
    private final GaodeButton startSimulationButton = new GaodeButton("开始模拟", GaodeButton.Style.PRIMARY_FILL);
    private final GaodeButton pauseSimulationButton = new GaodeButton("暂停模拟", GaodeButton.Style.PRIMARY_OUTLINE);
    private final GaodeButton resetSimulationButton = new GaodeButton("重置模拟", GaodeButton.Style.TEXT_ONLY);
    private final GaodeButton clearButton = new GaodeButton("清空高亮", GaodeButton.Style.TEXT_ONLY);
    private final GaodeButton resetViewButton = new GaodeButton("重置视图", GaodeButton.Style.PRIMARY_OUTLINE);
    private final JTextArea resultArea = new JTextArea(12, 18);

    public ControlPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BG_LIGHT_GRAY);
        setPreferredSize(new Dimension(ThemeConstants.CONTROL_PANEL_WIDTH, 0));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ThemeConstants.BG_LIGHT_GRAY);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Card 1: Query coordinates
        CardPanel queryCard = new CardPanel("查询坐标");
        JPanel queryContent = verticalPanel();
        queryContent.add(fieldRow("X 坐标", xField));
        queryContent.add(Box.createVerticalStrut(6));
        queryContent.add(fieldRow("Y 坐标", yField));
        queryContent.add(Box.createVerticalStrut(10));
        queryContent.add(nearbyButton);
        queryCard.addContent(queryContent);
        scrollContent.add(queryCard);

        scrollContent.add(Box.createVerticalStrut(8));

        // Card 2: Route planning
        CardPanel routeCard = new CardPanel("路径规划");
        JPanel routeContent = verticalPanel();
        startLabel.setFont(ThemeConstants.FONT_14);
        startLabel.setForeground(ThemeConstants.TEXT_PRIMARY);
        endLabel.setFont(ThemeConstants.FONT_14);
        endLabel.setForeground(ThemeConstants.TEXT_PRIMARY);
        routeContent.add(startLabel);
        routeContent.add(Box.createVerticalStrut(4));
        routeContent.add(endLabel);
        routeContent.add(Box.createVerticalStrut(10));
        routeContent.add(distancePathButton);
        routeContent.add(Box.createVerticalStrut(6));
        routeContent.add(timePathButton);
        routeCard.addContent(routeContent);
        scrollContent.add(routeCard);

        scrollContent.add(Box.createVerticalStrut(8));

        // Card 3: Simulation
        CardPanel simCard = new CardPanel("车流模拟");
        JPanel simContent = verticalPanel();
        simContent.add(startSimulationButton);
        simContent.add(Box.createVerticalStrut(6));
        simContent.add(pauseSimulationButton);
        simContent.add(Box.createVerticalStrut(6));
        simContent.add(resetSimulationButton);
        simCard.addContent(simContent);
        scrollContent.add(simCard);

        scrollContent.add(Box.createVerticalStrut(8));

        // Card 4: View controls
        CardPanel viewCard = new CardPanel("视图操作");
        JPanel viewContent = verticalPanel();
        viewContent.add(generateButton);
        viewContent.add(Box.createVerticalStrut(6));
        viewContent.add(resetViewButton);
        viewContent.add(Box.createVerticalStrut(6));
        viewContent.add(clearButton);
        viewCard.addContent(viewContent);
        scrollContent.add(viewCard);

        scrollContent.add(Box.createVerticalStrut(8));

        // Card 5: Result output
        CardPanel resultCard = new CardPanel("结果输出");
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(ThemeConstants.FONT_12);
        resultArea.setForeground(ThemeConstants.TEXT_SECONDARY);
        resultArea.setBackground(ThemeConstants.CARD_WHITE);
        resultArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(null);
        resultScroll.setPreferredSize(new Dimension(0, 120));
        resultCard.addContent(resultScroll);
        scrollContent.add(resultCard);

        JScrollPane outerScroll = new JScrollPane(scrollContent);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.setBackground(ThemeConstants.BG_LIGHT_GRAY);
        outerScroll.getViewport().setBackground(ThemeConstants.BG_LIGHT_GRAY);
        add(outerScroll, BorderLayout.CENTER);
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    private JPanel fieldRow(String labelText, Component field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(ThemeConstants.FONT_14);
        label.setForeground(ThemeConstants.TEXT_SECONDARY);
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 2));
        return row;
    }

    // -- Public API (unchanged signatures) --

    public GaodeTextField getXField() {
        return xField;
    }

    public GaodeTextField getYField() {
        return yField;
    }

    public GaodeButton getGenerateButton() {
        return generateButton;
    }

    public GaodeButton getNearbyButton() {
        return nearbyButton;
    }

    public GaodeButton getDistancePathButton() {
        return distancePathButton;
    }

    public GaodeButton getTimePathButton() {
        return timePathButton;
    }

    public GaodeButton getStartSimulationButton() {
        return startSimulationButton;
    }

    public GaodeButton getPauseSimulationButton() {
        return pauseSimulationButton;
    }

    public GaodeButton getResetSimulationButton() {
        return resetSimulationButton;
    }

    public GaodeButton getClearButton() {
        return clearButton;
    }

    public GaodeButton getResetViewButton() {
        return resetViewButton;
    }

    public void setSelectedStart(String text) {
        startLabel.setText("A 点：" + text);
    }

    public void setSelectedEnd(String text) {
        endLabel.setText("B 点：" + text);
    }

    public void setResultText(String text) {
        resultArea.setText(text);
    }
}
