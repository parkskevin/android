package com.teamtreehouse.funfacts;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Kevin Parks on 7/24/2015.
 */
public class LayoutColor {
    public String[] mColorStrings = {
            "#39add1", // light blue
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#637a91", // dark gray
            "#f092b0", // pink
            "#b7c0c7"  // light gray
    };
    private Random mRandom;
    private List<String> mColors;
    private Color mColor;
    
    public LayoutColor() {
        mRandom = new Random();
        mColors = new ArrayList<String>(Arrays.asList(mColorStrings));
    }

    public int getColor() throws IllegalArgumentException {
        int retColor =mColor.parseColor(mColors.get(mRandom.nextInt(mColors.size())));
        return retColor;
    }
}
