package actionMain;
import android.os.Handler;
import android.os.Message;

import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import utils.SpeechRecognizerWrapper;
import utils.SpeechSynthesizerWrapper;
/**
 * Created by Administrator on 2015/6/5.
 * 主要功能是等待用户说出发送到短信或者拨打电话的联系人信息
 */
public class WaitingForPhone extends SupAction
{

    private Handler handler;
    public WaitingForPhone(Handler handler)
    {
        this.handler = handler;
    }
    public void waitingForName(final String info)
    {
        recognizerHandler = new SpeechHandler(
                SpeechHandler.SPEECH_RECOGNIZER_FLAG)
        {
            @Override
            public void handleMessage(Message msg)
            {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                speechMain.handleResMessage(msg, false);
                // 语音听写结束
                if (recognizerWrapper.getDictationStatus() ==
                        SpeechRecognizerWrapper
                                .DICTATION_OVER)
                {
                    Message resMsg = Message.obtain();
                    resMsg.obj = msg.obj;
                    handler.sendMessage(resMsg);
                }
            }
        };
        Thread speechSynthesizerThread = new Thread()
        {
            public void run()
            {
                lock.lock();
                try
                {
                    if (speechMain.getSessionStatus() ==
                            SpeechMainActivity.SESSION_ONGOING)
                    {
                        synthesizerWrapper.startSpeak(info);
                        synthesizerWrapper.setSynthesizerStatus(
                                SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_BEGIN);
                        // 等待用户语音合成完成
                        while (synthesizerWrapper.getSynthesizerStatus() !=
                                SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER)
                        {
                            Thread.sleep(100);
                        }
                    }
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
        Thread speechRecognizerThread = new Thread()
        {
            public void run()
            {
                lock.lock();
                try
                {
                    if (speechMain.getSessionStatus() !=
                            SpeechMainActivity.SESSION_OVER)
                    {
                        //                        condition.await();
                        // 开始语音听写
                        recognizerWrapper.startDictation();
                    }
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
        recognizerWrapper.setSpeechRecognizerHandler(recognizerHandler);
        speechSynthesizerThread.start();
        try
        {
            Thread.sleep(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        speechRecognizerThread.start();
    }
}
