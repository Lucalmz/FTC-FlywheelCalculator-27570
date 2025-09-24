// LauncherCalculator.java
package com.bear27570.flywheelcalculator;

import static java.lang.Math.*;

public class LauncherCalculator {

    // --- 物理常量 ---
    public static final double G = 9.81; // m/s^2
    public static final double GOAL_HEIGHT_M = 0.9845;
    public static final double MAX_FIELD_DISTANCE = 5.0;

    // --- 经验参数 (这些是模型中的主要假设，可根据实际测试微调) ---
    private static final double BALL_MASS_KG = 0.071;         // 球质量 (kg)
    private static final double BALL_EFFECTIVE_K = 5000.0;     // 球的有效弹性系数 (N/m), 估算值
    private static final double FLYWHEEL_MU = 0.7;             // 飞轮与球之间的动摩擦系数, 估算值

    /**
     * 主计算函数，调用相应的模型。
     */
    public static CalculationResult calculate(String launcherType, double diameter, double rpm, double height, double compression) {
        // ... (输入验证部分保持不变) ...
        if (isInvalid(diameter, rpm, height) || diameter <= 0 || rpm < 0 || height < 0) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 直径、转速和高度必须是有效的正数。");
        }
        if ("单飞轮+弧面".equals(launcherType) && (isInvalid(compression) || compression <= 0)) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 单飞轮模型需要一个有效的正压缩量。");
        }

        // --- STEP 1: 根据模型计算出射速度 ---
        double vExit;
        String warning;
        if ("单飞轮+弧面".equals(launcherType)) {
            vExit = calculateSingleFlywheelExitVelocity(diameter, rpm, compression);
            warning = "⚠ 能量模型基于估算的物理参数 (k, μ)。";
        } else {
            // 双飞轮模型（理想情况）
            vExit = (diameter / 2000.0) * (2 * PI * rpm / 60.0);
            warning = "双飞轮模型假设能量传递理想且无打滑。";
        }

        if (isInvalid(vExit) || vExit <= 1e-6) {
            return new CalculationResult(false, 0, 0, 0, 0, "错误: 计算出的出射速度为零或无效，请检查输入。");
        }

        // --- STEP 2: 弹道计算 (这部分物理学是准确的，保持不变) ---
        double deltaH = GOAL_HEIGHT_M - height;
        double v_sq = vExit * vExit;
        double energy_check = v_sq - 2 * G * deltaH;

        if (energy_check < 0) {
            return new CalculationResult(false, vExit, 0, 0, 0, "不可行: 速度不足以将球抛到目标高度。");
        }

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

        double recommendedRange = minRange + (maxRange - minRange) * 0.5;
        double recommendedAngle = calculateLaunchAngle(vExit, recommendedRange, deltaH);

        if (isInvalid(recommendedAngle)) {
            return new CalculationResult(false, vExit, minRange, maxRange, 0, "错误: 无法为推荐距离计算有效仰角。");
        }

        return new CalculationResult(true, vExit, minRange, maxRange, recommendedAngle, warning);
    }

    /**
     * [重构] 使用更精确的物理模型计算单飞轮出射速度
     * @param diameterMm 飞轮直径 (mm)
     * @param rpm 电机转速 (RPM)
     * @param compressionMm 压缩量 (mm)
     * @return 计算出的球的出射速度 (m/s)
     */
    private static double calculateSingleFlywheelExitVelocity(double diameterMm, double rpm, double compressionMm) {
        // --- 1. 基本参数转换 ---
        double flywheelRadiusM = diameterMm / 2000.0;
        double compressionM = compressionMm / 1000.0;
        double vWheel = flywheelRadiusM * (2 * PI * rpm / 60.0); // 飞轮表面线速度

        // --- 2. [改进] 动态计算接触角和弧长 ---
        // 通过几何关系计算接触半角 (theta_half)
        // 模型: 飞轮半径R, 压缩量c. 形成直角三角形，斜边为R, 一直角边为R-c
        // cos(theta_half) = (R - c) / R
        if (compressionM >= flywheelRadiusM) { // 防止 acos 的参数大于1
            compressionM = flywheelRadiusM * 0.99;
        }
        double contactAngleHalfRad = acos((flywheelRadiusM - compressionM) / flywheelRadiusM);
        double contactAngleRad = 2 * contactAngleHalfRad; // 总接触角
        double arcLengthM = flywheelRadiusM * contactAngleRad; // 实际接触弧长

        if (isInvalid(arcLengthM) || arcLengthM <= 0) {
            return 0.0; // 如果没有接触，则速度为0
        }

        // --- 3. [改进] 使用平均法向力计算摩擦功 ---
        // W_friction = F_friction_avg * d = (μ * F_normal_avg) * arcLength
        // 对于线性弹簧，平均力是最大力的一半: F_avg = 1/2 * F_max
        double maxNormalForce = BALL_EFFECTIVE_K * compressionM;
        double avgNormalForce = 0.5 * maxNormalForce;
        double workDoneByFriction = FLYWHEEL_MU * avgNormalForce * arcLengthM;

        // --- 4. 通过功能定理计算出射速度 ---
        // Work = ΔKineticEnergy => W = 1/2 * m * v^2
        if (workDoneByFriction <= 0) return 0.0;
        double vFromWork = sqrt(2 * workDoneByFriction / BALL_MASS_KG);

        // --- 5. 最终速度不能超过飞轮线速度 ---
        // 如果摩擦功足够大，球会被加速到与飞轮相同的速度；否则，速度由摩擦功决定。
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