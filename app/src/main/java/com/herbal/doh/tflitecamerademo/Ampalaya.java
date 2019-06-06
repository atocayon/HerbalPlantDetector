package com.herbal.doh.tflitecamerademo;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

public class Ampalaya extends AppCompatActivity {

    private TabLayout tabLayout;
    private AppBarLayout appBarLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ampalaya);

        ImageButton button = (ImageButton) findViewById(R.id.back);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(Ampalaya.this,Manual.class);
            startActivity(intent);
        });

        tabLayout = (TabLayout) findViewById(R.id.tablayout_id);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbarId);
        viewPager = (ViewPager) findViewById(R.id.viewpager_id);

        AmpalayaPagerAdapter  adapter = new AmpalayaPagerAdapter(getSupportFragmentManager());
        //Adding Fragments
        adapter.AddFragment(new AmpalayaInfo(), "Information");
        adapter.AddFragment(new AmpalayaPreparation(),"Preparation & Use");
        //Adapter Setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
