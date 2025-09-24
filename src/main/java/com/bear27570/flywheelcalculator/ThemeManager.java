// ThemeManager.java
package com.bear27570.flywheelcalculator;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Enumeration;

public class ThemeManager {

    public static Font getCrossPlatformFont() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new Font("Microsoft YaHei UI", Font.PLAIN, 14);
        } else if (os.contains("mac")) {
            return new Font("PingFang SC", Font.PLAIN, 14);
        } else {
            return new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    public static void applyBlueBlackTheme() {
        Color darkGray = new Color(0x2B2B2B);
        Color mediumGray = new Color(0x3C3F41);
        Color lightGray = new Color(0x555555);
        Color fontColor = new Color(0xBBBBBB);
        Color accentBlue = new Color(0x3574F0);

        // --- 全局设置 (现在简单多了) ---
        UIManager.put("Panel.background", darkGray);
        UIManager.put("Label.foreground", fontColor);
        UIManager.put("TitledBorder.titleColor", fontColor);
        UIManager.put("TitledBorder.border", new LineBorder(lightGray));

        // --- 文本组件样式 (保持不变) ---
        UIManager.put("TextField.background", mediumGray);
        UIManager.put("TextField.foreground", fontColor);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                new LineBorder(lightGray), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        UIManager.put("TextArea.background", mediumGray);
        UIManager.put("TextArea.foreground", fontColor);
        UIManager.put("TextArea.caretForeground", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", accentBlue);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.border", new LineBorder(lightGray));
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("TextArea.caretForeground", Color.WHITE);

        // --- 其他UI组件 ---
        UIManager.put("ScrollBar.background", darkGray);
        UIManager.put("ScrollBar.thumb", lightGray);
        UIManager.put("ScrollBar.track", darkGray);
        UIManager.put("ScrollPane.border", new LineBorder(lightGray));
        UIManager.put("SplitPane.background", darkGray);
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());

        // --- 全局应用字体 ---
        setUIFont(new javax.swing.plaf.FontUIResource(getCrossPlatformFont()));
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }
}