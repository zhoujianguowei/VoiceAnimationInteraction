package actionMain;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by Administrator on 2015/6/4.
 * 打开wifi
 */
public class OpenWifi extends SupAction
{

    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        String reg = "(\\S+)wifi";
        Matcher matcher = Pattern.compile(reg).matcher(dictation);
        String command = null;
        if (matcher.find())
        {
            command = matcher.group(1);
        }
        if (!TextUtils.isEmpty(command) && command.contains("关闭"))
        {
            openOrCloseWifi(false);
        }
        else
        {
            openOrCloseWifi(true);
        }
        context.setSessionStatus(SpeechMainActivity.SESSION_OVER);
    }
    private void openOrCloseWifi(boolean isEnabled)
    {
        final WifiManager wifiManager =
                (WifiManager) speechMain.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
        int wifiStatus = wifiManager.getWifiState();
        if (isEnabled)
        {
            if (wifiStatus == WifiManager.WIFI_STATE_ENABLED)
            {
                synthesizerWrapper.startSpeak("wifi已经打开");
                return;
            }
            else
            {
                synthesizerWrapper.startSpeak("正在打开wifi");
                if (!wifiManager.setWifiEnabled(true))
                {
                    synthesizerHandler.postDelayed(new Runnable()
                    {
                        @Override public void run()
                        {
                            synthesizerWrapper.startSpeak("无法打开wifi");
                        }
                    }, 1000);
                }
                else
                {
                    synthesizerHandler.postDelayed(new Runnable()
                    {
                        @Override public void run()
                        {
                            synthesizerWrapper.startSpeak("wifi成功打开");
                        }
                    }, 1000);
                }
            }
        }
        else
        {
            if (wifiStatus == WifiManager.WIFI_STATE_DISABLED)
            {
                synthesizerWrapper.startSpeak("wifi没有打开");
                return;
            }
            else
            {
                if (!wifiManager.setWifiEnabled(isEnabled))
                {
                    synthesizerWrapper.startSpeak("无法关闭wifi");
                }
                else
                {
                    synthesizerWrapper.startSpeak("wifi成功关闭");
                }
            }
        }
    }
}
