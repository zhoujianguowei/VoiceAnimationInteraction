package actionMain;
import com.example.voiceanimationinteraction.SpeechMainActivity;

import services.SearchMusicService;
/**
 * Created by Administrator on 2015/6/3.
 */
public class PlayMusicAction extends SupAction
{

    @Override
    public void handleDictation(SpeechMainActivity context, String dictation)
    {
        super.handleDictation(context, dictation);
        context.showProgressDialog("正在搜索音频文件");
        SearchMusicService.startSearchAudio(context);
    }
}
