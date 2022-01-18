package com.sochat.soulter.sophoneinfor;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE=0;
    private Switch showCloseBt;

    private CheckBox cbSpeedUp;
    private CheckBox cbSpeedDown;
    private CheckBox cbBattery;
    private CheckBox cbNetStatus;

    private EditText speedDelayTime;
    private Button ok;

    private SeekBar sbWindowSize;
    private TextView tvWindowSize;

    private Button btAbout;

    public static final String UP="up";
    public static final String DOWN="down";
    public static final String BATTERY="battery";
    public static final String NET_STATUS= "net_status";
    public static final String B_FALSE="false";
    public static final String B_TRUE="true";
    public static final String CHANGED="changed";
    public static final String INIT_X="init_x";
    public static final String INIT_Y="init_y";
    public static final String IS_SHOWN="is_shown";
    public static final String SPEED_DELAY = "speed_delay";
    public static final String WINDOW_SIZE = "window_size";
    public static final String ABOUT_CACHE = "about_cache";

    public static Boolean isShow = false;//悬浮窗标识符
    private int speedDelayTimeInt = 2000;//网速更新延迟
    public static int handlerWhatCount = 0;//计时器what参数计数


    private MyTask mtask;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检查悬浮窗权限
        if(checkDrawOverlayPermission()){
            init();
        }

    }


    private void init() {
        showCloseBt= (Switch) findViewById(R.id.bt_show_close);

        cbSpeedUp = (CheckBox)findViewById(R.id.cb_speed_up);
        cbSpeedDown = (CheckBox)findViewById(R.id.cb_speed_down);
        cbBattery = (CheckBox)findViewById(R.id.cb_battery);
        cbNetStatus = (CheckBox)findViewById(R.id.cb_net_status);

        speedDelayTime = (EditText)findViewById(R.id.ed_SpeedDelayTime);
        ok = (Button) findViewById(R.id.ok);

        btAbout = (Button)findViewById(R.id.bt_about);

        sbWindowSize = (SeekBar)findViewById(R.id.sb_window_size);
        tvWindowSize = (TextView)findViewById(R.id.tv_window_size);


        btAbout.setText(String.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this, ABOUT_CACHE, "关于\n\n作者QQ:905617992\n\n建议将本应用后台配置/省电策略设置为无限制")));
        speedDelayTime.setText(String.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this, SPEED_DELAY, 2000)));
        tvWindowSize.setText(String.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this,WINDOW_SIZE,10)));



        //扫描服务
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(200)) {
            if ("com.sochat.soulter.sophoneinfor.SpeedCalculationService".equals(service.service.getClassName())){
                isShow = true;
                Utils.Toast("服务正在运行呀!",MainActivity.this);
                break;
            }
        }
        if (!isShow){
            isShow = true;
            WindowUtil.interval= Integer.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this,MainActivity.SPEED_DELAY,2000).toString());
            startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                    .putExtra(CHANGED,true));
            startService(new Intent(MainActivity.this,SpeedCalculationService.class));
            makeWebsiteQuery();
        }

        showCloseBt.setChecked(true);
        //检测 并设置控件可见性
        if(isShow){
            widgetVisibility(true);
        }else{
            widgetVisibility(false);
        }

        //TODO:待优化代码
        //设置复选框点击事件
        cbSpeedUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, UP, B_TRUE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
                else{
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, UP, B_FALSE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
            }
        });
        cbSpeedDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, DOWN, B_TRUE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
                else{
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, DOWN, B_FALSE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
            }
        });
        cbBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, BATTERY, B_TRUE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
                else{
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, BATTERY, B_FALSE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
            }
        });
        cbNetStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, NET_STATUS, B_TRUE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
                else{
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, NET_STATUS, B_FALSE);
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                }
            }
        });

        showCloseBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    isShow = true;
                    WindowUtil.interval= Integer.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this,MainActivity.SPEED_DELAY,2000).toString());
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                            .putExtra(CHANGED,true));
                    startService(new Intent(MainActivity.this,SpeedCalculationService.class));

                    widgetVisibility(true);
                }else{
                    isShow = false;
                    stopService(new Intent(MainActivity.this,SpeedCalculationService.class));
                    widgetVisibility(false);
                }
            }
        });


        WindowUtil.statusBarHeight=getStatusBarHeight();

        //检查复选框选择情况
        String isUpChecked= (String) SharedPreferencesUtils.getFromSpfs(this, UP, B_TRUE);
        String isDownChecked= (String) SharedPreferencesUtils.getFromSpfs(this, DOWN, B_TRUE);
        String isBatteryChecked= (String) SharedPreferencesUtils.getFromSpfs(this, BATTERY, B_TRUE);
        String isNetStatusChecked= (String) SharedPreferencesUtils.getFromSpfs(this, NET_STATUS, B_TRUE);
        if(isUpChecked.equals(B_TRUE)){
            cbSpeedUp.setChecked(true);
        }
        if(isDownChecked.equals(B_TRUE)){
            cbSpeedDown.setChecked(true);
        }
        if(isBatteryChecked.equals(B_TRUE)){
            cbBattery.setChecked(true);
        }
        if(isNetStatusChecked.equals(B_TRUE)){
            cbNetStatus.setChecked(true);
        }


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    speedDelayTimeInt = Integer.parseInt(speedDelayTime.getText().toString());


                }catch (NumberFormatException e){
                    Utils.Toast("emmmm,好像啥也没输呐~",MainActivity.this);
                    return;
                }

                if((int)SharedPreferencesUtils.getFromSpfs(MainActivity.this,SPEED_DELAY,2000)==speedDelayTimeInt)
                    return;

                WindowUtil.interval= Integer.valueOf(SharedPreferencesUtils.getFromSpfs(MainActivity.this,MainActivity.SPEED_DELAY,2000).toString());
                handlerWhatCount+=1;
                SharedPreferencesUtils.putToSpfs(MainActivity.this,SPEED_DELAY,speedDelayTimeInt);
                startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                        .putExtra(CHANGED,true));
            }
        });

        sbWindowSize.setMax(64);
        sbWindowSize.setProgress((int)SharedPreferencesUtils.getFromSpfs(MainActivity.this,WINDOW_SIZE,10));
        sbWindowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvWindowSize.setText(String.valueOf(i));
                SharedPreferencesUtils.putToSpfs(MainActivity.this,WINDOW_SIZE,i);
                startService(new Intent(MainActivity.this,SpeedCalculationService.class)
                        .putExtra(CHANGED,true));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Toast("onActivityResult-->",MainActivity.this);
        if(requestCode==REQUEST_CODE){
            if (Settings.canDrawOverlays(this)) {
                init();
            }else{
                Toast.makeText(this, "请授予悬浮窗权限!!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void widgetVisibility(Boolean wantWidgetShow){
        if(!wantWidgetShow){
            showCloseBt.setText(R.string.window_show);
            cbSpeedUp.setEnabled(false);
            cbSpeedDown.setEnabled(false);
            cbBattery.setEnabled(false);
            cbNetStatus.setEnabled(false);
            speedDelayTime.setEnabled(false);
            ok.setEnabled(false);
            sbWindowSize.setEnabled(false);
        }else{
            showCloseBt.setText(R.string.window_close);
            cbSpeedUp.setEnabled(true);
            cbSpeedDown.setEnabled(true);
            cbBattery.setEnabled(true);
            cbNetStatus.setEnabled(true);
            speedDelayTime.setEnabled(true);
            ok.setEnabled(true);
            sbWindowSize.setEnabled(true);
        }
    }


    private int getStatusBarHeight(){
        Rect rectangle = new Rect();
        Window window =getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }


    private void makeWebsiteQuery(){
        URL webUri = NetworkUtils.buildUrl("https://sorater.top/appinfor.html");
        mtask = new MyTask();
        mtask.execute(webUri);
    }


    private class MyTask extends AsyncTask<URL,Void,String>{
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String websiteResults = null;
            try{
                websiteResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);

            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return websiteResults;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if (s != null || s.equals("")) {
                    btAbout.setText(s);
                    SharedPreferencesUtils.putToSpfs(MainActivity.this, ABOUT_CACHE, s);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                SharedPreferencesUtils.getFromSpfs(MainActivity.this, ABOUT_CACHE, "关于\n\n作者QQ:905617992\n\n建议将本应用后台配置/省电策略设置为无限制");
            }
        }


    }

}


