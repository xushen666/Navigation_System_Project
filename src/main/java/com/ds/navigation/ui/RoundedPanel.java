package com.ds.navigation.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private int arcWidth = ThemeConstants.CARD_ARC;
    private int arcHeight = ThemeConstants.CARD_ARC;
    private boolean showShadow = true;
    private int shadowSize = ThemeConstants.SHADOW_SIZE;

    public RoundedPanel() {
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
