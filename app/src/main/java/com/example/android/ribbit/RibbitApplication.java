package com.example.android.ribbit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Carlos on 2015-01-29.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "qwMKM0BASF5UkMQPdRX54KWp3cpCTHbsONlkPUNQ", "Tc4qXUxnnhTcYAGKWgV4rgPS483HGN4W9PTd0fws");

    }
}
