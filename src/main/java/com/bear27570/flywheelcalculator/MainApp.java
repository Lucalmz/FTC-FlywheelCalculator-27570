// MainApp.java
package com.bear27570.flywheelcalculator;

import javax.swing.*;
import java.awt.*;

public class MainApp {
    private JFrame frame;
    private FieldPanel fieldPanel;
    private JTextArea resultArea;
    private CustomComboBox<String> launcherTypeCombo;
    // --- 关键改动 1: 声明新的输入框 ---
    private JTextField flywheelDiameterField, motorRpmField, launchHeightField, compressionField, frictionCoeffField, springConstField;
    // --- 关键改动 2: 声明新的标签 ---
    private JLabel compressionLabel, frictionCoeffLabel, springConstLabel;


    public void createAndShowGUI() {
        frame = new JFrame("FTC 飞轮发射器弹道计算器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入参数"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        launcherTypeCombo = new CustomComboBox<>(new String[]{"单飞轮+弧面", "双飞轮"});
        flywheelDiameterField = new JTextField("100", 10);
        motorRpmField = new JTextField("3000", 10);
        launchHeightField = new JTextField("0.3", 10);
        compressionField = new JTextField("4.0", 10);
        frictionCoeffField = new JTextField("0.7", 10);
        // --- 关键改动 3: 实例化新的输入框 ---
        springConstField = new JTextField("5000.0", 10); // 弹性系数 k (N/m)

        StyledButton calculateButton = new StyledButton("计算");

        // --- 布局 (已更新) ---
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(new JLabel("发射器类型:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(launcherTypeCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(new JLabel("飞轮直径 (mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(flywheelDiameterField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; inputPanel.add(new JLabel("电机转速 (RPM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; inputPanel.add(motorRpmField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; inputPanel.add(new JLabel("发射口高度 (m):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; inputPanel.add(launchHeightField, gbc);

        // --- 单飞轮相关参数 ---
        compressionLabel = new JLabel("压缩量 (mm):");
        gbc.gridx = 0; gbc.gridy = 4; inputPanel.add(compressionLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; inputPanel.add(compressionField, gbc);

        frictionCoeffLabel = new JLabel("摩擦系数 (μ):");
        gbc.gridx = 0; gbc.gridy = 5; inputPanel.add(frictionCoeffLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 5; inputPanel.add(frictionCoeffField, gbc);

        // --- 关键改动 4: 添加新的标签和输入框到面板 ---
        springConstLabel = new JLabel("弹性系数 (k, N/m):");
        gbc.gridx = 0; gbc.gridy = 6; inputPanel.add(springConstLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 6; inputPanel.add(springConstField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.insets = new Insets(15, 5, 8, 5); // 按钮位置下移
        inputPanel.add(calculateButton, gbc);

        // ... (其他UI布局代码保持不变) ...
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

        launcherTypeCombo.addActionListener(e -> toggleSingleFlywheelParams());
        calculateButton.addActionListener(e -> calculateAndDisplay());
        toggleSingleFlywheelParams();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * 更新辅助方法，以控制所有单飞轮特有参数的可见性
     */
    private void toggleSingleFlywheelParams() {
        boolean isSingleFlywheel = "单飞轮+弧面".equals(launcherTypeCombo.getSelectedItem());
        compressionField.setVisible(isSingleFlywheel);
        compressionLabel.setVisible(isSingleFlywheel);
        frictionCoeffField.setVisible(isSingleFlywheel);
        frictionCoeffLabel.setVisible(isSingleFlywheel);
        // --- 关键改动 5: 控制新组件的可见性 ---
        springConstField.setVisible(isSingleFlywheel);
        springConstLabel.setVisible(isSingleFlywheel);
    }

    private void calculateAndDisplay() {
        resultArea.setText("正在计算中...");
        SwingUtilities.invokeLater(() -> {
            try {
                // 读取所有输入值
                double diameter = Double.parseDouble(flywheelDiameterField.getText());
                double rpm = Double.parseDouble(motorRpmField.getText());
                double height = Double.parseDouble(launchHeightField.getText());
                double compression = Double.parseDouble(compressionField.getText());
                double frictionCoeff = Double.parseDouble(frictionCoeffField.getText());
                // --- 关键改动 6: 读取新的弹性系数值 ---
                double springConst = Double.parseDouble(springConstField.getText());

                String type = (String) launcherTypeCombo.getSelectedItem();

                // --- 关键改动 7: 将所有变量传递给计算方法 ---
                CalculationResult result = LauncherCalculator.calculate(type, diameter, rpm, height, compression, frictionCoeff, springConst);

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