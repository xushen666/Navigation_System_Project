package com.ds.navigation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ControlPanel extends JPanel {
    private final JTextField xField = new JTextField("5000", 12);
    private final JTextField yField = new JTextField("5000", 12);
    private final JLabel startLabel = new JLabel("A 点：未选择");
    private final JLabel endLabel = new JLabel("B 点：未选择");
    private final JButton generateButton = new JButton("生成地图");
    private final JButton nearbyButton = new JButton("附近100点");
    private final JButton distancePathButton = new JButton("距离最短路径");
    private final JButton timePathButton = new JButton("路况最优路径");
    private final JButton startSimulationButton = new JButton("开始模拟");
    private final JButton pauseSimulationButton = new JButton("暂停模拟");
    private final JButton resetSimulationButton = new JButton("重置模拟");
    private final JButton clearButton = new JButton("清空高亮");
    private final JButton resetViewButton = new JButton("重置视图");
    private final JTextArea resultArea = new JTextArea(18, 18);

    public ControlPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(300, 0));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("查询坐标 X"), gbc);
        gbc.gridy = 1;
        topPanel.add(xField, gbc);
        gbc.gridy = 2;
        topPanel.add(new JLabel("查询坐标 Y"), gbc);
        gbc.gridy = 3;
        topPanel.add(yField, gbc);
        gbc.gridy = 4;
        topPanel.add(startLabel, gbc);
        gbc.gridy = 5;
        topPanel.add(endLabel, gbc);
        gbc.gridy = 6;
        topPanel.add(generateButton, gbc);
        gbc.gridy = 7;
        topPanel.add(nearbyButton, gbc);
        gbc.gridy = 8;
        topPanel.add(distancePathButton, gbc);
        gbc.gridy = 9;
        topPanel.add(timePathButton, gbc);
        gbc.gridy = 10;
        topPanel.add(startSimulationButton, gbc);
        gbc.gridy = 11;
        topPanel.add(pauseSimulationButton, gbc);
        gbc.gridy = 12;
        topPanel.add(resetSimulationButton, gbc);
        gbc.gridy = 13;
        topPanel.add(clearButton, gbc);
        gbc.gridy = 14;
        topPanel.add(resetViewButton, gbc);
        gbc.gridy = 15;
        gbc.weighty = 1;
        topPanel.add(Box.createVerticalGlue(), gbc);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("结果输出"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public JTextField getXField() {
        return xField;
    }

    public JTextField getYField() {
        return yField;
    }

    public JButton getGenerateButton() {
        return generateButton;
    }

    public JButton getNearbyButton() {
        return nearbyButton;
    }

    public JButton getDistancePathButton() {
        return distancePathButton;
    }

    public JButton getTimePathButton() {
        return timePathButton;
    }

    public JButton getStartSimulationButton() {
        return startSimulationButton;
    }

    public JButton getPauseSimulationButton() {
        return pauseSimulationButton;
    }

    public JButton getResetSimulationButton() {
        return resetSimulationButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getResetViewButton() {
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
