package services;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import com.zhoujianguo.assistantTool.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Administrator on 2015/6/3.
 */
public class SearchMusicService extends IntentService
{

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    private static final String ACTION_SEARCH_AUDIO = "services" +
            ".ACTION_SEARCH_AUDIO";
    public final static String EXTRA_AUDIOS = "extra_audio";
    public static final String BROADCAST_SEARCH_AUDIO = "services" +
            ".BROADCAST_SERACH_AUDIO";
    private static ArrayList<String> musics = new ArrayList<>();//保存上次搜索到的文件
    String[] audioExtensionNames = new String[]{"mp3", "ape", "flac"};
    FileUtils fileUtils;
    public SearchMusicService(String name)
    {
        super(name);
        fileUtils = new FileUtils();
    }
    public SearchMusicService()
    {
        this("audio");
    }
    public static void startSearchAudio(Context context)
    {
        Intent intent = new Intent(context, SearchMusicService.class);
        intent.setAction(ACTION_SEARCH_AUDIO);
        context.startService(intent);
    }
    @Override protected void onHandleIntent(Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_SEARCH_AUDIO:
                executeSearchAudio();
                break;
        }
    }
    private void executeSearchAudio()
    {
        if (musics != null && !musics.isEmpty())
        {
            sendSearchAudioBroadcast(musics);
            return;
        }
        else
        {
            if (Environment.getExternalStorageState().equals(Environment
                    .MEDIA_MOUNTED))
            {
                List<File> musicFileList = fileUtils.getFiles(Environment
                        .getExternalStorageDirectory(), audioExtensionNames);
                for (File musicFile : musicFileList)
                    musics.add(musicFile.getAbsolutePath());
            }
        }
        sendSearchAudioBroadcast(musics);
    }
    private void sendSearchAudioBroadcast(ArrayList<String> musics)
    {
        Intent intent = new Intent(BROADCAST_SEARCH_AUDIO);
//        intent.setAction()
        intent.putExtra("status", "fail");
        if (musics != null && !musics.isEmpty())
        {
            intent.putExtra("status", "success");
            intent.putStringArrayListExtra(EXTRA_AUDIOS, musics);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
