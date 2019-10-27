package services;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import entity.OilPrice;
import utils.Constant;
import utils.HttpConnectionUtility;
public class OilService extends IntentService
{

    private static final String TAG = OilService.class.getSimpleName();
    /**
     * 从http://www.haoservice.com/网站获取的请求oil的key
     */
    private static final String ACTION_GET_OIL_PRICE =
            "com.example.voiceanimationinteraction.ACTION_GET_OIL_PRICE";
    public static final String BROADCAST_OIL_PRICE_ACTION =
            "com.example.voiceanimationinteraction.BROADCAST_OIL_PRICE_ACTION";
    public static final String EXTRA_PROVINCE = "extra_province";
    public static final String EXTRA_OIL_PRICES = "extra_oil_prices";
    public OilService(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }
    public OilService()
    {
        this("province");
    }
    public static void queryOilPrice(Context context, String province)
    {
        Intent intent = new Intent(context, OilService.class);
        intent.setAction(ACTION_GET_OIL_PRICE);
        intent.putExtra(EXTRA_PROVINCE, province);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent(Intent intent)
    {
        // TODO Auto-generated method stub
        switch (intent.getAction())
        {
            case ACTION_GET_OIL_PRICE:
                executeQueryCurrentProvinceOilPrice(
                        intent.getStringExtra(EXTRA_PROVINCE));
                break;
            default:
                break;
        }
    }
    /**
     * 请求油价，数据类型
     */
    private void executeQueryCurrentProvinceOilPrice(String province)
    {
        // TODO Auto-generated method stub
        String checkOilPriceUrl = Constant.OIL_HTTP_SERVER + "/oil/query";
        HttpConnectionUtility oilUtility =
                new HttpConnectionUtility(checkOilPriceUrl);
        oilUtility.setMethodType(HttpConnectionUtility.POST_REQUEST);
        HashMap<String, Object> requestHeader = new HashMap<>();
        requestHeader.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, " +
                        "like Gecko) Chrome/49.0.2623.221 Safari/537.36 SE 2.X " +
                        "MetaSr 1.0");
//        oilUtility.setRequestBody(requestHeader);
        oilUtility.setRequestProperty(requestHeader);
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("appkey", Constant.OIL_KEY);
        requestBody.put("province", province);
        oilUtility.setRequestBody(requestBody);
        HashMap<String, Object> oilResMap =
                oilUtility.executeSingleAttachThread();
        if (oilResMap.get("status").toString().equals("success"))
        {
            sendQueryOilPriceBroadcast(parseResOilPrice(new String(
                    (byte[]) oilResMap.get("result")), province), province);
        }
        else
        {
            sendQueryOilPriceBroadcast(null, province);
        }
    }
    private ArrayList<OilPrice> parseResOilPrice(String resJson, String
            province)
    {
        /**
         * 数据格式如下：
         *  {
         "status": "0",
         "msg": "ok",
         "result": {
         "province": "安徽",
         "oil89": "5.85",
         "oil92": "6.25",
         "oil95": "6.71",
         "oil0": "5.91",
         "oil90": "5.85",
         "oil93": "6.25",
         "oil97": "6.71",
         "updatetime": "2017-09-19 07:00:01"
         }
         }
         */
        // TODO Auto-generated method stub
        JSONObject oilJsRoot = null;
        Log.i("oilInfo", resJson);
        ArrayList<OilPrice> resPrices = new ArrayList<OilPrice>();
        try
        {
            oilJsRoot = new JSONObject(resJson);
            if (oilJsRoot.getString("msg").equals("ok"))
            {
                JSONObject oilJsItem = oilJsRoot.getJSONObject("result");
                int[] oilType = new int[]{0, 89, 90, 92, 93, 95, 97};
                for (int i = 0; i < oilType.length; i++)
                {
                    OilPrice oilPrice = new OilPrice(province);
                    oilPrice.setOilType(oilType[i] + "号");
                    oilPrice.setOilPrice(
                            (float) oilJsItem.optDouble("oil" + oilType[i], 0.0));
                    resPrices.add(oilPrice);
                }
            }
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resPrices;
    }
    private void sendQueryOilPriceBroadcast(@Nullable ArrayList<OilPrice>
                                                    oilPrices, String province)
    {
        Intent intent = new Intent(BROADCAST_OIL_PRICE_ACTION);
        intent.putExtra("status", "fail");
        if (oilPrices != null && !oilPrices.isEmpty())
        {
            intent.putExtra("status", "success");
            intent.putExtra(EXTRA_PROVINCE, province);
            intent.putParcelableArrayListExtra(EXTRA_OIL_PRICES, oilPrices);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
