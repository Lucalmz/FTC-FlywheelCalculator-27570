// MainApp.java
package com.bear27570.flywheelcalculator;

import javax.swing.*;
import java.awt.*;

public class MainApp {
    private JFrame frame;
    private FieldPanel fieldPanel;
    private JTextArea resultArea;
    // --- 关键改动：使用我们全新的自定义下拉框 ---
    private CustomComboBox<String> launcherTypeCombo;
    private JTextField flywheelDiameterField, motorRpmField, launchHeightField, compressionField;

    public void createAndShowGUI() {
        frame = new JFrame("FTC DECODE 赛季发射器角度计算器-BY27570");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入参数"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- 关键改动：实例化我们全新的自定义组件 ---
        launcherTypeCombo = new CustomComboBox<>(new String[]{"单飞轮+弧面", "双飞轮"});
        flywheelDiameterField = new JTextField("100", 10);
        motorRpmField = new JTextField("3000", 10);
        launchHeightField = new JTextField("0.3", 10);
        compressionField = new JTextField("4.0", 10);

        // 我们仍然可以使用 StyledButton，因为它工作得很好
        StyledButton calculateButton = new StyledButton("计算");

        // --- 布局 ---
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("发射器类型:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(launcherTypeCombo, gbc);
        // ... (其他组件布局保持不变) ...
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("飞轮直径 (mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(flywheelDiameterField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("电机转速 (RPM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; inputPanel.add(motorRpmField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(new JLabel("发射口高度 (m):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; inputPanel.add(launchHeightField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; inputPanel.add(new JLabel("压缩量 (mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; inputPanel.add(compressionField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(15, 5, 8, 5);
        inputPanel.add(calculateButton, gbc);

        // ... (其他UI部分保持不变) ...
        resultArea = new JTextArea("请点击“计算”按钮生成结果...");
        resultArea.setEditable(false);
        resultArea.setFont(ThemeManager.getCrossPlatformFont());
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(250, 150));
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        frame.add(mainSplitPane, BorderLayout.CENTER);
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        fieldPanel = new FieldPanel();
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(fieldPanel);
        mainSplitPane.setDividerLocation(350);

        // --- 事件监听器 ---
        launcherTypeCombo.addActionListener(e -> {
            boolean isSingleFlywheel = "单飞轮+弧面".equals(launcherTypeCombo.getSelectedItem());
            compressionField.setEnabled(isSingleFlywheel);
        });
        calculateButton.addActionListener(e -> calculateAndDisplay());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void calculateAndDisplay() {
        resultArea.setText("正在计算中...");
        SwingUtilities.invokeLater(() -> {
            try {
                double diameter = Double.parseDouble(flywheelDiameterField.getText());
                double rpm = Double.parseDouble(motorRpmField.getText());
                double height = Double.parseDouble(launchHeightField.getText());
                double compression = Double.parseDouble(compressionField.getText());
                String type = (String) launcherTypeCombo.getSelectedItem();
                CalculationResult result = LauncherCalculator.calculate(type, diameter, rpm, height, compression);
                resultArea.setText(result.toString());
                fieldPanel.updateLaunchData(result);
            } catch (Exception ex) {
                resultArea.setText("计算错误:\n" + ex.getMessage());
                fieldPanel.updateLaunchData(null);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyBlueBlackTheme();
            new MainApp().createAndShowGUI();
        });
    }
}