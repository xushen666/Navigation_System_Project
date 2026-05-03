package com.ds.navigation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class NavigationBar extends JPanel {
    private final JPanel menuPanel;
    private final Map<String, List<PopupItem>> menuMap = new LinkedHashMap<>();

    public NavigationBar() {
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.TOOLBAR_BG);
        setPreferredSize(new Dimension(0, ThemeConstants.NAV_BAR_HEIGHT));

        JLabel titleLabel = new JLabel("导航系统课程设计");
        titleLabel.setFont(ThemeConstants.FONT_16);
        titleLabel.setForeground(ThemeConstants.TEXT_WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        add(titleLabel, BorderLayout.WEST);

        menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 10));
        menuPanel.setOpaque(false);
        add(menuPanel, BorderLayout.EAST);
    }

    public void addMenu(String menuName, List<PopupItem> items) {
        menuMap.put(menuName, items);
        GaodeButton btn = new GaodeButton(menuName, GaodeButton.Style.TEXT_ONLY);
        btn.setFont(ThemeConstants.FONT_14);
        btn.setForeground(ThemeConstants.TEXT_WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_LIGHT));
        for (PopupItem item : items) {
            JMenuItem menuItem = new JMenuItem(item.label());
            menuItem.setFont(ThemeConstants.FONT_14);
            menuItem.addActionListener(item.action());
            menuItem.setBackground(ThemeConstants.CARD_WHITE);
            popup.add(menuItem);
        }

        btn.addActionListener(e -> popup.show(btn, 0, btn.getHeight()));
        menuPanel.add(btn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeConstants.PRIMARY_DARK);
        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
        g2.dispose();
    }

    public record PopupItem(String label, ActionListener action) {
    }
}
