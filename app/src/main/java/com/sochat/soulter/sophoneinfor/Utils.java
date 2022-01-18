package com.sochat.soulter.sophoneinfor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Utils {

    public static void Toast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
