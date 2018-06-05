package com.incomrecycle.prms.rvm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incomrecycle.common.SysConfig;
import com.incomrecycle.common.utils.PropUtils;

import java.util.Date;
import java.util.Properties;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by 9second on 2018/6/5.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String rootDateTime = Long.valueOf(new Date().getTime()).toString();
        Properties externalProp = new Properties();
        externalProp.put("BOOT.TIME", rootDateTime);
        PropUtils.update(SysConfig.get("EXTERNAL.FILE"), externalProp);
        SysConfig.set(externalProp);
        Intent ootStartIntent = new Intent(context, RVMActivity.class);
        ootStartIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(ootStartIntent);
    }
}

