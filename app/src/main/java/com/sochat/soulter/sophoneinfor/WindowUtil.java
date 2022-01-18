package com.sochat.soulter.sophoneinfor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

import static com.sochat.soulter.sophoneinfor.MainActivity.BATTERY;
import static com.sochat.soulter.sophoneinfor.MainActivity.B_TRUE;
import static com.sochat.soulter.sophoneinfor.MainActivity.DOWN;
import static com.sochat.soulter.sophoneinfor.MainActivity.NET_STATUS;
import static com.sochat.soulter.sophoneinfor.MainActivity.UP;
import static com.sochat.soulter.sophoneinfor.MainActivity.WINDOW_SIZE;


public class WindowUtil {
    public static int statusBarHeight=0;
    //记录悬浮窗的位置
    public static int initX,initY;
    private WindowManager windowManager;
    public  SpeedView speedView;
    private WindowManager.LayoutParams params;
    private Context context;


    private static int netWorkClass;
    public static int intLevel;
    public static int intScale;



    public boolean isShowing() {
        return isShowing;
    }

    public static boolean isShowing=false;


    public static int interval= 2000;//默认值
    private long preRxBytes = 0;
    private long preSeBytes = 0;
    private long rxBytes,seBytes;


    private int handlerCount;



    /*COMPLETED:如何在关闭service后关闭这个
     究极算法By Sincon :在MainActivity创建一个静态变量用于handler的what参数计数.
     当要重启服务时计数器加一,让这里的handler检测到,启动what+1的handler,再清除上一个handler.
     */
    private Handler handler=new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            calculateNetSpeed();
//            interval= Integer.valueOf(SharedPreferencesUtils.getFromSpfs(context,MainActivity.SPEED_DELAY,2000).toString());
            sendEmptyMessageDelayed(MainActivity.handlerWhatCount,interval);
            if (!MainActivity.isShow){
                handler.removeMessages(MainActivity.handlerWhatCount);
            }
            if (handlerCount != MainActivity.handlerWhatCount){
                handler.removeMessages(handlerCount);
            }

            handlerCount = MainActivity.handlerWhatCount;

        }
    };

    //改变设置时调用
    public void onSettingChanged(){

        //复选框检查并且改变
        String isUpChecked= (String) SharedPreferencesUtils.getFromSpfs(context, UP, B_TRUE);
        String isDownChecked= (String) SharedPreferencesUtils.getFromSpfs(context, DOWN, B_TRUE);
        String isBatteryChecked= (String) SharedPreferencesUtils.getFromSpfs(context, BATTERY, B_TRUE);
        String isNetStatusChecked= (String) SharedPreferencesUtils.getFromSpfs(context, NET_STATUS, B_TRUE);
        if(isUpChecked.equals("true"))
            speedView.upText.setVisibility(View.VISIBLE);
        else
            speedView.upText.setVisibility(View.GONE);
        if(isDownChecked.equals("true"))
            speedView.downText.setVisibility(View.VISIBLE);
        else
            speedView.downText.setVisibility(View.GONE);
        if(isBatteryChecked.equals("true"))
            speedView.batteryText.setVisibility(View.VISIBLE);
        else {
            speedView.batteryText.setVisibility(View.GONE);
        }
        if(isNetStatusChecked.equals("true"))
            speedView.netStatusText.setVisibility(View.VISIBLE);
        else{
            speedView.netStatusText.setVisibility(View.GONE);
        }


        int windowSize = (int)SharedPreferencesUtils.getFromSpfs(context,WINDOW_SIZE,10);
        speedView.upText.setTextSize(windowSize);
        speedView.downText.setTextSize(windowSize);
        speedView.batteryText.setTextSize(windowSize);
        speedView.netStatusText.setTextSize(windowSize);
    }

    private void calculateNetSpeed() {
        rxBytes=TrafficStats.getTotalRxBytes();//down
        seBytes=TrafficStats.getTotalTxBytes();//getTotalTxBytes得到up字节数
        double downloadSpeed=(rxBytes-preRxBytes)/2;
        double uploadSpeed=(seBytes-preSeBytes)/2;
        if(preRxBytes == 0||preSeBytes == 0){
            updateSpeed("↓ 0B/s","↑ 0B/s");
            preRxBytes=rxBytes;
            preSeBytes=seBytes;
            return;
        }
        preRxBytes=rxBytes;
        preSeBytes=seBytes;
        //根据范围决定显示单位
        String upSpeed=null;
        String downSpeed=null;

        NumberFormat df= java.text.NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(2);

        if(downloadSpeed>1024*1024){
            downloadSpeed/=(1024*1024);
            downSpeed=df.format(downloadSpeed)+"M/s";
        }else if(downloadSpeed>1024){
            downloadSpeed/=(1024);
            downSpeed=df.format(downloadSpeed)+"K/s";
        }else{
            downSpeed=df.format(downloadSpeed)+"B/s";
        }

        if(uploadSpeed>1024*1024){
            uploadSpeed/=(1024*1024);
            upSpeed=df.format(uploadSpeed)+"M/s";
        }else if(uploadSpeed>1024){
            uploadSpeed/=(1024);
            upSpeed=df.format(uploadSpeed)+"K/s";
        }else{
            upSpeed=df.format(uploadSpeed)+"B/s";
        }

        updateSpeed("↓ "+downSpeed,"↑ "+upSpeed);
    }

    public WindowUtil(Context context) {
        this.context = context;


        //注册电量广播
        context.registerReceiver(netTypeReceiver,new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
        context.registerReceiver(BatInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        speedView=new SpeedView(context);
        params=new WindowManager.LayoutParams();
        params=new WindowManager.LayoutParams();
        params.x=initX;
        params.y=initY;
        params.width=params.height=WindowManager.LayoutParams.WRAP_CONTENT;
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.gravity= Gravity.LEFT|Gravity.TOP;
        params.format= PixelFormat.TRANSPARENT;
        params.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //|WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        //设置悬浮窗可以拖拽至状态栏的位置

    }

    public void showSpeedView(){
        windowManager.addView(speedView,params);
        isShowing=true;
        preRxBytes= TrafficStats.getTotalRxBytes();
        preSeBytes=TrafficStats.getTotalTxBytes();
        handler.sendEmptyMessage(0);

    }

    public void closeSpeedView(){
        windowManager.removeView(speedView);
        context.unregisterReceiver(BatInfoReceiver);
        context.unregisterReceiver(netTypeReceiver);
        isShowing=false;
    }

    public void updateSpeed(String downSpeed,String upSpeed){
        speedView.upText.setText(upSpeed);
        speedView.downText.setText(downSpeed);
    }











    //获取电量并且显示
    public BroadcastReceiver BatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //如果捕捉到的Action是ACTION_BATTERY_CHANGED则运行onBatteryInforECEIVER()
            if (intent.ACTION_BATTERY_CHANGED.equals(action)) {
                //获得当前电量
                intLevel = intent.getIntExtra("level", 0);
                //获得手机总电量
                intScale = intent.getIntExtra("scale", 100);
                // 在下面会定义这个函数，显示手机当前电量
                int percent = intLevel * 100 / intScale;
                speedView.batteryText.setText(percent + "%");
            }
        }
    };

    public  BroadcastReceiver netTypeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT<23) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    //连上的网络类型判断：wifi还是移动网络
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        speedView.netStatusText.setText("WiFi");
                        if (!networkInfo.isAvailable()) {
                            speedView.netStatusText.setText("(x)WiFi");
                        }
                    } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                        netWorkClass = getNetWorkClass(context);
                        if (netWorkClass == Constants.NETWORK_CLASS_2_G)
                            speedView.netStatusText.setText("2G");
                        else if (netWorkClass == Constants.NETWORK_CLASS_3_G)
                            speedView.netStatusText.setText("3G");
                        else if (netWorkClass == Constants.NETWORK_CLASS_4_G)
                            speedView.netStatusText.setText("4G");
                        else
                            speedView.netStatusText.setText("?G");
                    }
            //        具体连接状态判断
                }

            }
            else {
                System.out.println("API level 大于23");
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取所有网络连接的信息
                Network[] networks;
                try {
                     networks = connMgr.getAllNetworks();
                    //通过循环将网络信息逐个取出来
                    for (int i = 0; i < networks.length; i++) {
                        //获取ConnectivityManager对象对应的NetworkInfo对象
                        NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                        if ("MOBILE".equals(networkInfo.getTypeName()))
                            speedView.netStatusText.setText("MOBILE");
                        if ("WiFi".equals(networkInfo.getTypeName()))
                            speedView.netStatusText.setText("WiFi");
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                    speedView.netStatusText.setText("");
                }

            }
        }
    };


    public int getNetWorkClass(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return Constants.NETWORK_CLASS_2_G;

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return Constants.NETWORK_CLASS_3_G;

            case TelephonyManager.NETWORK_TYPE_LTE:
                return Constants.NETWORK_CLASS_4_G;

            default:
                return Constants.NETWORK_CLASS_UNKNOWN;
        }
    }


























}