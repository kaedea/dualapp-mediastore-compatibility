/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.mediastore.dualappcompat.home;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoPagerAdapter extends FragmentStatePagerAdapter {

    public DemoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return DemoListFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return DemoProvider.demos.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return DemoProvider.demos.keyAt(position);
    }
}
