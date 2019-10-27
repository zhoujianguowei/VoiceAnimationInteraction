package actionMain;
import android.os.Message;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.voiceanimationinteraction.ContactList;
import com.example.voiceanimationinteraction.SpeechHandler;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.QueryContactsAdapter;
import entity.SimpleContact;
import utils.Constant;
import utils.PinyinUtils;
import utils.SpeechSynthesizerWrapper;
/**
 * Created by Administrator on 2015/5/30.
 */
public class QueryContactAction extends SupAction
{

    public final static String TAG = QueryContactAction.class.getSimpleName();
    private boolean isLvAdded = false;
    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        dictation = PinyinUtils.filterPunctuation(dictation);
        String reg1 = "查[找|询](联系人)?(\\S+)";
        Matcher matcher = Pattern.compile(reg1).matcher(dictation);
        String contactName = null;
        if (matcher.find())
        {
            contactName = matcher.group(matcher.groupCount());
        }
        else
            synthesizerWrapper.startSpeak("查询什么?");
        if (!TextUtils.isEmpty(contactName))
        {
//            Log.e(TAG,contactName);
            ArrayList<SimpleContact> simpleContacts = ContactList
                    .getSimpleContactList(speechMain, contactName);
            if (simpleContacts.isEmpty())
            {
                synthesizerWrapper.startSpeak("没有查询到联系人" + contactName);
                speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
                return;
            }
            final ListView queryContactLv = new ListView(speechMain);
            LinearLayout.LayoutParams lvParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                            .LayoutParams.WRAP_CONTENT);
            lvParams.height = (int) (Constant.screenWidth * 1.0f / 4);
            queryContactLv
                    .setLayoutParams
                            (lvParams);
            queryContactLv.setAdapter(new QueryContactsAdapter(speechMain,
                    simpleContacts));
            queryContactLv.setDividerHeight(0);
            StringBuilder queryBuilder = new StringBuilder
                    ("共查询到联系人" + contactName + "的" + simpleContacts.size()
                            + "条记录");
            synthesizerHandler =
                    new SpeechHandler(SpeechHandler.SPEECH_SYNTHESIER_FLAG)
                    {
                        @Override public void handleMessage(Message msg)
                        {
                            super.handleMessage(msg);
                            speechMain.handleResMessage(msg, false);
                            if (msg.what ==
                                    SpeechSynthesizerWrapper
                                            .SPEECH_SYNTHESIZER_BEGIN && !isLvAdded)
                            {
                                speechMain.addViewToWindow(queryContactLv);
                                isLvAdded = true;
                            }
                        }
                    };
            synthesizerWrapper.setSynthesizerHandler(synthesizerHandler);
//            synthesizerWrapper.startSpeak;
            synthesizerWrapper.startSpeak(queryBuilder.toString());
        }
        speechMain.setSessionStatus(SpeechMainActivity.SESSION_OVER);
    }
}
