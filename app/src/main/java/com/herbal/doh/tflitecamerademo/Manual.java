package com.herbal.doh.tflitecamerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class Manual extends AppCompatActivity {
    private ListView listView;
    private ImageButton button;

    String items[] = new String[]{"1. Sambong","2. Akapulko","3. Niyog-niyogan","4. Tsaang Gubat","5. Ampalaya","6. Lagundi","7. Ulasimang Bato","8. Bawang","9. Bayabas","10. Yerba Buena"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        button = (ImageButton) findViewById(R.id.back);

        button.setOnClickListener(v -> {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i;
            if (items[position] == "1. Sambong"){
                i = new Intent(Manual.this,Sambong.class);
                startActivity(i);
            }else if(items[position] == "2. Akapulko"){
                i = new Intent(Manual.this,Akapulko.class);
                startActivity(i);
            }else if (items[position] == "3. Niyog-niyogan"){
                i = new Intent(Manual.this,NiyogNiyogan.class);
                startActivity(i);
            }else if (items[position] == "4. Tsaang Gubat"){
                i = new Intent(Manual.this,TsaangGubat.class);
                startActivity(i);
            }else if (items[position] == "5. Ampalaya"){
                i = new Intent(Manual.this,Ampalaya.class);
                startActivity(i);
            }else if (items[position] == "6. Lagundi"){
                i = new Intent(Manual.this,Lagundi.class);
                startActivity(i);
            }else if (items[position] == "7. Ulasimang Bato"){
                i = new Intent(Manual.this,UlasimangBato.class);
                startActivity(i);
            }else if (items[position] == "8. Bawang"){
                i = new Intent(Manual.this,Bawang.class);
                startActivity(i);
            }else if (items[position] == "9. Bayabas"){
                i = new Intent(Manual.this,Bayabas.class);
                startActivity(i);
            }else if (items[position] == "10. Yerba Buena"){
                i = new Intent(Manual.this,YerbaBuena.class);
                startActivity(i);
            }else{
                Toast.makeText(Manual.this,"Invalid",Toast.LENGTH_SHORT).show();
            }

        });

    }

}
