/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.mediastore.dualappcompat.home;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.kaedea.mediastore.dualappcompat.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.tablayout);
        ViewPager viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        viewPager.setAdapter(new DemoPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);
    }
}
