package com.herbal.doh.tflitecamerademo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

public class Project_Info extends AppCompatActivity {

    ImageButton button;
    TextView label1, label2, label3,label4,label5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);

        button = (ImageButton) findViewById(R.id.back);

        button.setOnClickListener(v -> {
            Intent i = new Intent(Project_Info.this,About.class);
            startActivity(i);
        });
    }
}
