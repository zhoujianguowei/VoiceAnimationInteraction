package utils;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.example.voiceanimationinteraction.SpeechHandler;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
/**
 * 将文字信息转换为可听的声音信息，封装了开始，暂停，继续，停止一类方法。
 * 采用的是单例模式
 *
 * @author Administrator
 */
public class SpeechSynthesizerWrapper
{

    private static SpeechSynthesizerWrapper speechSynthesizerWrapper;
    private SpeechSynthesizer synthesizer;
    static SynthesizerListener listener;
    public static boolean isInit;
    private static boolean isStart;
    private static boolean isPaused;
    static Context context;
    SpeechHandler synthesizerHandler;
    // 语音合成开始
    public static final int SPEECH_SYNTHESIZER_BEGIN = 20;
    // 语音识别进行中
    public static final int SPEECH_SYNTHESIZER_ONGOING = 21;
    // 语音识别暂停
    public static final int SPEECH_SYNTHESIZER_PAUSE = 22;
    // 语音识别结束
    public static final int SPEECH_SYNTHESIZER_OVER = 23;
    private volatile int synthesizerStatus = SPEECH_SYNTHESIZER_OVER;
    /**
     * 要说的话
     */
    private static String speakText;
    public static synchronized SpeechSynthesizerWrapper getSingleInstance(
            Context
                    context,
            SpeechHandler handler)
    {
        if (speechSynthesizerWrapper == null)
        {
            speechSynthesizerWrapper = new SpeechSynthesizerWrapper(context,
                    handler);
        }
        return speechSynthesizerWrapper;
    }
    public SpeechHandler getSynthesizerHandler()
    {
        return synthesizerHandler;
    }
    public void setSynthesizerHandler(
            SpeechHandler synthesizerHandler)
    {
        this.synthesizerHandler = synthesizerHandler;
    }
    private SpeechSynthesizerWrapper(Context context,
                                     SpeechHandler handler)
    {
        this.context = context;
        this.synthesizerHandler = handler;
        initParameter();
    }
    private void initParameter()
    {
        if (isInit)
        {
            return;
        }
        synthesizer = SpeechSynthesizer.createSynthesizer(context, null);
        // 设置发音人，默认值为xiaoyan
        synthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        // 设置音量（0-100）
        synthesizer.setParameter(SpeechConstant.VOLUME, Constant.getVolume());
        // 设置语速（0-100）
        synthesizer.setParameter(SpeechConstant.SPEED, Constant.getSpeed());
        // synthesizer.setParameter(SpeechConstant.IST_AUDIO_PATH,
        // Constant.speechSynthesisSavePath);
        listener = new SynthesizerListener()
        {
            @Override
            public void onSpeakResumed()
            {
                // TODO Auto-generated method stub
                // status = SPEECH_SYNTHESIZER_ONGOING;
                setSynthesizerStatus(SPEECH_SYNTHESIZER_ONGOING);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_SYNTHESIER_FLAG;
                msg.what = SPEECH_SYNTHESIZER_ONGOING;
                synthesizerHandler.sendMessage(msg);
            }
            @Override
            public void onSpeakProgress(int arg0, int arg1, int arg2)
            {
                // TODO Auto-generated method stub
            }
            @Override
            public void onSpeakPaused()
            {
                // TODO Auto-generated method stub
                // status = SPEECH_SYNTHESIZER_PAUSE;
                setSynthesizerStatus(SPEECH_SYNTHESIZER_PAUSE);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_SYNTHESIER_FLAG;
                msg.what = SPEECH_SYNTHESIZER_PAUSE;
                synthesizerHandler.sendMessage(msg);
            }
            @Override
            public void onSpeakBegin()
            {
                // TODO Auto-generated method stub
                // status = SPEECH_SYNTHESIZER_BEGIN;
                setSynthesizerStatus(SPEECH_SYNTHESIZER_BEGIN);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_SYNTHESIER_FLAG;
                msg.what = SPEECH_SYNTHESIZER_BEGIN;
                if (speakText == null || speakText.trim().length() == 0)
                {
                    speakText = "没有听到你说话";
                }
                msg.arg2 = synthesizerHandler.getInterruptedFlag();
                msg.obj = speakText;
                synthesizerHandler.sendMessage(msg);
            }
            @Override
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
            {
                // TODO Auto-generated method stub
            }
            @Override
            public void onCompleted(SpeechError arg0)
            {
                // TODO Auto-generated method stub
                // status = SPEECH_SYNTHESIZER_OVER;
                setSynthesizerStatus(SPEECH_SYNTHESIZER_OVER);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_SYNTHESIER_FLAG;
                //用来标记是否将文本绘制到窗口
                msg.arg2 = synthesizerHandler.getInterruptedFlag();
                msg.what = SPEECH_SYNTHESIZER_OVER;
                synthesizerHandler.sendMessage(msg);
            }
            @Override
            public void onBufferProgress(int arg0, int arg1, int arg2,
                                         String arg3)
            {
                // TODO Auto-generated method stub
            }
        };
        isInit = true;
    }
    public void setSynthesizerStatus(int synthesizerStatus)
    {
        this.synthesizerStatus = synthesizerStatus;
    }
    public void startSpeak(String text)
    {
        if (!isInit)
        {
            throw new IllegalArgumentException("语音合成器还没有初始化");
        }
        else if (null == text || text.trim().equals(""))
        {
            Toast.makeText(context, "文本为空，无法朗读", Toast.LENGTH_LONG).show();
        }
        else
        {
            speakText = text;
            // 如果正在播放或者已经暂停，退出
            if (synthesizer.isSpeaking() || isPaused)
            {
                pauseSpeak();
            }
            synthesizer.startSpeaking(text, listener);
            isStart = true;
        }
    }
    public void pauseSpeak()
    {
        if (!isStart)
        {
            throw new IllegalArgumentException("语音还没有开始合成");
        }
        else
        {
            synthesizer.pauseSpeaking();
            isPaused = true;
        }
    }
    public void resumeSpeak()
    {
        if (isPaused)
        {
            synthesizer.resumeSpeaking();
            isPaused = false;
        }
    }
    public void stopSpeak()
    {
        if (!isStart)
        {
            return;
        }
        else
        {
            synthesizer.stopSpeaking();
        }
    }
    /**
     * 语音合成状态
     */
    public int getSynthesizerStatus()
    {
        return synthesizerStatus;
    }
    public void destroySpeak()
    {
        if (!isStart)
        {
            return;
        }
        else
        {
            isStart = false;
            isPaused = false;
            isInit = false;
            synthesizer.destroy();
            synthesizer = null;
        }
    }
}
