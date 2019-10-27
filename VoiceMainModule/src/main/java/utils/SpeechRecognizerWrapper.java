package utils;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.example.voiceanimationinteraction.SpeechHandler;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
/**
 * 语音听写,将语音信息转化为文本信息
 *
 * @author Administrator
 */
public class SpeechRecognizerWrapper
{

    private static final String TAG = SpeechRecognizerWrapper.class
            .getSimpleName();
    private static SpeechRecognizerWrapper recognizerWrapper;
    // 变量是否初始化
    private boolean isInit;
    // 是否已经开始听写
    private boolean isStart;
    private Context context;
    private InitListener initListener;
    private RecognizerListener recognizerListener;
    private SpeechRecognizer recognizer;
    // 语音识别出来的文本结果
    private StringBuilder result;
    // 主线程传过来的handler，当语音听写完成时通知主线程更新UI
    private SpeechHandler recognizerHandler;
    // 定义语音听写的状态
    // 语音听写开始，录音开始
    public static final int DICTATION_RECORD_BEGIN = 1;
    /*
     * 语音录音结束,onEndOfSpeech()被调用
     */
    public static final int DICTATION_RECORD_OVER = 2;
    // 语音听写结束,onResult最后一个语句被调用
    public static final int DICTATION_OVER = 3;
    // 语音听写出错
    public static final int DICTATION_ERROR = 4;
    /**
     * 语音听写正在等待云端返回结果
     */
    public static final int DICTATION_RECOGNIZING = 5;
    private volatile int recognizerStatus = DICTATION_OVER;
    public static SpeechRecognizerWrapper getSingleInstance(Context context,
                                                            SpeechHandler handler)
    {
        if (recognizerWrapper == null)
        {
            recognizerWrapper = new SpeechRecognizerWrapper(context, handler);
        }
        return recognizerWrapper;
    }
    /**
     * 重新设置handler，使得消息传送不在SpeechMainActivity 中进行
     * SpeechMainActivity每次会话结束必须重置handler
     */
    private SpeechRecognizerWrapper(Context context,
                                    SpeechHandler handler)
    {
        this.context = context;
        this.recognizerHandler = handler;
        initParameter();
    }
    public void setSpeechRecognizerHandler(SpeechHandler speechHandler)
    {
        recognizerHandler = speechHandler;
    }
    private void initParameter()
    {
        if (isInit)
        {
            return;
        }
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "="
                + Constant.appid);
        initListener = new InitListener()
        {
            @Override
            public void onInit(int arg0)
            {
                // TODO Auto-generated method stub
                if (arg0 != 0)
                {
                    Toast toast = new Toast(context);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    // toast.makeText(SpeechMainActivity.this, "语音识别出错",
                    // Toast.LENGTH_LONG).show();
                    toast.setText("语音识别出错");
                    toast.setDuration(Toast.LENGTH_LONG);
                    // toast.show();
                }
            }
        };
        recognizerListener = new RecognizerListener()
        {
            @Override
            public void onVolumeChanged(int arg0)
            {
            }
            @Override
            public void onResult(RecognizerResult arg0, boolean arg1)
            {
                // TODO Auto-generated method stub
                // String resultString = arg0.getResultString();
                result.append(
                        JsonParser.parseIatResult(arg0.getResultString()));
                /**
                 * 最后一条语句
                 */
                if (arg1 == true)
                {
                    isStart = false;
//                    recognizerStatus = DICTATION_OVER;
                    setDictationStatus(DICTATION_OVER);
                    Message msg = Message.obtain();
                    msg.arg1 = SpeechHandler.SPEECH_RECOGNIZER_FLAG;
                    msg.what = SpeechRecognizerWrapper.DICTATION_OVER;
                    /**
                     * 用来标记是否将语音合成的文本绘制到窗口
                     */
                    msg.arg2 = recognizerHandler.getInterruptedFlag();
                    if (!TextUtils.isEmpty(result))
                    {
                        msg.obj = result;
                        recognizerHandler.sendMessage(msg);
                    }
                }
            }
            @Override
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
            {
                // TODO Auto-generated method stub
            }
            /**
             * 停止录音，设置后置端点当用户一段时间没有语音输入，默认回调此函数
             * 当用户声音过小或者没有说话时候，不会回调onResult方法,在onEndSpeech 进行处理
             */
            @Override
            public void onError(SpeechError arg0)
            {
                setDictationStatus(DICTATION_ERROR);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_RECOGNIZER_FLAG;
                msg.what = DICTATION_ERROR;
                String errorDes = arg0.getErrorDescription();
                msg.obj = !TextUtils.isEmpty(errorDes) ? errorDes : "您好像没有说话呀";
                recognizerHandler.sendMessage(msg);
            }
            @Override
            public void onEndOfSpeech()
            {
                // TODO Auto-generated method stub
                setDictationStatus(DICTATION_RECORD_OVER);
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_RECOGNIZER_FLAG;
                msg.arg2 = recognizerHandler.getInterruptedFlag();
                msg.what = DICTATION_RECORD_OVER;
                recognizerHandler.sendMessage(msg);
            }
            /**
             * 语音听写开始,开始录音，采用遍录音边发送的方式
             */
            @Override
            public void onBeginOfSpeech()
            {
                // TODO Auto-generated method stub
                setDictationStatus(DICTATION_RECORD_BEGIN);
                result = new StringBuilder();
                isStart = true;
                Message msg = Message.obtain();
                msg.arg1 = SpeechHandler.SPEECH_RECOGNIZER_FLAG;
                msg.what = DICTATION_RECORD_BEGIN;
                recognizerHandler.sendMessage(msg);
            }
        };
        recognizer = SpeechRecognizer.createRecognizer(context, initListener);
        /**
         * 接受的语言是普通话
         */
        recognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 获取网络状态
        recognizer.setParameter(SpeechConstant.ASR_NET_PERF, "true");
        recognizer.setParameter(SpeechConstant.LANGUAGE, "zh-cn");
        // 语音识别应用领域（：iat，search，video，poi，music）
        recognizer.setParameter(SpeechConstant.DOMAIN, "iat");
        // 设置编码类型
        recognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置是否有标点
        recognizer.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置前端点超时
        recognizer.setParameter(SpeechConstant.VAD_BOS, Constant.getVadBox());
        // 设置后端点超时
        recognizer.setParameter(SpeechConstant.VAD_EOS, Constant.getVadEox());
        //设置音频保存路径
        recognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                "/sdcard/iflytek");
        isInit = true;
    }
    public void setDictationStatus(int status)
    {
        recognizerStatus = status;
    }
    /**
     * 在使用之前必须调用initParameter(Context context)进行初始化
     */
    public void startDictation()
    {
        if (!isInit)
        {
            throw new IllegalArgumentException("语音听写识别器还没有初始化");
        }
        else
        {
            recognizer.startListening(recognizerListener);
        }
    }
    public void stopDictation()
    {
        if (!isStart)
        {
            return;
        }
        else
        {
            Message msg = Message.obtain();
            msg.arg1 = SpeechHandler.SPEECH_RECOGNIZER_FLAG;
            msg.what = DICTATION_RECOGNIZING;
            recognizerHandler.sendMessage(msg);
            /**
             * 只会调用SpeechRecognizer的onEndOfSpeech()，表示录音结束 如果有声音才会调用onResult()。
             */
            recognizer.stopListening();
        }
    }
    /**
     * 返回语音听写当前状态
     */
    public int getDictationStatus()
    {
        return recognizerStatus;
    }
    public void destroyDictation()
    {
        if (recognizer != null)
        {
            // 停止录音
            recognizer.stopListening();
            recognizer.destroy();
            recognizer = null;
            isInit = false;
            recognizerListener = null;
            initListener = null;
        }
    }
}
