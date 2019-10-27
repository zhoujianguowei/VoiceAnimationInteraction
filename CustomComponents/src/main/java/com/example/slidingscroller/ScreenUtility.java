package com.example.slidingscroller;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
public class ScreenUtility
{

    private static int screenWidth;
    private static int screenHeight;
    private static int density;
    private static boolean isInit;
    public static int getScreenWidth()
    {
        if (!isInit)
            throw new IllegalAccessError("Metrics hasnt been initailed");
        return screenWidth;
    }
    public static int getScreenHeight()
    {
        if (!isInit)
            throw new IllegalAccessError("Metrics hasn't been initailed");
        return screenHeight;
    }
    public static void initParams(Context context)
    {
        synchronized (ScreenUtility.class)
        {
            isInit = true;
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager manager = (WindowManager) (context)
                    .getSystemService(Context.WINDOW_SERVICE);
            manager.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            density = metrics.densityDpi;
        }
    }
}
