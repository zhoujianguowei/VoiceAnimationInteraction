package com.example.voiceanimationinteraction;
import android.os.Handler;
/**
 * 覆盖原有的Handler，处理语音听写和语音合成时候， 接收消息后根据消息类型，
 * 直接设置 动画效果,该软件的所有异步 处理必须使用该Handler
 */
public class SpeechHandler extends Handler
{

    public static final int ALLOW_DRAW = 0;
    public static final int DISALLOW_DRAW = 1;
    private volatile int interruptedFlag = ALLOW_DRAW;  //是否发送消息，用来提醒更新界面
    public static final int SPEECH_RECOGNIZER_FLAG = 1;  //语音听写标志
    public static final int SPEECH_SYNTHESIER_FLAG = 2;   //语音合成标志
    private int flag;
    public SpeechHandler(Callback speechMain, int flag)
    {
        super(speechMain);
        if (flag != SPEECH_RECOGNIZER_FLAG && flag != SPEECH_SYNTHESIER_FLAG)
        {
            throw new IllegalArgumentException("flag必须声明为语音听写和语音合成之一");
        }
        this.flag = flag;
        // TODO Auto-generated constructor stub
    }
    public SpeechHandler(int flag)
    {
        super();
        this.flag = flag;
    }
    public int getInterruptedFlag()
    {
        return interruptedFlag;
    }
    public void setInterruptedFlag(int interruptedFlag)
    {
        if (interruptedFlag != ALLOW_DRAW &&
                interruptedFlag != DISALLOW_DRAW)
            throw new IllegalArgumentException("参数只能是0或者1");
        this.interruptedFlag = interruptedFlag;
    }
    public int getFlag()
    {
        return flag;
    }
}