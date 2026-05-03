package com.ds.navigation.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CardPanel extends RoundedPanel {
    private final JLabel titleLabel;
    private final JPanel contentPanel;
    private String title;

    public CardPanel(String title) {
        this.title = title;
        setBackground(ThemeConstants.CARD_WHITE);
        setLayout(new BorderLayout(0, 0));

        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeConstants.FONT_16);
        titleLabel.setForeground(ThemeConstants.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));

        add(titleLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void addContent(Component comp) {
        contentPanel.add(comp);
    }

    public void setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeConstants.BORDER_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        int y = titleLabel.getY() + titleLabel.getHeight();
        int drawW = getWidth() - (isShowShadow() ? ThemeConstants.SHADOW_SIZE : 0);
        g2.drawLine(12, y, drawW - 24, y);
        g2.dispose();
    }

    private boolean isShowShadow() {
        return true;
    }
}
