// LauncherCalculator.java
package com.bear27570.flywheelcalculator;

import static java.lang.Math.*;

public class LauncherCalculator {

    // --- 物理常量 ---
    public static final double G = 9.81;
    public static final double GOAL_HEIGHT_M = 0.9845;
    public static final double MAX_FIELD_DISTANCE = 5.0;

    // --- 经验参数 (现在只剩球的质量是固定的) ---
    private static final double BALL_MASS_KG = 0.071;

    /**
     * 主计算函数，现在接收弹性系数作为参数
     */
    public static CalculationResult calculate(String launcherType, double diameter, double rpm, double height, double compression, double frictionCoeff, double springConst) {
        if ("单飞轮+弧面".equals(launcherType) && (isInvalid(compression, frictionCoeff, springConst) || compression <= 0 || frictionCoeff <= 0 || springConst <= 0)) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 单飞轮模型需要所有参数都为有效的正数。");
        }

        double vExit;
        String warning;
        if ("单飞轮+弧面".equals(launcherType)) {
            // --- 关键改动 1: 将弹性系数传递给计算方法 ---
            vExit = calculateSingleFlywheelExitVelocity(diameter, rpm, compression, frictionCoeff, springConst);
            warning = "⚠ 能量模型基于输入的物理参数。";
        } else {
            vExit = (diameter / 2000.0) * (2 * PI * rpm / 60.0);
            warning = "双飞轮模型假设能量传递理想且无打滑。";
        }

        if (isInvalid(vExit) || vExit <= 1e-6) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 计算出的出射速度为零或无效，请检查输入。");
        }

        // --- 弹道计算 (保持不变) ---
        double deltaH = GOAL_HEIGHT_M - height;
        double v_sq = vExit * vExit;
        double energy_check = v_sq - 2 * G * deltaH;
        if (energy_check < 0) {
            return new CalculationResult(false, vExit, 0, 0, 0, "不可行: 速度不足以将球抛到目标高度。");
        }

        // ... (后续的弹道计算逻辑完全保持不变) ...
        double maxRange = (vExit / G) * sqrt(energy_check);
        double minRange = calculateMinRange(deltaH, v_sq);
        if (minRange > MAX_FIELD_DISTANCE) {
            return new CalculationResult(false, vExit, minRange, maxRange, 0, "不可行: 速度太快，最小射程已超出场地。");
        }
        double recommendedRange = minRange + (maxRange - minRange) * 0.5;
        double recommendedAngle = calculateLaunchAngle(vExit, recommendedRange, deltaH);
        if (isInvalid(recommendedAngle)) {
            return new CalculationResult(false, vExit, minRange, maxRange, 0, "错误: 无法为推荐距离计算有效仰角。");
        }

        return new CalculationResult(true, vExit, minRange, maxRange, recommendedAngle, warning);
    }

    /**
     * [重构] 单飞轮模型现在接收弹性系数 (springConstantK) 作为参数
     */
    private static double calculateSingleFlywheelExitVelocity(double diameterMm, double rpm, double compressionMm, double mu, double springConstantK) {
        // --- 基本参数转换 (保持不变) ---
        double flywheelRadiusM = diameterMm / 2000.0;
        double compressionM = compressionMm / 1000.0;
        double vWheel = flywheelRadiusM * (2 * PI * rpm / 60.0);

        // --- 动态计算接触角和弧长 (保持不变) ---
        if (compressionM >= flywheelRadiusM) {
            compressionM = flywheelRadiusM * 0.99;
        }
        double contactAngleHalfRad = acos((flywheelRadiusM - compressionM) / flywheelRadiusM);
        double arcLengthM = flywheelRadiusM * (2 * contactAngleHalfRad);

        if (isInvalid(arcLengthM) || arcLengthM <= 0) return 0.0;

        // --- 使用平均法向力计算摩擦功 ---
        // --- 关键改动 2: 在这里使用传入的弹性系数 `springConstantK` ---
        double maxNormalForce = springConstantK * compressionM;
        double avgNormalForce = 0.5 * maxNormalForce;
        double workDoneByFriction = mu * avgNormalForce * arcLengthM;

        // --- 通过功能定理计算出射速度 (保持不变) ---
        if (workDoneByFriction <= 0) return 0.0;
        double vFromWork = sqrt(2 * workDoneByFriction / BALL_MASS_KG);

        // --- 最终速度不能超过飞轮线速度 (保持不变) ---
        return min(vFromWork, vWheel);
    }

    // --- 弹道计算辅助函数 (保持不变) ---
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