package services;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import entity.Weather;
import utils.Constant;
import utils.HttpConnectionUtility;
/**
 * Created by Administrator on 2015/6/3.
 */
public class WeatherService extends IntentService
{

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public static final String EXTRA_CURRENT_CITY = "extra_current_city";
    public static final String EXTRA_WEATHERS = "extra_weathers";
    public static final String ACTION_GET_CURRENT_CITY_WEATHER = "services" +
            "_action_get_current_city_weather";
    public static final String BROADCAST_CURRENT_CITY_WEATHER_ACTION = "services" +
            "_broadcast_current_city_weather";
    public WeatherService()
    {
        this("weather");
    }
    private WeatherService(String name)
    {
        super(name);
    }
    public static void startQueryWeather(Context context, String city)
    {
        Intent intent = new Intent(context, WeatherService.class);
        intent.setAction(ACTION_GET_CURRENT_CITY_WEATHER);
        intent.putExtra(EXTRA_CURRENT_CITY, city);
        context.startService(intent);
    }
    @Override protected void onHandleIntent(Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_GET_CURRENT_CITY_WEATHER:
                executeQueryCurrentCityWeather(intent.getStringExtra
                        (EXTRA_CURRENT_CITY));
                break;
        }
    }
    private void executeQueryCurrentCityWeather(String city)
    {
        String queryWeatherUrl = null;
        try
        {
            queryWeatherUrl = Constant
                    .WEATHER_HTTP_SERVER + "?cityname=" + URLEncoder.encode(city,
                    "utf-8") + "&key=" + Constant.WEATHER_KEY;
            HttpConnectionUtility weatherUtility = new HttpConnectionUtility
                    (queryWeatherUrl);
            HashMap<String, Object> resWeatherMap = weatherUtility
                    .executeSingleAttachThread();
            if (resWeatherMap.get("status").toString().equals("success"))
            {
                parseQueryWeatherInfo(new String((byte[]) resWeatherMap.get
                        ("result")));
                return;
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        sendQueryWeatherBroadcast(null);
    }
    /**
     * @param result
     */
    private void parseQueryWeatherInfo(String result)
    {
/**
 {"resultcode":"200","reason":"successed!","result":{"sk":{"temp":"28",
 "wind_direction":"西风","wind_strength":"1级","humidity":"53%","time":"16:22"},
 "today":{"temperature":"19℃~27℃","weather":"阴转小雨","weather_id":{"fa":"02",
 "fb":"07"},"wind":"微风","week":"星期四","city":"成都","date_y":"2017年09月21日",
 "dressing_index":"热","dressing_advice":"天气热，建议着短裙、短裤、短薄外套、T恤等夏季服装。",
 "uv_index":"最弱","comfort_index":"","wash_index":"不宜","travel_index":"较适宜",
 "exercise_index":"较适宜","drying_index":""},
 "future":{"day_20170921":{"temperature":"19℃~27℃","weather":"阴转小雨",
 "weather_id":{"fa":"02","fb":"07"},"wind":"微风","week":"星期四","date":"20170921"},
 "day_20170922":{"temperature":"19℃~22℃","weather":"小雨转阴","weather_id":{"fa":"07",
 "fb":"02"},"wind":"微风","week":"星期五","date":"20170922"},
 "day_20170923":{"temperature":"19℃~23℃","weather":"阴","weather_id":{"fa":"02",
 "fb":"02"},"wind":"微风","week":"星期六","date":"20170923"},
 "day_20170924":{"temperature":"21℃~24℃","weather":"阴","weather_id":{"fa":"02",
 "fb":"02"},"wind":"微风","week":"星期日","date":"20170924"},
 "day_20170925":{"temperature":"21℃~26℃","weather":"阴转小雨","weather_id":{"fa":"02",
 "fb":"07"},"wind":"微风","week":"星期一","date":"20170925"},
 "day_20170926":{"temperature":"21℃~24℃","weather":"阴","weather_id":{"fa":"02",
 "fb":"02"},"wind":"微风","week":"星期二","date":"20170926"},
 "day_20170927":{"temperature":"19℃~23℃","weather":"阴","weather_id":{"fa":"02",
 "fb":"02"},"wind":"微风","week":"星期三","date":"20170927"}}},"error_code":0}
 */
        JSONObject rootJs = null;
        JSONObject resultJs = null;
        JSONObject futureJsObj;
        ArrayList<Weather> weatherList = new ArrayList<>();
        Weather todayWeather = null;
        try
        {
            rootJs = new JSONObject(result);
            if (rootJs.getString("resultcode").equals("200"))
            {
                resultJs = rootJs.getJSONObject("result");
                JSONObject todayJs = resultJs.getJSONObject("today");
                todayWeather = new Weather();
                todayWeather.setCity(todayJs.optString("city"));
                todayWeather.setWeather(todayJs.optString("weather"));
                todayWeather.setWind(todayJs.optString("wind"));
                todayWeather.setTemperature(todayJs.optString("temperature"));
                //当前时间的天气状况
                JSONObject skJs = resultJs.getJSONObject("sk");
                todayWeather.setCurrent_temp(skJs.optString("temp"));
                weatherList.add(todayWeather);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                        "yyyyMMdd");
                Calendar currentDate = Calendar.getInstance();
                futureJsObj = resultJs.getJSONObject("future");
                for (int i = 0; i < futureJsObj.length(); i++)
                {
                    JSONObject futureJsItem = futureJsObj
                            .getJSONObject("day_" +
                                    simpleDateFormat.format(currentDate.getTime()));
                    Weather weather = new Weather();
                    weather.setCity(todayWeather.getCity());
                    weather.setWind(futureJsItem.optString("wind"));
                    weather.setWeather(futureJsItem.optString("weather"));
                    weather.setTemperature(futureJsItem.optString
                            ("temperature"));
                    weatherList.add(weather);
                    currentDate.add(Calendar.DATE, 1);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        sendQueryWeatherBroadcast(weatherList);
    }
    private void sendQueryWeatherBroadcast(@Nullable ArrayList<Weather>
                                                   weatherList)
    {
        Intent intent = new Intent(BROADCAST_CURRENT_CITY_WEATHER_ACTION);
        intent.putExtra("status", "fail");
        if (weatherList != null && !weatherList.isEmpty())
        {
            intent.putExtra("status", "success");
            intent.putParcelableArrayListExtra(EXTRA_WEATHERS, weatherList);
            Log.e("weathers", weatherList.toString());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
