package actionMain;
import android.content.Intent;
import android.text.TextUtils;

import com.example.voiceanimationinteraction.AllApplication;
import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entity.AppInfo;
import utils.PinyinUtils;
/**
 * 打开手机上的应用程序
 *
 * @author Administrator
 */
public class OpenApplication extends SupAction
{

    private String appLabel;
    private String appPac;
    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        // 如果仅仅是打开两个字，返回主会话
        if (dictation.equals("打开"))
        {
            synthesizerWrapper.startSpeak("打开什么应用程序?");
            speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
            return;
        }
        String regx = "打开(\\S+)";
        Matcher matcher = Pattern.compile(regx).matcher(dictation);
        if (matcher.find())
        {
            appLabel = matcher.group(1);
            List<AppInfo> appInfos =
                    AllApplication.getAllApplications(speechMain);
            // 采用首次匹配原则
            for (AppInfo appInfo : appInfos)
            {
                String label = appInfo.getLabel();
                String pacName = appInfo.getPackageName();
                if (!TextUtils.isEmpty(appLabel) &&
                        PinyinUtils.converterToSpell(label).contains(
                                PinyinUtils.converterToSpell(appLabel))
                        || PinyinUtils.converterToSpell(appLabel).contains(
                        PinyinUtils.converterToSpell(label)))
                {
                    appPac = pacName;
                    break;
                }
            }
            if (appPac != null)
            {
                if (recognizerHandler == null)
                {
                    recognizerHandler = new SpeechHandler(speechMain,
                            SpeechHandler.SPEECH_RECOGNIZER_FLAG);
                }
                synthesizerWrapper.startSpeak("正在打开：" + appLabel);
                recognizerHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        Intent intent = speechMain.getPackageManager()
                                .getLaunchIntentForPackage(appPac);
                        if (intent != null)
                        {
                            speechMain.startActivity(intent);
                        }
                        else
                        {
                            synthesizerWrapper.startSpeak("无法打开" + appLabel
                                    + "应用");
                        }
                    }
                }, 2000);
            }
            else
            {
                synthesizerWrapper.startSpeak("没有安装" + appLabel + "应用程序");
            }
        }
        speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
    }
}
