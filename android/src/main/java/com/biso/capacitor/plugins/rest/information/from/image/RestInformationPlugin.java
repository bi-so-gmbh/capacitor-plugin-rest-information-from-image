package com.biso.capacitor.plugins.rest.information.from.image;

import android.util.Log;

public class RestInformationPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
