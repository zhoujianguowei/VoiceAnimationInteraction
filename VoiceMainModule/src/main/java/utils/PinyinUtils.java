package utils;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
public class PinyinUtils
{

    /**
     * 汉字转换为汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines)
    {
        String pinyinName = "";
        // 转化为字符
        char[] nameChar = chines.toCharArray();
        // for(int i=0;i<nameChar.length;i++){
        // System.out.println(nameChar[i]);
        // }
        // 汉语拼音格式输出类
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出设置,大小写,音标方式等
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 忽略拼音音节
        // defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        // 示例输出：zhou1jian4guo2shi4yi1ge4wei3da4de5ren2
        for (int i = 0; i < nameChar.length; i++)
        {
            // 如果是中文
            if (nameChar[i] > 256)
            {
                try
                {
                    pinyinName +=
                            PinyinHelper.toHanyuPinyinStringArray(nameChar[i],
                                    defaultFormat)[0].charAt(0);
                }
                catch (BadHanyuPinyinOutputFormatCombination e)
                {
                    e.printStackTrace();
                }
            }
            else
            {// 为英文字符
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }
    /**
     * @param useTone 是否设置音调
     */
    public static String converterToSpell(String chines, boolean useTone)
    {
        chines = filterPunctuation(chines);
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        if (!useTone)
        {
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        }
        else
        {
            for (int i = 0; i < nameChar.length; i++)
            {
                if (nameChar[i] > 256)
                {
                    try
                    {
                        //由于汉字存在多音字的情况，所以转换过来的是拼音数组
                        pinyinName += PinyinHelper
                                .toHanyuPinyinStringArray(nameChar[i],
                                        defaultFormat)[0];
                    }
                    catch (BadHanyuPinyinOutputFormatCombination e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    pinyinName += nameChar[i];
                }
            }
        }
        return pinyinName.toLowerCase();
    }
    /**
     * 汉字转换位汉语拼音，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToSpell(String chines)
    {
        return converterToSpell(chines, true);
    }
    /**
     * 过滤掉标点符号
     */
    public static String filterPunctuation(String chines)
    {
        // TODO Auto-generated method stub
        StringBuilder builder = new StringBuilder();
        String punctuations = ",.?!-，。？！<>《》[]{}()（）【】” ‘’\"\'?!-";
        for (int i = 0; i < chines.length(); i++)
        {
            String charCode = chines.substring(i, i + 1);
            if (!punctuations.contains(charCode))
            {
                builder.append(charCode);
            }
        }
        return builder.toString().trim();
    }
}