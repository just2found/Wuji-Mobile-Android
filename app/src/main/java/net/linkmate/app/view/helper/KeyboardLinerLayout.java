package net.linkmate.app.view.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public class KeyboardLinerLayout extends LinearLayout {
    public KeyboardLinerLayout(Context context) {
        this(context, null);
    }

    public KeyboardLinerLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardLinerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyboardLinerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
/*
*import android.content.Context;

import android.content.res.Configuration;

import android.graphics.PixelFormat;

import android.util.DisplayMetrics;

import android.util.Log;

import android.view.Gravity;

import android.view.View;

import android.view.WindowManager;

 

public class FloatKeyboardMonitor extends View{

    private static final boolean DEBUG = true;

    private static final String TAG ="float.KeyboardMonitor";

   

    private final WindowManagerwindowManager;

    private final WindowManager.LayoutParamslayoutParams;

   

    private int screenHeight = 0;

    private int softKeyboardHeight = 0;

    private int oldOrientation=0;

 

    public FloatKeyboardMonitor(Context context){

        super(context);

        windowManager =((WindowManager)context.getSystemService("window"));

        layoutParams = newWindowManager.LayoutParams();       

        layoutParams.width= 0;

        layoutParams.x=0;

        layoutParams.height =WindowManager.LayoutParams.MATCH_PARENT;

        layoutParams.type =WindowManager.LayoutParams.TYPE_PHONE;

        //关键是WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM

        //要求能够被输入法遮挡

        layoutParams.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        layoutParams.format= PixelFormat.TRANSPARENT;

        layoutParams.gravity= Gravity.LEFT | Gravity.TOP;

       windowManager.addView(this, layoutParams);

       setScreenHeight(context);

        setSoftKeyboardHeight(context);

    }   

    private void setScreenHeight(Contextcontext){

        DisplayMetrics dm =context.getResources().getDisplayMetrics();

        screenHeight =dm.heightPixels;//手机屏幕高度

    } 

    private void setSoftKeyboardHeight(Contextcontext){

        DisplayMetrics dm =context.getResources().getDisplayMetrics();

        softKeyboardHeight =(int)(dm.density*100f+0.5f);//软键盘的高度，这里定义了一个随机值，假设所有手机输入法最小高度为100dp

    } 

    @Override

    protected void onSizeChanged(int w, int h,int oldw, int oldh) {

       super.onSizeChanged(w, h, oldw, oldh);

        if (DEBUG) {

           Log.i(TAG, "screenHeight=" + screenHeight + ";w=" + w +";h=" + h + ";oldw=" + oldw + ";oldh=" + oldh);

        }

        if (h ==screenHeight) {

           if (oldh != 0) {

               if (DEBUG) {

                   Log.i(TAG, "变化为全屏了.");

               }   

            }else{

               if (DEBUG) {

                   Log.i(TAG, "初始化，当前为全屏状态.");

               }

           }           

        } else if(Math.abs(h - oldh) > softKeyboardHeight) {

           if (h >= oldh) {

               if (DEBUG) {

                   Log.i(TAG, "变化为正常状态(输入法关闭).");

               }

           }else{

               if (DEBUG) {

                   Log.i(TAG, "输入法显示了.");

               }

           }          

        } else{

           if (oldh != 0) {

               if (DEBUG) {

                   Log.i(TAG, "变化为正常状态.(全屏关闭)");

               }

           }else{

               if (DEBUG) {

                   Log.i(TAG, "初始化，当前为正常状态.");

               }

           }

        }

    }

 

    @Override

    protected void onConfigurationChanged(Configuration newConfig) {

       super.onConfigurationChanged(newConfig);

        if (DEBUG) {

           Log.i(TAG, "onConfigurationChanged neworientation=" +newConfig.orientation + ";oldOrientation=" + oldOrientation);

        }

        if (oldOrientation !=newConfig.orientation) {

           setScreenHeight(getContext());

           oldOrientation = newConfig.orientation;

        }

    }

}
 */