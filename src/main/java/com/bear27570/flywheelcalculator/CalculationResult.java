// CalculationResult.java
package com.bear27570.flywheelcalculator;

public class CalculationResult {
    private final boolean isFeasible;
    private final double exitVelocity;
    private final String message;

    private final double minRange;
    private final double maxRange;
    private final double recommendedRange;
    private final double recommendedAngleDeg; // 新增：推荐仰角

    public CalculationResult(boolean isFeasible, double exitVelocity, double minRange, double maxRange, double recommendedAngleDeg, String message) {
        this.isFeasible = isFeasible;
        this.exitVelocity = exitVelocity;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.recommendedRange = (minRange + maxRange) / 2.0; // 推荐距离取中间值
        this.recommendedAngleDeg = recommendedAngleDeg;
        this.message = message;
    }

    // Getters
    public boolean isFeasible() { return isFeasible; }
    public double getExitVelocity() { return exitVelocity; }
    public String getMessage() { return message; }
    public double getMinRange() { return minRange; }
    public double getMaxRange() { return maxRange; }
    public double getRecommendedRange() { return recommendedRange; }
    public double getRecommendedAngleDeg() { return recommendedAngleDeg; }

    @Override
    public String toString() {
        if (!isFeasible) {
            return String.format(
                    "计算结果:\n" +
                            "出射速度: %.2f m/s\n" +
                            "状态: 不可行\n" +
                            "提示: %s",
                    exitVelocity, message
            );
        }
        return String.format(
                "计算结果:\n" +
                        "出射速度: %.2f m/s\n" +
                        "可行射程范围: [%.2f m, %.2f m]\n" +
                        "推荐射击距离: %.2f m\n" +
                        "对应的推荐仰角: %.1f°\n" + // 新增显示
                        "提示: %s",
                exitVelocity, minRange, maxRange, recommendedRange, recommendedAngleDeg, (message == null || message.isEmpty()) ? "所有参数在范围内均有效。" : message
        );
    }
}