package com.example.gachabox.logic;

import com.example.gachabox.model.GachaItem;
import java.util.Random;

public class GachaEngine {
    private int pityCounter = 0;
    private final int PITY_THRESHOLD = 30; // 30次保底
    private Random random = new Random();

    public GachaItem pull() {
        pityCounter++;

        // 1. 优先触发保底机制
        if (pityCounter >= PITY_THRESHOLD) {
            pityCounter = 0; // 触发保底后清零
            return new GachaItem("Godzilla", "Secret");
        }

        // 2. 正常加权随机算法 (0-99的随机数)
        int roll = random.nextInt(100);

        if (roll < 1) { // 0: 1% 概率出隐藏款
            pityCounter = 0; // 运气好提前抽中，保底也要清零
            return new GachaItem("Godzilla", "Secret");
        }
        else if (roll < 15) { // 1-14: 14% 概率出稀有款
            return new GachaItem("Husky", "Rare");
        }
        else { // 15-99: 85% 概率出普通款
            return new GachaItem("Golden Retriever", "Common");
        }
    }

    // 获取当前保底次数（留给前端显示用）
    public int getPityCounter() {
        return pityCounter;
    }
}