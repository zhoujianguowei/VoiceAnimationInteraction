package services;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import utils.SpeechRecognizerWrapper;
/**
 * Created by Administrator on 2017/9/22.
 * 用来执行唤醒当前语音窗口
 */
public class WakeUpService extends IntentService implements Handler.Callback
{

    SpeechRecognizerWrapper speechRecognizerWrapper;
    SpeechHandler speechHandler;
    String packageName;
    static volatile boolean isServiceStarted = false;
    public static String START_WAKE_UP_EXTRA = "startWakeUpExtra";
    private volatile boolean dictationOver = true;
    private static volatile boolean isInterrupted = false;
    private final static String TAG = WakeUpService.class.getSimpleName();
    @Override public void onCreate()
    {
        super.onCreate();
        isInterrupted = false;
        isServiceStarted = true;
        speechHandler = new SpeechHandler(this,
                SpeechHandler.SPEECH_RECOGNIZER_FLAG);
        speechRecognizerWrapper = SpeechRecognizerWrapper
                .getSingleInstance(this, speechHandler);
        packageName = getPackageName();
        isServiceStarted = true;
    }
    public WakeUpService()
    {
        super("wakeUpService");
    }
    public WakeUpService(String name)
    {
        super(name);
    }
    @Override protected void onHandleIntent(@Nullable Intent intent)
    {
        if (intent.getStringExtra(START_WAKE_UP_EXTRA).equals("wakeUp"))
        {
            while (!isInterrupted)
            {
                speechRecognizerWrapper.startDictation();
                while (!dictationOver)
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
            }
        }
    }
    @Override public boolean handleMessage(Message msg)
    {
        Log.i(TAG, msg.obj.toString());
        if (msg.arg1 == SpeechHandler.SPEECH_RECOGNIZER_FLAG)
        {
            switch (msg.what)
            {
                case SpeechRecognizerWrapper.DICTATION_OVER:
                    String dictationResult = msg.obj.toString();
                    if (dictationResult.equals("芝麻开门"))
                    {
                        Intent intent = new Intent(WakeUpService.this,
                                SpeechMainActivity.class);
                        WakeUpService.this.startActivity(intent);
                        isInterrupted = true;
                        isServiceStarted = false;
                        WakeUpService.this.stopSelf();
                    }
                    dictationOver = true;
                    break;
                case SpeechRecognizerWrapper.DICTATION_ERROR:
                    dictationOver = true;
                    break;
            }
        }
        return true;
    }
}
