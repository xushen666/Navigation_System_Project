package com.ds.navigation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
    private final JLabel statusLabel = new JLabel("就绪");

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.STATUSBAR_BG);
        setPreferredSize(new Dimension(0, ThemeConstants.STATUSBAR_HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));

        statusLabel.setFont(ThemeConstants.FONT_12);
        statusLabel.setForeground(ThemeConstants.TEXT_SECONDARY);
        add(statusLabel, BorderLayout.CENTER);
    }

    public void setMessage(String message) {
        statusLabel.setText(message);
    }
}
