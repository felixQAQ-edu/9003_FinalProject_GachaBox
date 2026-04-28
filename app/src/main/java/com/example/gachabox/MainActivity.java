package com.example.gachabox;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.gachabox.database.DatabaseHelper;
import com.example.gachabox.logic.GachaEngine;
import com.example.gachabox.model.GachaItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        GachaEngine engine = new GachaEngine();
        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);

        Log.d("GachaTest", "start test...");

        for (int i = 1; i <= 35; i++) {
            GachaItem result = engine.pull();

            boolean success = dbHelper.addGachaItem(result.getName(), result.getRarity());

            if (success) {
                Log.d("GachaTest", "第 " + i + " 抽: " + result.toString() + " | 存入成功!");
            } else {
                Log.e("GachaTest", "第 " + i + " 抽存入失败!");
            }
        }
    }
}