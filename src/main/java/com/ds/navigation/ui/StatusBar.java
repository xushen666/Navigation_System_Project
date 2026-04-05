package com.ds.navigation.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
    private final JLabel statusLabel = new JLabel("就绪");

    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.CENTER);
    }

    public void setMessage(String message) {
        statusLabel.setText(message);
    }
}
