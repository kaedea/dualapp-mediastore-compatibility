/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package com.kaedea.mediastore.dualappcompat.home;


import com.kaedea.mediastore.dualappcompat.ConvertActivity;
import com.kaedea.mediastore.dualappcompat.ProfileActivity;
import com.kaedea.mediastore.dualappcompat.SaveImgActivity;
import com.kaedea.mediastore.dualappcompat.SaveReadActivity;
import com.kaedea.mediastore.dualappcompat.ShareImgActivity;

import androidx.collection.ArrayMap;

/**
 * Created by Kaede on 16/8/10.
 */
public class DemoProvider {
    public static ArrayMap<String, ActivityHolder> demos;

    static {
        demos = new ArrayMap<>();
        ActivityHolder tab1 = new ActivityHolder();

        // default demos
        tab1.addActivity("ProfileActivity", "Test WorkProfile APIs", ProfileActivity.class);
        tab1.addActivity("SaveReadActivity", "Test MediaStore save & read image", SaveReadActivity.class);
        tab1.addActivity("SaveImgActivity", "Test MediaStore save & read image 2", SaveImgActivity.class);
        tab1.addActivity("ConvertActivity", "Test MediaStore uri/file_path convert", ConvertActivity.class);
        tab1.addActivity("ShareImgActivity", "Test receive simple image sharing", ShareImgActivity.class);
        demos.put("Default", tab1);
    }
}
