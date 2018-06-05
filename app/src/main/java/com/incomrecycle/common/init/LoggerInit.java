package com.incomrecycle.common.init;

import android.content.Context;

import com.google.code.microlog4android.config.PropertyConfigurator;
import java.util.HashMap;

public class LoggerInit implements InitInterface {
    private static String micrologfile = null;

    @Override
    public void Init(Context context, HashMap hashMap) {
        if (micrologfile == null) {
            if (hashMap != null) {
                micrologfile = (String) hashMap.get("microlog.properties");
            }
            if (micrologfile == null) {
                micrologfile = "microlog.properties";
            }
            PropertyConfigurator.getConfigurator(context).configure(micrologfile);
        }
    }
}
