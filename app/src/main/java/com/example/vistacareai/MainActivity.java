package com.example.vistacareai;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cardBMI).setOnClickListener(v ->
                startActivity(new Intent(this, BmiActivity.class)));

        findViewById(R.id.cardPeriod).setOnClickListener(v ->
                startActivity(new Intent(this, PeriodActivity.class)));

        findViewById(R.id.cardHealth).setOnClickListener(v ->
                startActivity(new Intent(this, HealthActivity.class)));

        findViewById(R.id.cardAI).setOnClickListener(v ->
                startActivity(new Intent(this, AiActivity.class)));
    }
}
