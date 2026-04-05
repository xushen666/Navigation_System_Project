package com.ds.navigation.util;

public final class InputValidator {
    private InputValidator() {
    }

    public static double parseDouble(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + "必须是数字");
        }
    }
}
