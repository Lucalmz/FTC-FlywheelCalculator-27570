// LauncherCalculator.java
package com.bear27570.flywheelcalculator;

import static java.lang.Math.*;

public class LauncherCalculator {

    public static final double G = 9.81;
    public static final double GOAL_HEIGHT_M = 0.9845;
    public static final double MAX_FIELD_DISTANCE = 5.0;

    public static CalculationResult calculate(String launcherType, double diameter, double rpm, double height, double compression) {
        // --- STEP 1: 初始参数有效性检查 ---
        if (isInvalid(diameter, rpm, height) || diameter <= 0 || rpm < 0 || height < 0) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 直径、转速和高度必须是有效的正数。");
        }
        if ("单飞轮+弧面".equals(launcherType) && (isInvalid(compression) || compression <= 0)) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 单飞轮模型需要一个有效的正压缩量。");
        }

        // --- STEP 2: 计算出射速度 ---
        double vExit = launcherType.equals("单飞轮+弧面") ?
                calculateSingleFlywheelExitVelocity(diameter, rpm, compression) :
                (diameter / 2000.0) * (2 * PI * rpm / 60.0);

        if (isInvalid(vExit) || vExit <= 1e-6) { // 使用一个小的阈值来判断速度是否有效
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 计算出的出射速度为零或无效，请检查输入。");
        }
        String warning = launcherType.equals("单飞轮+弧面") ? "⚠ 能量模型为估算值。" : "双飞轮模型假设能量传递理想。";

        // --- STEP 3: 弹道可行性检查 ---
        double deltaH = GOAL_HEIGHT_M - height;
        double v_sq = vExit * vExit;
        double energy_check = v_sq - 2 * G * deltaH;

        if (energy_check < 0) {
            return new CalculationResult(false, vExit, 0, 0, 0, "不可行: 速度不足以将球抛到目标高度。");
        }

        // --- STEP 4: 范围计算（带全面检查） ---
        double maxRange = (vExit / G) * sqrt(energy_check);
        if (isInvalid(maxRange)) {
            return new CalculationResult(false, vExit, 0, 0, 0, "错误: 计算最大射程时发生数学错误。");
        }

        double minRange = calculateMinRange(deltaH, v_sq);
        if (isInvalid(minRange)) {
            return new CalculationResult(false, vExit, 0, 0, 0, "错误: 计算最小射程时发生数学错误。");
        }

        if (minRange > MAX_FIELD_DISTANCE) {
            return new CalculationResult(false, vExit, minRange, maxRange, 0, "不可行: 速度太快，最小射程已超出场地。");
        }

        // --- STEP 5: 推荐仰角计算 ---
        double recommendedRange = minRange + (maxRange - minRange) * 0.5; // 取中间值
        double recommendedAngle = calculateLaunchAngle(vExit, recommendedRange, deltaH);

        if (isInvalid(recommendedAngle)) {
            return new CalculationResult(false, vExit, minRange, maxRange, 0, "错误: 无法为推荐距离计算有效仰角。");
        }

        return new CalculationResult(true, vExit, minRange, maxRange, recommendedAngle, warning);
    }

    private static double calculateSingleFlywheelExitVelocity(double d, double rpm, double c) {
        double vWheel = (d / 2000.0) * (2 * PI * rpm / 60.0);
        double availableWork = ((PI * d / 1000.0) * (1.0/6.0)) * (0.7 * (5000.0) * (c / 1000.0));
        if (availableWork <= 0) return 0.0;
        double vFromWork = sqrt(2 * availableWork / 0.071);
        return min(vFromWork, vWheel);
    }

    private static double calculateMinRange(double deltaH, double v_sq) {
        if (deltaH <= 0) return 0.0;
        double term_inside_asin = 2 * G * deltaH / v_sq;
        if (term_inside_asin >= 1.0) return 0.0;
        return deltaH / tan(asin(sqrt(term_inside_asin)));
    }

    private static double calculateLaunchAngle(double v, double x, double h) {
        if (x <= 1e-6) return 90.0;
        double v2 = v * v;
        double discriminant = v2*v2 - G * (G * x*x + 2 * h * v2);
        if (discriminant < 0) return Double.NaN;
        return toDegrees(atan((v2 - sqrt(discriminant)) / (G * x)));
    }

    private static boolean isInvalid(double... vals) {
        for (double val : vals) {
            if (Double.isNaN(val) || Double.isInfinite(val)) return true;
        }
        return false;
    }
}