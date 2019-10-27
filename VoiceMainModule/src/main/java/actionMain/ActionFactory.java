package actionMain;
public class ActionFactory
{

    private ActionImpl action;
    public ActionFactory()
    {
    }
    public ActionImpl buildAction(String dictation)
    {
        action = null;
        if (dictation.contains("电话"))
        {
            action = new TelAction();
        }
        else if (dictation.contains("天气"))
        {
            action = new QueryWeatherAction();
        }
        else if (dictation.contains("wifi"))
        {
            action = new OpenWifi();
        }
        // 打开手机应用程序
        else if (dictation.startsWith("打开"))
        {
            action = new OpenApplication();
        }
        else if (dictation.contains("油价"))
        {
            action = new QueryOilAction();
        }
        else if (dictation.contains("查询") || dictation.contains("查找"))
        {
            action = new QueryContactAction();
        }
        else if (dictation.contains("播放音乐"))
        {
            action = new PlayMusicAction();
        }
        else if (dictation.contains("短信"))
        {
            action = new SendSmsAction();
        }
        return action;
    }
}
