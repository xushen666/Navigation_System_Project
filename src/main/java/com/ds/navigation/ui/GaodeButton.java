package com.ds.navigation.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class GaodeButton extends JButton {

    public enum Style {
        PRIMARY_FILL,
        PRIMARY_OUTLINE,
        TEXT_ONLY
    }

    private Style style;
    private boolean hover;
    private boolean pressed;

    public GaodeButton(String text) {
        this(text, Style.PRIMARY_FILL);
    }

    public GaodeButton(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(ThemeConstants.FONT_14);
        installMouseListeners();
    }

    public void setGaodeStyle(Style style) {
        this.style = style;
        repaint();
    }

    private void installMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        };
        addMouseListener(adapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = ThemeConstants.BUTTON_ARC;

        switch (style) {
            case PRIMARY_FILL -> {
                Color bg = pressed ? ThemeConstants.PRIMARY_DARK
                        : hover ? ThemeConstants.darken(ThemeConstants.PRIMARY_BLUE, 0.08)
                        : ThemeConstants.PRIMARY_BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(ThemeConstants.TEXT_WHITE);
            }
            case PRIMARY_OUTLINE -> {
                Color fill = hover ? ThemeConstants.PRIMARY_LIGHT : ThemeConstants.CARD_WHITE;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(ThemeConstants.PRIMARY_BLUE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.setColor(hover ? ThemeConstants.PRIMARY_DARK : ThemeConstants.PRIMARY_BLUE);
            }
            case TEXT_ONLY -> {
                if (hover) {
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 40));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, arc, arc);
                }
                g2.setColor(hover ? ThemeConstants.PRIMARY_DARK : getForeground());
            }
        }

        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h + fm.getAscent()) / 2 - 2;
        g2.drawString(getText(), textX, textY);
        g2.dispose();
    }
}
