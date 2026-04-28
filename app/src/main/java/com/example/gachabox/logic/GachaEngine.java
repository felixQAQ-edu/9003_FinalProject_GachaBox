package com.example.gachabox.logic;

import com.example.gachabox.model.GachaItem;
import java.util.Random;

public class GachaEngine {
    private int pityCounter = 0;
    private final int PITY_THRESHOLD = 30;
    private Random random = new Random();

    public GachaItem pull() {
        pityCounter++;

        if (pityCounter >= PITY_THRESHOLD) {
            pityCounter = 0; // 触发保底后清零
            return new GachaItem("Godzilla", "Secret");
        }

        int roll = random.nextInt(100);

        if (roll < 1) {
            pityCounter = 0;
            return new GachaItem("Godzilla", "Secret");
        }
        else if (roll < 15) {
            return new GachaItem("Husky", "Rare");
        }
        else {
            return new GachaItem("Golden Retriever", "Common");
        }
    }


    public int getPityCounter() {
        return pityCounter;
    }
}