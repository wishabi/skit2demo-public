package com.wishabi.flipp.skit2demo.ui.java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wishabi.flipp.skit2demo.R;

public class JavaActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        Button storefrontButton = findViewById(R.id.storefrontCtaFromJava);
        storefrontButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(JavaActivity.this, StorefrontJavaActivity.class);
        startActivity(intent);
    }
}