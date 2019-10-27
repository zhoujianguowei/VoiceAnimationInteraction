package actionMain;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.voiceanimationinteraction.ContactList;
import com.example.voiceanimationinteraction.R;
import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entity.Contact;
import utils.Constant;
/**
 * 由于中国多音字的影响，采用拼音匹配的方式
 * 即现将中文名字提取出来然后与通讯录匹配 拨打电话相应的动作
 *
 * @author Administrator
 */
public class TelAction extends SupAction implements Handler.Callback
{

    ArrayList<Contact> contacts;
    // 存储手机号，联系人信息
    HashMap<String, String> phoneUser;
    /**
     * 语音听写线程
     */
    Thread speechRecognizerThread;
    /**
     * 语音合成线程
     */
    Thread speechSynthesizerThread;
    // 语音听写结果
    private volatile String dictionResult;
    private Handler waitingForPhoneHandler;
    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        // TODO Auto-generated method stub
        super.handleDictation(context, dictation);
        String phoneName = null;
        String reg1 = "给?(\\S+)打电话";
        String reg2 = "打电话给?(\\S+)";
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
//            waitingForPhoneName();
            waitingForPhoneHandler = new Handler(this);
            new WaitingForPhone(waitingForPhoneHandler).waitingForName
                    ("打电话给谁?");
        }
        else
        {
            call(speechMain, phoneName);
        }
    }
    /**
     * 根据联系人拨打电话
     */
    private void call(Context context, String phoneName)
    {
        if (phoneName == null || phoneName.trim().length() == 0)
        {
            return;
        }
        contacts = ContactList.getContactsList(context, phoneName);
        // 查找不到联系人,此次回话结束
        if (contacts.size() == 0)
        {
            synthesizerWrapper.startSpeak("查找不到联系人" + phoneName);
            speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
        }
        else
        {
            phoneUser = new HashMap<String, String>();
            Iterator<Contact> iterator = contacts.iterator();
            while (iterator.hasNext())
            {
                Contact contact = iterator.next();
                HashSet<String> phoneSet = contact.getPhones();
                String userName = contact.getName();
                String[] phoneArray = new String[phoneSet.size()];
                phoneSet.toArray(phoneArray);
                for (String phone : phoneArray)
                    phoneUser.put(phone, userName);
            }
            int size = phoneUser.size();
            // 只有一个联系人
            if (size == 1)
            {
                Set<String> keySet = phoneUser.keySet();
                Iterator<String> keySetIterator = keySet.iterator();
                if (keySetIterator.hasNext())
                {
                    String phone = keySetIterator.next();
                    callPhone(phone, phoneUser.get(phone));
                }
            }
            // 联系人电话至少2个
            else
            {
                final ListView lv = new ListView(speechMain);
                List<HashMap<String, String>> mapList =
                        new ArrayList<HashMap<String, String>>();
                Set<String> keySet = phoneUser.keySet();
                Iterator<String> keySetIterator = keySet.iterator();
                // 序列号
                int order = 1;
                while (keySetIterator.hasNext())
                {
                    String key = keySetIterator.next();
                    HashMap<String, String> map = new HashMap<String, String>();
                    // map.put(key, phoneUser.get(key));
                    map.put("order", "序号:" + order);
                    map.put("user", phoneUser.get(key));
                    map.put("phone", key);
                    mapList.add(map);
                    order++;
                }
                SimpleAdapter adater = new SimpleAdapter(speechMain, mapList,
                        R.layout.dial_cell,
                        new String[]{"order", "user", "phone"}, new int[]{
                        R.id.dialOrder, R.id.phoneName, R.id.phone})
                {
                    public View getView(int position,
                                        View convertView,
                                        android.view.ViewGroup parent)
                    {
                        convertView = LayoutInflater.from(speechMain).inflate(
                                R.layout.dial_cell, null);
                        final TextView dialOrder = (TextView) convertView
                                .findViewById(R.id.dialOrder);
                        final TextView phoneName = (TextView) convertView
                                .findViewById(R.id.phoneName);
                        final TextView phone = (TextView) convertView
                                .findViewById(R.id.phone);
                        ImageButton dial = (ImageButton) convertView
                                .findViewById(R.id.dialIb);
                        HashMap<String, String> map =
                                (HashMap<String, String>) getItem(position);
                        dialOrder.setText(map.get("order"));
                        phoneName.setText(Html.fromHtml("<font color='#ff0000'>"
                                + map.get("user") + "</font>"));
                        phone.setText(map.get("phone"));
                        dial.setOnClickListener(new OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                // TODO Auto-generated method stub
                                speechMain.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // TODO Auto-generated method stub'
                                        callPhone(phone.getText().toString(),
                                                phoneName.getText().toString());
                                    }
                                });
                            }
                        });
                        // dialOrder.setText();
                        return convertView;
                    }
                };
                lv.setAdapter(adater);
                lv.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) (Constant.screenWidth / 1.1),
                        (int) (Constant.screenHeight / 5)));
                synthesizerWrapper.startSpeak("查询到联系人" + phoneName + "的"
                        + adater.getCount() + "个号码，请手动拨号");
                if (recognizerHandler == null)
                {
                    recognizerHandler = new SpeechHandler(speechMain,
                            SpeechHandler.SPEECH_RECOGNIZER_FLAG);
                }
                recognizerHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        speechMain.addViewToWindow(lv);
                    }
                }, 3000);
            }
            speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
        }
    }
    /**
     * 根据姓名和手机号拨打电话
     */
    public static void callPhone(final String phone, String phoneName)
    {
        synthesizerWrapper.startSpeak("正在拨打电话给：" + phoneName);
        if (recognizerHandler == null)
        {
            recognizerHandler = new SpeechHandler(speechMain,
                    SpeechHandler.SPEECH_RECOGNIZER_FLAG);
        }
        // 2秒钟之后拨打电话
        recognizerHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phone));
                speechMain.startActivity(intent);
            }
        }, 2000);
    }
    @Override public boolean handleMessage(Message msg)
    {
        if (msg.obj != null)
        {
            call(speechMain, msg.obj.toString());
        }
        return true;
    }
}
