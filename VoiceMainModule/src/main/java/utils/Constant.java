package utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;
public class Constant
{

    public static final String SETTING_INFO = "settingInfo";
    public static int screenWidth;
    public static int screenHeight;
    /**
     * 查询油价的domain
     */
    public static final String OIL_HTTP_SERVER = "http://api.jisuapi.com";
    public static final String OIL_KEY = "d63e35f29ed6b58f";
    /**
     * 查询天气的domain
     */
    public static final String WEATHER_HTTP_SERVER = "http://v.juhe.cn/weather/index";
    public static final String WEATHER_KEY = "0bb5b5d33b6c5c62100c3daa7baeee77";
    /**
     * 像素密度
     */
    public static float density;
    private static SharedPreferences sharedPreferences;
    /**
     * 用户申请的id
     */
    public static String appid;
    // 前后端点超时
    private static final String VAD_BOX = "vad_box";
    private static final String VAD_EOX = "vad_eox";
    // 是否首次运行应用程序
    private static final String HAS_INSTALLED = "has_install";
    /**
     * 下面是语音合成所使用的常量
     */
    private static final String VOLUME = "volume";
    private static final String SPEED = "speed";
    private static final String CURRENT_CITY = "current_city";
    private static final String CURRENT_PROVINCE = "current_province";
    private static ArrayList<Activity> activityArrayList = new ArrayList<>();
    public static boolean pushActivity(Activity activity)
    {
        if (!activityArrayList.contains(activity))
        {
            activityArrayList.add(activity);
            return true;
        }
        return false;
    }
    public static boolean popActivity(Activity activity)
    {
        if (activityArrayList.contains(activity))
        {
            activityArrayList.remove(activity);
            return true;
        }
        return false;
    }
    public static void finishAllActivities(boolean shouldRestart)
    {
        Activity activity = null;
        for (int i = activityArrayList.size() - 1; i >= 0; i--)
        {
            activity = activityArrayList.get(i);
            activity.finish();
            if (i == 0 && shouldRestart)
            {
                Intent intent = activity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(activity.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }
    public static void initConstant(Context context)
    {
        ScreenSize.ScreenSizeInital(context);
        screenWidth = ScreenSize.getScreenWidth();
        screenHeight = ScreenSize.getScreenHeight();
        density = ScreenSize.getScreenDensity();
        appid = getAppidFromMetaData(context);
        sharedPreferences = context.getSharedPreferences(SETTING_INFO,
                Context.MODE_PRIVATE);
        initialPreference(context);
    }
    /**
     * 获得前向超时
     */
    public static String getVadBox()
    {
        return sharedPreferences.getString(VAD_BOX, "5000");
    }
    public static String getVadEox()
    {
        return sharedPreferences.getString(VAD_EOX, "3000");
    }
    public static void setVadBox(String vadBoxMillSeconds)
    {
        sharedPreferences.edit().putString(VAD_BOX, vadBoxMillSeconds).apply();
    }
    public static void setVadEox(String vadEoxMillSeconds)
    {
        sharedPreferences.edit().putString(VAD_EOX, vadEoxMillSeconds).apply();
    }
    public static String getCurrentProvince()
    {
        return sharedPreferences.getString(CURRENT_PROVINCE, "四川");
    }
    public static void setCurrentProvince(String province)
    {
        sharedPreferences.edit().putString(CURRENT_PROVINCE, province).apply();
    }
    public static String getCurrentCity()
    {
        return sharedPreferences.getString(CURRENT_CITY, "成都");
    }
    public static void setCurrentCity(String city)
    {
        Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_CITY, city);
        editor.apply();
    }
    public static String getVolume()
    {
        return sharedPreferences.getString(VOLUME, "50");
    }
    public static void setVolume(String volume)
    {
        sharedPreferences.edit().putString(VOLUME, volume).apply();
    }
    public static String getSpeed()
    {
        return sharedPreferences.getString(SPEED, "50");
    }
    public static void setSpeed(String speed)
    {
        sharedPreferences.edit().putString(SPEED, speed).apply();
    }
    private static void initialPreference(Context context)
    {
        // TODO Auto-generated method stub
        // 首次配置设置
        if (!sharedPreferences.contains(HAS_INSTALLED))
        {
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(HAS_INSTALLED, true);
            editor.putString(CURRENT_PROVINCE, "四川");
            editor.putString(CURRENT_CITY, "成都");
            editor.putString(VAD_BOX, 5000 + "");
            editor.putString(VAD_EOX, 3000 + "");
            editor.putString(SPEED, 50 + "");
            editor.putString(VOLUME, "50");
            editor.commit();
        }
    }
    protected static String getAppidFromMetaData(Context context)
    {
        ApplicationInfo applicationInfo = null;
        try
        {
            applicationInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
        }
        catch (NameNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null == applicationInfo)
        {
            return null;
        }
        else
        {
            return applicationInfo.metaData.getString("appid");
        }
    }
    static class ScreenSize
    {

        private static boolean isInit;
        private static DisplayMetrics metrics;
        private static Context context;
        public static void ScreenSizeInital(Context context)
        {
            ScreenSize.context = context;
        }
        static void init()
        {
            if (!isInit)
            {
                metrics = new DisplayMetrics();
                WindowManager manager = (WindowManager) context
                        .getSystemService(Context.WINDOW_SERVICE);
                manager.getDefaultDisplay().getMetrics(metrics);
                isInit = true;
            }
        }
        static int getScreenWidth()
        {
            if (!isInit)
            {
                init();
            }
            return metrics.widthPixels;
        }
        static int getScreenHeight()
        {
            if (!isInit)
            {
                init();
            }
            return metrics.heightPixels;
        }
        static float getScreenDensity()
        {
            if (!isInit)
            {
                init();
            }
            return metrics.density;
        }
    }
}