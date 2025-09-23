// CustomComboBox.java
package com.bear27570.flywheelcalculator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CustomComboBox<E> extends JPanel {
    // 颜色和字体定义
    private static final Color BG_COLOR = new Color(0x3C3F41);
    private static final Color BORDER_COLOR = new Color(0x555555);
    private static final Color FONT_COLOR = new Color(0xBBBBBB);
    private static final Color POPUP_BG_COLOR = new Color(0x2B2B2B);
    private static final Color SELECTION_BG_COLOR = new Color(0x1E1F22);
    private static final Font FONT = ThemeManager.getCrossPlatformFont();

    private final JLabel selectedValueLabel;
    private final JPopupMenu popupMenu;
    private E selectedItem;
    private final List<ActionListener> listeners = new ArrayList<>();

    public CustomComboBox(E[] items) {
        super(new BorderLayout());

        this.selectedValueLabel = new JLabel();
        this.popupMenu = new JPopupMenu();

        // --- 核心：设置我们自己的外观 ---
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // --- 设置显示文本的标签 ---
        selectedValueLabel.setFont(FONT);
        selectedValueLabel.setForeground(FONT_COLOR);
        selectedValueLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        add(selectedValueLabel, BorderLayout.CENTER);

        // --- 设置箭头 ---
        JLabel arrowLabel = new JLabel(new ArrowIcon());
        arrowLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        add(arrowLabel, BorderLayout.EAST);

        // --- 配置弹出菜单 ---
        popupMenu.setBackground(POPUP_BG_COLOR);
        popupMenu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // --- 为每个选项创建菜单项 ---
        for (E item : items) {
            JMenuItem menuItem = new JMenuItem(item.toString());
            menuItem.setFont(FONT);
            menuItem.setBackground(POPUP_BG_COLOR);
            menuItem.setForeground(FONT_COLOR);
            menuItem.setOpaque(true);

            // 自定义选择时的高亮样式
            menuItem.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
                {
                    selectionBackground = SELECTION_BG_COLOR;
                    selectionForeground = Color.WHITE;
                }
            });

            menuItem.addActionListener(e -> {
                setSelectedItem(item);
                fireActionEvent();
            });
            popupMenu.add(menuItem);
        }

        // --- 添加事件监听器来显示菜单 ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 设置弹出菜单的宽度与组件一致
                popupMenu.setPopupSize(getWidth(), popupMenu.getPreferredSize().height);
                popupMenu.show(CustomComboBox.this, 0, getHeight());
            }
        });

        // 设置初始选项
        if (items != null && items.length > 0) {
            setSelectedItem(items[0]);
        }
    }

    public E getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(E item) {
        this.selectedItem = item;
        this.selectedValueLabel.setText(item.toString());
    }

    public void addActionListener(ActionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void fireActionEvent() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "comboBoxChanged");
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    // 用于绘制箭头的内部类
    private static class ArrowIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(FONT_COLOR);
            int y_offset = y + 2;
            int[] xPoints = {x, x + 4, x + 8};
            int[] yPoints = {y_offset, y_offset + 4, y_offset};
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 12; }
        @Override public int getIconHeight() { return 8; }
    }
}