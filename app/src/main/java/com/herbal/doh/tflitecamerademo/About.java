package com.herbal.doh.tflitecamerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;

public class About extends AppCompatActivity {
    private ImageButton button;
    private Button btn_dev;
    private Button btn_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        button = (ImageButton) findViewById(R.id.back);


        button.setOnClickListener(v -> {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        btn_dev = (Button) findViewById(R.id.btn_dev);
        btn_dev.setOnClickListener(v -> {
            Intent i = new Intent(About.this,Developers.class);
            startActivity(i);
        });

        btn_info = (Button) findViewById(R.id.btn_proj);
        btn_info.setOnClickListener(v -> {
            Intent i = new Intent(About.this,Project_Info.class);
            startActivity(i);
        });

    }
}
