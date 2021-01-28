package com.ybs.myroute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class BatteryReceiver extends BroadcastReceiver
{
    private int level;
    private int LOW_BATTERY = 15;
    private Boolean shown = false;
    @Override
    public void onReceive(final Context context, final Intent intent) {

        level = intent.getIntExtra("level", -1);

        if(!shown && level <= LOW_BATTERY && level != -1)
        {

            Toast.makeText(context, "Low battery: "+level, Toast.LENGTH_SHORT).show();
            shown=true;
        }
        if(level >LOW_BATTERY )
            shown=false;

    }

}
