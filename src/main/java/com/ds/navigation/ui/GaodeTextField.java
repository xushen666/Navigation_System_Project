package com.ds.navigation.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class GaodeTextField extends JTextField {
    private final int arc = ThemeConstants.FIELD_ARC;
    private boolean focused;

    public GaodeTextField(int columns) {
        super(columns);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setFont(ThemeConstants.FONT_14);
        setForeground(ThemeConstants.TEXT_PRIMARY);
        setCaretColor(ThemeConstants.PRIMARY_BLUE);
        setSelectionColor(ThemeConstants.PRIMARY_LIGHT);
        setSelectedTextColor(ThemeConstants.TEXT_PRIMARY);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(ThemeConstants.CARD_WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        Color borderColor = focused ? ThemeConstants.PRIMARY_BLUE : ThemeConstants.BORDER_LIGHT;
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
