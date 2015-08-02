package com.teamtreehouse.funfacts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Kevin Parks on 7/24/2015.
 */
public class FactBook {
    private List<String> mFacts;
    private Random mRandom;
    private String[] mFactStrings = {
        "Ants stretch when they wake up in the morning.",
        "Ostriches can run faster than horses.",
        "Olympic gold medals are actually made mostly of silver.",
        "You are born with 300 bones; by the time you are an adult you will have 206.",
        "It takes about 8 minutes for light from the Sun to reach Earth.",
        "Some bamboo plants can grow almost a meter in just one day.",
        "The state of Florida is bigger than England.",
        "Some penguins can leap 2-3 meters out of the water.",
        "On average, it takes 66 days to form a new habit.",
        "Mammoths still walked the earth when the Great Pyramid was being built." };

    public FactBook() {
        mFacts = new ArrayList<String>(Arrays.asList(mFactStrings));
        mRandom = new Random();
    }

    public String getFact() {
        return mFacts.get(mRandom.nextInt(mFacts.size()));
    }

}
