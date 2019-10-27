package actionMain;
import android.text.TextUtils;

import com.example.voiceanimationinteraction.SpeechMainActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.OilService;
import utils.Constant;
public class QueryOilAction extends SupAction
{

    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        // TODO Auto-generated method stub
        super.handleDictation(context, dictation);
        String province = null;
        String reg = "(\\S+)(今日)?油价";
        Matcher matcher = Pattern.compile(reg).matcher(dictation);
        if (matcher.find())
        {
            province = matcher.group(1);
        }
        if (province != null && province.contains("今日"))
        {
            int provinceIndex = province.indexOf("今日");
            province = province.substring(0, provinceIndex);
        }
        /**
         * 删除省|市字段
         */
        if (!TextUtils.isEmpty(province) && (province.endsWith("省") || province
                .endsWith
                        ("市")))
        {
            province = province.substring(0, province.length() - 1);
        }
        province = TextUtils.isEmpty(province) ? Constant.getCurrentProvince() :
                province;
        OilService.queryOilPrice(context, province);
    }
}
