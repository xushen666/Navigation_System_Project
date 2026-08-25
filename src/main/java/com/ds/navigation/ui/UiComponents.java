package com.ds.navigation.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class RoundedPanel extends JPanel {
    private int arcWidth = ThemeConstants.CARD_ARC;
    private int arcHeight = ThemeConstants.CARD_ARC;
    private boolean showShadow = true;
    private int shadowSize = ThemeConstants.SHADOW_SIZE;

    RoundedPanel() {
        setOpaque(false);
    }

    public void setArc(int arc) {
        this.arcWidth = arc;
        this.arcHeight = arc;
    }

    public void setShowShadow(boolean showShadow) {
        this.showShadow = showShadow;
    }

    @Override
    public Insets getInsets() {
        Insets base = super.getInsets();
        int extra = showShadow ? shadowSize : 0;
        return new Insets(base.top + extra, base.left + extra,
                base.bottom + extra, base.right + extra);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int drawW = getWidth() - (showShadow ? shadowSize : 0);
        int drawH = getHeight() - (showShadow ? shadowSize : 0);

        if (showShadow) {
            for (int i = shadowSize; i > 0; i--) {
                int alpha = 3;
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRoundRect(shadowSize - i, shadowSize + i / 2,
                        drawW - (shadowSize - i) * 2,
                        drawH - shadowSize - i / 2,
                        arcWidth, arcHeight);
            }
        }

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, drawW, drawH, arcWidth, arcHeight);

        g2.dispose();
        super.paintComponent(g);
    }
}

class CardPanel extends RoundedPanel {
    private final JLabel titleLabel;
    private final JPanel contentPanel;
    private String title;

    CardPanel(String title) {
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
        int drawW = getWidth() - ThemeConstants.SHADOW_SIZE;
        g2.drawLine(12, y, drawW - 24, y);
        g2.dispose();
    }
}

class Button extends JButton {

    public enum Style {
        PRIMARY_FILL,
        PRIMARY_OUTLINE,
        TEXT_ONLY
    }

    private Style style;
    private boolean hover;
    private boolean pressed;

    Button(String text) {
        this(text, Style.PRIMARY_FILL);
    }

    Button(String text, Style style) {
        super(text);
        this.style = style;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(ThemeConstants.FONT_13);
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
        int innerW = Math.max(0, w - 2);
        int innerH = Math.max(0, h - 2);

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
                g2.fillRoundRect(1, 1, innerW, innerH, arc, arc);
                g2.setStroke(new BasicStroke(1.1f));
                g2.setColor(ThemeConstants.PRIMARY_BLUE);
                g2.drawRoundRect(1, 1, innerW - 1, innerH - 1, arc, arc);
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

class TextField extends JTextField {
    private final int arc = ThemeConstants.FIELD_ARC;
    private boolean focused;

    TextField(int columns) {
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

class StatusBar extends JPanel {
    private final JLabel statusLabel = new JLabel("就绪");

    StatusBar() {
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
