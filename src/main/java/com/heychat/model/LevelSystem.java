package com.heychat.model;

public class LevelSystem {
    // 定义每个等级所需的经验值
    private static final int[] LEVEL_EXPERIENCE = {
        0,      // 1级
        50,     // 2级
        200,    // 3级
        500,    // 4级
        1000,   // 5级
        3000,   // 6级
        6000,   // 7级
        15000,  // 8级
        30000,  // 9级
        50000   // 10级
    };

    // 获取当前等级
    public static int calculateLevel(int experience) {
        for (int i = LEVEL_EXPERIENCE.length - 1; i >= 0; i--) {
            if (experience >= LEVEL_EXPERIENCE[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    // 获取下一级所需经验值
    public static int getNextLevelExp(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= LEVEL_EXPERIENCE.length) {
            return -1;
        }
        return LEVEL_EXPERIENCE[currentLevel];
    }

    // 获取当前等级的经验值范围
    public static int getCurrentLevelExp(int currentLevel) {
        if (currentLevel <= 1) {
            return 0;
        }
        return LEVEL_EXPERIENCE[currentLevel - 2];
    }

    // 检查是否升级
    public static boolean checkLevelUp(int oldExp, int newExp) {
        return calculateLevel(oldExp) < calculateLevel(newExp);
    }

    // 获取升级提示信息
    public static String getLevelUpMessage(int newLevel) {
        return String.format("恭喜升级到 %d 级！", newLevel);
    }
} 