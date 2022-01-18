package com.sochat.soulter.sophoneinfor;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import static com.sochat.soulter.sophoneinfor.MainActivity.INIT_X;
import static com.sochat.soulter.sophoneinfor.MainActivity.INIT_Y;

public class SpeedView extends FrameLayout {
    private Context mContext;
    public TextView downText;
    public TextView upText;
    public TextView batteryText;
    public TextView netStatusText;
    private WindowManager windowManager;
    private int statusBarHeight;
    private float preX,preY,x,y;

    public SpeedView(Context context) {
        super(context);
        mContext=context;
        init();
    }

    private void init() {
        statusBarHeight=WindowUtil.statusBarHeight;
        windowManager= (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //a view inflate itself, that's funny
        inflate(mContext,R.layout.speed_layout,this);
        downText= (TextView) findViewById(R.id.speed_down);
        upText= (TextView) findViewById(R.id.speed_up);
        batteryText = (TextView)findViewById(R.id.battery);
        netStatusText = (TextView)findViewById(R.id.net_status);



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preX=event.getRawX();preY=event.getRawY()-statusBarHeight;
                return true;
            case MotionEvent.ACTION_MOVE:
                x=event.getRawX();y=event.getRawY()-statusBarHeight;
                WindowManager.LayoutParams params= (WindowManager.LayoutParams) getLayoutParams();
                params.x+=x-preX;
                params.y+=y-preY;
                windowManager.updateViewLayout(this,params);
                SharedPreferencesUtils.putToSpfs(mContext, INIT_X,params.x);
                SharedPreferencesUtils.putToSpfs(mContext, INIT_Y,params.y);
                preX=x;preY=y;
                return true;
            default:
                break;

        }
        return super.onTouchEvent(event);
}

}
