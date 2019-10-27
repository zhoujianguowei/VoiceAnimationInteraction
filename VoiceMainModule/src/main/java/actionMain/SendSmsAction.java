package actionMain;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.voiceanimationinteraction.ContactList;
import com.example.voiceanimationinteraction.R;
import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.SendSmsAdapter;
import entity.SimpleContact;
import utils.Constant;
import utils.PinyinUtils;
import utils.SpeechRecognizerWrapper;
import utils.SpeechSynthesizerWrapper;
/**
 * 通过语音发送短信
 *
 * @author Administrator
 */
public class SendSmsAction extends SupAction implements Handler.Callback
{

    private ArrayList<SimpleContact> simpleContacts;
    private volatile StringBuilder smsContent = new StringBuilder();
    private boolean isContactListAdded = false;
    private ListView smsLV;
    private TextView recContactName;//收件人姓名
    private TextView sendSmsContent;//发送文本内容
    private Button sendSmsBt;  //短信发送按钮
    private Button cancelSmsBt;//短信取消发送按钮
    private Thread recognizerThread;
    private Thread synthesizerThread;
    private String targetName;
    private String targetPhone;
    private volatile boolean isInterrupted;//线程终止标志,用来标记该轮短信发送完成
    private volatile boolean isRecognizerOver;//标志一次语音听写是否完成
    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        // TODO Auto-generated method stub
        String phoneName = null;
        String reg1 = "给?(\\S+)发短信";
        String reg2 = "发短信给?(\\S+)";
        Pattern pattern1 = Pattern.compile(reg1, Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile(reg2, Pattern.CASE_INSENSITIVE);
        Matcher matcher1 = pattern1.matcher(dictation);
        Matcher matcher2 = pattern2.matcher(dictation);
        if (matcher1.find())
        {
            phoneName = matcher1.group(1);
        }
        else if (matcher2.find())
        {
            phoneName = matcher2.group(1);
        }
        if (TextUtils.isEmpty(phoneName))
        {
            new WaitingForPhone(new Handler(this)).waitingForName("发短信给谁?");
        }
        else
        {
            handleSmsPhoneName(phoneName);
        }
    }
    private void handleSmsPhoneName(String phoneName)
    {
        simpleContacts =
                ContactList.getSimpleContactList(speechMain, phoneName);
        synthesizerHandler =
                new SpeechHandler(SpeechHandler.SPEECH_SYNTHESIER_FLAG)
                {
                    @Override public void handleMessage(Message msg)
                    {
                        super.handleMessage(msg);
                        speechMain.handleResMessage(msg, false);
                        if (msg.what == SpeechSynthesizerWrapper
                                .SPEECH_SYNTHESIZER_BEGIN &&
                                simpleContacts.size()
                                        > 1 && !isContactListAdded)
                        {
                            speechMain.addViewToWindow(smsLV);
                            isContactListAdded = true;
                        }
                    }
                };
        synthesizerWrapper.setSynthesizerHandler(synthesizerHandler);
        if (simpleContacts.size() == 0)
        {
            synthesizerWrapper.startSpeak("没有联系人" + phoneName);
            speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
            return;
        }
        else
        {
            if (simpleContacts.size() > 1)
            {
                synthesizerWrapper.startSpeak
                        ("共查询到联系人" + phoneName + "的" + simpleContacts.size()
                                + "条记录,请手动发送短信");
                smsLV = new ListView(speechMain);
                LinearLayout.LayoutParams lvParams =
                        new LinearLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                                        .LayoutParams.WRAP_CONTENT);
                lvParams.height = (int) (Constant.screenHeight * 1.0f / 4);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                speechMain.getWindowManager()
                        .getDefaultDisplay().getMetrics(displayMetrics);
                lvParams.leftMargin = (int) TypedValue.applyDimension(TypedValue
                        .COMPLEX_UNIT_DIP, 20, displayMetrics);
                lvParams.rightMargin = lvParams.leftMargin;
                smsLV.setLayoutParams(lvParams);
                smsLV.setAdapter(
                        new SendSmsAdapter(simpleContacts, speechMain, this));
            }
            else
            {
                sendMsg(simpleContacts.get(0).getPhone(), simpleContacts.get
                        (0).getName());
            }
        }
    }
    public void sendMsgWithContent(String phone, String name, String
            smsContent)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, smsContent, null, null);
        synthesizerHandler.setInterruptedFlag(SpeechHandler.ALLOW_DRAW);
        synthesizerWrapper.startSpeak("短信已发送");
        speechMain.setSessionStatus(
                SpeechMainActivity.SESSION_OVER);
        ((ViewGroup) sendSmsBt.getParent()).setVisibility(View.GONE);
    }
    public void sendMsg(String phone, String name)
    {
        targetPhone = phone;
        targetName = name;
        speechMain.setSessionStatus(SpeechMainActivity.SESSION_ONGOING);
        speechMain.addViewToWindow(initialComponents(phone, name));
        recognizerHandler =
                new SpeechHandler(SpeechHandler.SPEECH_RECOGNIZER_FLAG)
                {
                    @Override public void handleMessage(Message msg)
                    {
                        super.handleMessage(msg);
                        speechMain.handleResMessage(msg, false);
                        if (recognizerWrapper.getDictationStatus()
                                == SpeechRecognizerWrapper.DICTATION_ERROR)
                        {
                            isInterrupted = true;
                            return;
                        }
                        if (recognizerWrapper.getDictationStatus() ==
                                SpeechRecognizerWrapper.DICTATION_OVER && msg
                                .obj != null)
                        {
                            String dictationStr = msg.obj.toString();
                            if (PinyinUtils.filterPunctuation(dictationStr)
                                    .equals("发送"))
                            {
                                sendMsgWithContent(targetPhone, targetName,
                                        smsContent.toString());
                                isInterrupted = true;
                            }
                            else if (PinyinUtils.filterPunctuation
                                    (dictationStr).equals("取消"))
                            {
                                synthesizerHandler.setInterruptedFlag
                                        (SpeechHandler.ALLOW_DRAW);
                                synthesizerWrapper.startSpeak("已取消短信发送");
                                speechMain.setSessionStatus
                                        (SpeechMainActivity.SESSION_OVER);
                                ((ViewGroup) cancelSmsBt.getParent())
                                        .setVisibility(View.GONE);
                                isInterrupted = true;
                            }
                            else
                            {
                                smsContent.append(dictationStr);
                            }
                            sendSmsContent.setText("内容：" + smsContent);
                            isRecognizerOver = true;
                        }
                    }
                };
        //语音听写内容不要绘制到窗口
        recognizerHandler.setInterruptedFlag(SpeechHandler.DISALLOW_DRAW);
        synthesizerHandler.setInterruptedFlag(SpeechHandler.DISALLOW_DRAW);
        recognizerWrapper.setSpeechRecognizerHandler(recognizerHandler);
        recognizerThread = new Thread()
        {
            @Override public void run()
            {
                super.run();
                lock.lock();
                try
                {
                    while (!isInterrupted)
                    {
                        recognizerWrapper.startDictation();
                        recognizerWrapper.setDictationStatus
                                (SpeechRecognizerWrapper.DICTATION_RECORD_BEGIN);
                        isRecognizerOver = false;
                        while (recognizerWrapper.getDictationStatus() !=
                                SpeechRecognizerWrapper.DICTATION_OVER)
                            Thread.sleep(100);
                        condition.signal();
                        condition.await();
                    }
                    condition.signalAll();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
        synthesizerThread = new Thread()
        {
            @Override public void run()
            {
                super.run();
                lock.lock();
                try
                {
                    synthesizerWrapper.startSpeak("语音输入短信内容");
                    synthesizerWrapper.setSynthesizerStatus
                            (SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_BEGIN);
                    while (synthesizerWrapper.getSynthesizerStatus() !=
                            SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER)
                        Thread.sleep(100);
                    condition.await();
                    while (!isInterrupted)
                    {
                        while (!isRecognizerOver)
                            Thread.sleep(100);
                        if (isInterrupted)
                        {
                            break;
                        }
                        synthesizerWrapper.startSpeak
                                ("短信内容：" + smsContent + "\t\n" + "发送还是取消");
                        synthesizerWrapper.setSynthesizerStatus
                                (SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_BEGIN);
                        while (synthesizerWrapper.getSynthesizerStatus() !=
                                SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER)
                            Thread.sleep(100);
                        //等待用户反应
                        Thread.sleep(500);
                        condition.signal();
                        condition.await();
                    }
                    condition.signalAll();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
        synthesizerThread.start();
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        recognizerThread.start();
//        speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
    }
    /**
     * 初始化短信发送View视图
     */
    private View initialComponents(String phone, String name)
    {
        View convertView = LayoutInflater.from(speechMain).inflate(R.layout
                .send_sms_dialog, null);
        recContactName =
                (TextView) convertView.findViewById(R.id.rec_phone_name);
        sendSmsContent =
                (TextView) convertView.findViewById(R.id.send_sms_content);
        sendSmsBt = (Button) convertView.findViewById(R.id.send_sms_bt);
        cancelSmsBt = (Button) convertView.findViewById(R.id.cancel_sms_bt);
        LinearLayout.LayoutParams sendSmsBtParams =
                (LinearLayout.LayoutParams) sendSmsBt.getLayoutParams();
        sendSmsBtParams.height = (int) (Constant.screenWidth * 1.0f / 10);
        LinearLayout.LayoutParams cancelSmsBtParams =
                (LinearLayout.LayoutParams) cancelSmsBt
                        .getLayoutParams();
        cancelSmsBtParams.height = sendSmsBtParams.height;
        sendSmsBt.setLayoutParams(sendSmsBtParams);
        cancelSmsBt.setLayoutParams(cancelSmsBtParams);
        recContactName.append(name + "(" + phone + ")");
        sendSmsBt.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                sendMsgWithContent(targetPhone, targetName,
                        smsContent.toString());
                isInterrupted = true;
                isRecognizerOver = true;
                ((View) v.getParent()).setVisibility(View.GONE);
            }
        });
        cancelSmsBt.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                synthesizerHandler.setInterruptedFlag(
                        SpeechHandler.ALLOW_DRAW);
                synthesizerWrapper.startSpeak("已取消短信发送");
                speechMain.setSessionStatus
                        (SpeechMainActivity.SESSION_OVER);
                isInterrupted = true;
                isRecognizerOver = true;
                ((View) v.getParent()).setVisibility(View.GONE);
            }
        });
        return convertView;
    }
    @Override public boolean handleMessage(Message msg)
    {
        handleSmsPhoneName(msg.obj.toString());
        return true;
    }
}
