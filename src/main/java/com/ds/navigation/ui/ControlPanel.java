package com.ds.navigation.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ControlPanel extends JPanel {
    private final TextField xField = new TextField(10);
    private final TextField yField = new TextField(10);
    private final JLabel startLabel = new JLabel("未选择");
    private final JLabel endLabel = new JLabel("未选择");
    private final Button generateButton = new Button("生成地图", Button.Style.PRIMARY_FILL);
    private final Button nearbyButton = new Button("附近100点", Button.Style.PRIMARY_FILL);
    private final Button distancePathButton = new Button("距离最短路径", Button.Style.PRIMARY_FILL);
    private final Button timePathButton = new Button("路况最优路径", Button.Style.PRIMARY_OUTLINE);
    private final Button startSimulationButton = new Button("开始模拟", Button.Style.PRIMARY_FILL);
    private final Button pauseSimulationButton = new Button("暂停模拟", Button.Style.PRIMARY_OUTLINE);
    private final Button resetSimulationButton = new Button("重置模拟", Button.Style.PRIMARY_OUTLINE);
    private final Button clearButton = new Button("清空高亮", Button.Style.PRIMARY_OUTLINE);
    private final Button resetViewButton = new Button("重置视图", Button.Style.PRIMARY_OUTLINE);
    private final JTextArea resultArea = new JTextArea(12, 18);

    public ControlPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.BG_LIGHT_GRAY);
        setPreferredSize(new Dimension(ThemeConstants.CONTROL_PANEL_WIDTH, 0));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ThemeConstants.BG_LIGHT_GRAY);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        configureActionButtons();

        // 查询坐标
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

        // 路径规划
        CardPanel routeCard = new CardPanel("路径规划");
        JPanel routeContent = verticalPanel();
        startLabel.setFont(ThemeConstants.FONT_14);
        startLabel.setForeground(ThemeConstants.TEXT_PRIMARY);
        startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        endLabel.setFont(ThemeConstants.FONT_14);
        endLabel.setForeground(ThemeConstants.TEXT_PRIMARY);
        endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        routeContent.add(labelRow(startLabel));
        routeContent.add(Box.createVerticalStrut(4));
        routeContent.add(labelRow(endLabel));
        routeContent.add(Box.createVerticalStrut(10));
        routeContent.add(centerRow(distancePathButton));
        routeContent.add(Box.createVerticalStrut(6));
        routeContent.add(centerRow(timePathButton));
        routeCard.addContent(routeContent);
        scrollContent.add(routeCard);

        scrollContent.add(Box.createVerticalStrut(8));

        // 车流模拟
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

        // 视图操作
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

        // 结果输出
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

    private void configureActionButtons() {
        List<Button> buttons = List.of(
                generateButton,
                nearbyButton,
                distancePathButton,
                timePathButton,
                startSimulationButton,
                pauseSimulationButton,
                resetSimulationButton,
                clearButton,
                resetViewButton);
        Dimension preferred = new Dimension(168, 28);
        for (Button button : buttons) {
            button.setPreferredSize(preferred);
            button.setMaximumSize(preferred);
            button.setMinimumSize(new Dimension(128, preferred.height));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        Dimension queryButtonSize = new Dimension(236, 28);
        nearbyButton.setPreferredSize(queryButtonSize);
        nearbyButton.setMaximumSize(queryButtonSize);
        nearbyButton.setMinimumSize(new Dimension(180, queryButtonSize.height));
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

    private JPanel labelRow(JLabel label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(label, BorderLayout.WEST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height + 2));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JPanel centerRow(Component component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.add(component);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        return row;
    }

    // -- Public API 

    public TextField getXField() {
        return xField;
    }

    public TextField getYField() {
        return yField;
    }

    public Button getGenerateButton() {
        return generateButton;
    }

    public Button getNearbyButton() {
        return nearbyButton;
    }

    public Button getDistancePathButton() {
        return distancePathButton;
    }

    public Button getTimePathButton() {
        return timePathButton;
    }

    public Button getStartSimulationButton() {
        return startSimulationButton;
    }

    public Button getPauseSimulationButton() {
        return pauseSimulationButton;
    }

    public Button getResetSimulationButton() {
        return resetSimulationButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getResetViewButton() {
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
