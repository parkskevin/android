package com.kparks.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Kevin Parks on 9/3/2015.
 */
public class RibbitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "lLHe5j3Tzi0Q40kgnYDmdsL79lwOdJKCkajPe7ru", "WhKHBGIiVEa5Fk9lJoDiqxdXjJ3MLmJkGiEez9M1");
    }
}
