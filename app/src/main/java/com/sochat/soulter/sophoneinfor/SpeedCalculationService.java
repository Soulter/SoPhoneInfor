package com.sochat.soulter.sophoneinfor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import static com.sochat.soulter.sophoneinfor.MainActivity.CHANGED;
import static com.sochat.soulter.sophoneinfor.MainActivity.INIT_X;
import static com.sochat.soulter.sophoneinfor.MainActivity.INIT_Y;


public class SpeedCalculationService extends Service {
    private WindowUtil windowUtil;
    private boolean changed=false;

    @Override
    public void onCreate() {
        super.onCreate();
        WindowUtil.initX= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_X,0);
        WindowUtil.initY= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_Y,0);
        windowUtil=new WindowUtil(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        changed=intent.getBooleanExtra(CHANGED,false);
        if(changed){
            windowUtil.onSettingChanged();
        }else{
            if(!windowUtil.isShowing()){
                windowUtil.showSpeedView();
            }
            SharedPreferencesUtils.putToSpfs(this,MainActivity.IS_SHOWN,true);
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowManager.LayoutParams params= (WindowManager.LayoutParams) windowUtil.speedView.getLayoutParams();
        SharedPreferencesUtils.putToSpfs(this, INIT_X,params.x);
        SharedPreferencesUtils.putToSpfs(this, INIT_Y,params.y);
        if(windowUtil.isShowing()){
            windowUtil.closeSpeedView();
            SharedPreferencesUtils.putToSpfs(this,MainActivity.IS_SHOWN,false);
        }

    }
}
