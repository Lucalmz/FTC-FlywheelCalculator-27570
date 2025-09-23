// FieldPanel.java
package com.bear27570.flywheelcalculator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class FieldPanel extends JPanel {

    private BufferedImage backgroundImage;
    private CalculationResult lastResult;

    // 场地尺寸和目标点
    private static final double FIELD_WIDTH_M = 3.66;
    private static final double FIELD_HEIGHT_M = 3.66;
    private static final double TARGET_X_M = 0.1;
    private static final double TARGET_Y_M = 0.1;

    public FieldPanel() {
        String imagePath = "/Field.png";
        try (InputStream is = FieldPanel.class.getResourceAsStream(imagePath)) {
            if (is == null) throw new IOException("无法在 resources 文件夹中找到图片: " + imagePath);
            backgroundImage = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("场地图片加载失败: " + e.getMessage());
            backgroundImage = null;
        }
    }

    public void updateLaunchData(CalculationResult result) {
        this.lastResult = result;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景和目标点
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.drawString("场地图片 (Field.png) 加载失败", 50, 50);
        }
        Point targetPx = metersToPixels(TARGET_X_M, TARGET_Y_M);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(targetPx.x - 10, targetPx.y, targetPx.x + 10, targetPx.y);
        g2d.drawLine(targetPx.x, targetPx.y - 10, targetPx.x, targetPx.y + 10);

        if (lastResult == null || !lastResult.isFeasible()) {
            return;
        }

        drawShootingRanges(g2d, targetPx);
    }

    private void drawShootingRanges(Graphics2D g2d, Point targetPx) {
        double maxRangePx = metersToPixelsLength(lastResult.getMaxRange());
        double minRangePx = metersToPixelsLength(lastResult.getMinRange());
        double recRangePx = metersToPixelsLength(lastResult.getRecommendedRange());

        // 绘制最大射程半圆弧 (橙色虚线)
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{9}, 0.0f));
        g2d.draw(new Arc2D.Double(targetPx.x - maxRangePx, targetPx.y - maxRangePx, maxRangePx * 2, maxRangePx * 2, 180, 180, Arc2D.OPEN));

        // 绘制最小射程半圆弧 (红色实线)
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Arc2D.Double(targetPx.x - minRangePx, targetPx.y - minRangePx, minRangePx * 2, minRangePx * 2, 180, 180, Arc2D.OPEN));

        // 绘制推荐射击位置弧线 (青色实线)
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(new Arc2D.Double(targetPx.x - recRangePx, targetPx.y - recRangePx, recRangePx * 2, recRangePx * 2, 180, 180, Arc2D.OPEN));

        // --- 绘制图例 (样式更新) ---
        // 背景改为深灰色
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(10, getHeight() - 85, 160, 75);

        // 标题用白色
        g2d.setColor(Color.WHITE);
        g2d.drawString("图例:", 20, getHeight() - 65);

        // 文字颜色与线条颜色对应
        g2d.setColor(Color.ORANGE);
        g2d.drawString("— — 最大射程", 25, getHeight() - 50);

        g2d.setColor(Color.RED);
        g2d.drawString("——— 最小射程", 25, getHeight() - 35);

        g2d.setColor(Color.CYAN);
        g2d.drawString("——— 推荐射击弧线", 25, getHeight() - 20);
    }

    // 坐标转换 (逻辑不变)
    private Point metersToPixels(double x_m, double y_m) {
        int px = (int) ((x_m / FIELD_WIDTH_M) * getWidth());
        int py = (int) ((y_m / FIELD_HEIGHT_M) * getHeight());
        return new Point(px, py);
    }
    private double metersToPixelsLength(double length_m) {
        double scaleX = getWidth() / FIELD_WIDTH_M;
        double scaleY = getHeight() / FIELD_HEIGHT_M;
        return length_m * ((scaleX + scaleY) / 2.0);
    }
}