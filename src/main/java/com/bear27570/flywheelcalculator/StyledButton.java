// StyledButton.java
package com.bear27570.flywheelcalculator;

import javax.swing.*;
import java.awt.*;

public class StyledButton extends JButton {
    private static final Color BUTTON_BLUE = new Color(0x073A8F);
    private static final Color BUTTON_HOVER_BLUE = new Color(0x244F91);

    public StyledButton(String text) {
        super(text);

        // --- 核心：告诉 L&F 不要绘制背景或边框，我们自己来 ---
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);

        // 设置基本样式
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 根据按钮状态选择颜色
        if (getModel().isPressed()) {
            g2.setColor(BUTTON_HOVER_BLUE);
        } else if (getModel().isRollover()) {
            g2.setColor(BUTTON_HOVER_BLUE);
        } else {
            g2.setColor(BUTTON_BLUE);
        }

        // 绘制我们自己的纯色圆角矩形背景
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();

        // 让父类（JButton）继续绘制文本等内容
        super.paintComponent(g);
    }
}