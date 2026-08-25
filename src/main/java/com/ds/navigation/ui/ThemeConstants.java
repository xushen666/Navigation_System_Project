package com.ds.navigation.ui;

import java.awt.Color;
import java.awt.Font;

public final class ThemeConstants {
    private ThemeConstants() {
    }

    // -- Primary palette (Gaode deep blue) --
    public static final Color PRIMARY_BLUE = new Color(0x1A, 0x6B, 0xC0);
    public static final Color PRIMARY_DARK = new Color(0x15, 0x58, 0xA0);
    public static final Color PRIMARY_LIGHT = new Color(0xE8, 0xF0, 0xFE);

    // -- Backgrounds --
    public static final Color BG_LIGHT_GRAY = new Color(0xF5, 0xF6, 0xFA);
    public static final Color CARD_WHITE = new Color(0xFF, 0xFF, 0xFF);
    public static final Color BORDER_LIGHT = new Color(0xE0, 0xE0, 0xE0);

    // -- Text --
    public static final Color TEXT_PRIMARY = new Color(0x33, 0x33, 0x33);
    public static final Color TEXT_SECONDARY = new Color(0x88, 0x88, 0x88);
    public static final Color TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);

    // -- Bars --
    public static final Color TOOLBAR_BG = new Color(0x1A, 0x6B, 0xC0);
    public static final Color STATUSBAR_BG = new Color(0xF0, 0xF0, 0xF4);

    // -- Road hierarchy widths (by length → tier) --
    public static final float ROAD_WIDTH_MAIN = 2.8f;
    public static final float ROAD_WIDTH_SECONDARY = 1.8f;
    public static final float ROAD_WIDTH_LOCAL = 1.0f;

    public static float roadWidthByLength(double length) {
        if (length >= 150) {
            return ROAD_WIDTH_MAIN;
        }
        if (length >= 100) {
            return ROAD_WIDTH_SECONDARY;
        }
        return ROAD_WIDTH_LOCAL;
    }

    // -- Map colors --
    public static final Color ROAD_DEFAULT = new Color(0xD5, 0xD8, 0xDD);
    public static final Color ROAD_QUERY = new Color(0x1A, 0x6B, 0xC0);
    public static final Color ROAD_DISTANCE = new Color(0x2E, 0xA8, 0x46);
    public static final Color ROAD_TIME = new Color(0xD6, 0x36, 0x2E);
    public static final Color POINT_DEFAULT = new Color(0x55, 0x5A, 0x63);
    public static final Color POINT_START = new Color(0x2E, 0xA8, 0x46);
    public static final Color POINT_END = new Color(0xD6, 0x36, 0x2E);
    public static final Color TRAFFIC_SMOOTH = new Color(0x2E, 0xA8, 0x46);
    public static final Color TRAFFIC_MODERATE = new Color(0xEC, 0xC4, 0x23);
    public static final Color TRAFFIC_CONGESTED = new Color(0xD6, 0x36, 0x2E);

    // -- Shadows --
    public static final Color SHADOW_COLOR = new Color(0x00, 0x00, 0x00, 18);

    // -- Fonts --
    public static final Font FONT_12 = new Font("Microsoft YaHei", Font.PLAIN, 12);
    public static final Font FONT_13 = new Font("Microsoft YaHei", Font.PLAIN, 13);
    public static final Font FONT_14 = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font FONT_16 = new Font("Microsoft YaHei", Font.BOLD, 16);
    public static final Font FONT_20 = new Font("Microsoft YaHei", Font.BOLD, 20);

    // -- Dimensions --
    public static final int SPACING_SMALL = 4;
    public static final int SPACING_MEDIUM = 8;
    public static final int SPACING_LARGE = 12;
    public static final int SPACING_XLARGE = 16;
    public static final int CONTROL_PANEL_WIDTH = 290;
    public static final int NAV_BAR_HEIGHT = 44;
    public static final int STATUSBAR_HEIGHT = 26;
    public static final int BUTTON_ARC = 6;
    public static final int CARD_ARC = 10;
    public static final int FIELD_ARC = 6;
    public static final int SHADOW_SIZE = 4;

    public static Color darken(Color color, double factor) {
        int r = (int) (color.getRed() * (1 - factor));
        int g = (int) (color.getGreen() * (1 - factor));
        int b = (int) (color.getBlue() * (1 - factor));
        return new Color(Math.max(0, r), Math.max(0, g), Math.max(0, b));
    }
}
