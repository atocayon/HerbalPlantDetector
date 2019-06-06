package com.herbal.doh.tflitecamerademo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class Developers extends AppCompatActivity {

    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developers);

        button = (ImageButton) findViewById(R.id.back);

        button.setOnClickListener(v -> {
            Intent i = new Intent(Developers.this,About.class);
            startActivity(i);
        });
    }
}
