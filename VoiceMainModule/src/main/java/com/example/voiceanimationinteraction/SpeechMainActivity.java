package com.example.voiceanimationinteraction;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klicen.citychoosermodule.ScreenSize;

import java.util.ArrayList;
import java.util.Random;

import actionMain.ActionFactory;
import actionMain.SupAction;
import entity.ConversationCell;
import entity.MyScrollView;
import entity.NetInfo;
import entity.OilPrice;
import entity.RunningProcess;
import entity.Weather;
import services.OilService;
import services.SearchMusicService;
import services.WakeUpService;
import services.WeatherService;
import utils.Constant;
import utils.PinyinUtils;
import utils.SpeechRecognizerWrapper;
import utils.SpeechSynthesizerWrapper;
public class SpeechMainActivity extends SupActivity implements OnTouchListener,
        Handler.Callback, ConversationCell.Callback
{

    private static final String TAG = SpeechMainActivity.class.getSimpleName();
    private static final String MISUNDERSTANDING = "对不起，我听不懂，请再说一遍";
    ProgressDialog dialog;
    // 对话框父组件
    LinearLayout contentContainer;
    FrameLayout controlIvContainer;//动画的父容器
    ImageView controlIv;
    Button controlBt;
    MyScrollView scrollView;
    AnimationDrawable frameAnimation;
    /**
     * 语音听写正在云端识别
     */
    RotateAnimation rotateAnimation;
    // 记录controlIv的尺寸，防止设置动画背景颜色时候变形
    private int controlIvHeight;
    private int controlIvWidth;
    private LayoutParams params;
    // 标志位，用于标记对话状态
    public final static int SESSION_ONGOING = 1; // 此次回话正在进行
    public final static int SESSION_OVER = 2; // 会话结束
    private static volatile int sessionStatus = SESSION_OVER;
    private ImageButton setting;
    private ImageButton help;
    QueryOilBroadcast oilBroadcast;
    QueryWeatherBroadcast weatherBroadcast;
    PlayMusicBroadcast playMusicBroadcast;
    private SpeechRecognizerWrapper recognizerWrapper;
    private SpeechSynthesizerWrapper synthesizerWrapper;
    ActionFactory actionFactory;
    private static final String HELP_TAG = "help_tag";
    private static final String SETTING_TAG = "setting_tag";
    private static final String ANIMATION_TAG = "animation_tag";
    private static final String ANIMATION_CONTAINER_TAG = "animation_container";
    private SupAction supAction;
    private ProgressDialog progressDialog;
    private ConversationCell lastRightCell;
    private ConversationCell lastLeftCell;
    /**
     * 实际文本的高度
     */
    public void showProgressDialog(String msg)
    {
        if (progressDialog == null)
        {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }
    public void dismissProgressDialog()
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            progressDialog.cancel();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstaceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstaceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        scrollView = (MyScrollView) findViewById(R.id.scrollView);
        scrollView.setDownwardScroll(true);
        scrollView.setUpwardScroll(true);
        contentContainer = (LinearLayout) findViewById(R.id.contentContainer);
        controlIvContainer =
                (FrameLayout) findViewById(R.id.controlIvContainer);
        controlIvContainer.setOnTouchListener(this);
        controlIv = (ImageView) findViewById(R.id.controlAnim);
        controlBt = (Button) findViewById(R.id.controlShow);
        setting = (ImageButton) findViewById(R.id.mainSetting);
        help = (ImageButton) findViewById(R.id.mainHelp);
        controlIv.setTag(ANIMATION_TAG);
        controlBt.setTag(ANIMATION_TAG);
        help.setTag(HELP_TAG);
        setting.setTag(SETTING_TAG);
        controlIvContainer.setTag(ANIMATION_CONTAINER_TAG);
        actionFactory = new ActionFactory();
        Constant.initConstant(SpeechMainActivity.this);
        ScreenSize.initial(this);
        //耗时操作在新的线程中运行
        new Thread()
        {
            @Override public void run()
            {
                super.run();
                ContactList.getContactsList(SpeechMainActivity.this);
                RunningProcess.getRunningProcessNames(SpeechMainActivity.this);
                /**
                 * 初始化变量
                 */
            }
        }.start();
        dialog = new ProgressDialog(this);
        controlIvWidth = Constant.screenWidth / 3;
        controlIvHeight = controlIvWidth;
        controlIv.setBackgroundResource(R.drawable.speak_ongoing);
        frameAnimation = (AnimationDrawable) controlIv.getBackground();
        rotateAnimation = new RotateAnimation(0.0f, 360.0f, Animation
                .RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(4000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setFillAfter(false);
        params = new LayoutParams(controlIvWidth, controlIvHeight);
        controlIv.setLayoutParams(params);
        controlBt.setLayoutParams(params);
        /**
         * 不得注册onClick,否则selector不起作用
         */
        controlIv.setOnTouchListener(this);
        controlBt.setOnTouchListener(this);
        setting.setOnTouchListener(this);
        help.setOnTouchListener(this);
        recognizerWrapper = SpeechRecognizerWrapper.getSingleInstance(this,
                recognizerHandler);
        synthesizerWrapper = SpeechSynthesizerWrapper.getSingleInstance(this,
                synthezierHandler);
    }
    // 用来语音合成时与SpeechSynthezierWraper进行异步通信
    private SpeechHandler synthezierHandler = new SpeechHandler(this,
            SpeechHandler.SPEECH_SYNTHESIER_FLAG);
    // 用来语音听写时与SpeechRecognizerWrapper通信
    private SpeechHandler recognizerHandler = new SpeechHandler(this,
            SpeechHandler.SPEECH_RECOGNIZER_FLAG);
    protected void onResume()
    {
        super.onResume();
//        stopService(new Intent(this, WakeUpService.class));
        Intent recIntent = getIntent();
        if (recIntent != null && recIntent.getBooleanExtra("restart", false))
        {
            ActivityManager manager =
                    (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            manager.killBackgroundProcesses(getPackageName());
        }
        oilBroadcast = new QueryOilBroadcast();
        IntentFilter oilPriceIntentFilter = new IntentFilter(
                OilService.BROADCAST_OIL_PRICE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(oilBroadcast,
                oilPriceIntentFilter);
        weatherBroadcast = new QueryWeatherBroadcast();
        IntentFilter weatherIntentFilter = new IntentFilter(
                WeatherService.BROADCAST_CURRENT_CITY_WEATHER_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver
                (weatherBroadcast, weatherIntentFilter);
        playMusicBroadcast = new PlayMusicBroadcast();
        IntentFilter playMusicIntentFilter = new IntentFilter();
        playMusicIntentFilter.addAction(SearchMusicService
                .BROADCAST_SEARCH_AUDIO);
        LocalBroadcastManager.getInstance(this).registerReceiver
                (playMusicBroadcast, playMusicIntentFilter);
    }
    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(oilBroadcast);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(weatherBroadcast);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(playMusicBroadcast);
    }
    @Override protected void onStop()
    {
        super.onStop();
        Intent intent = new Intent(this, WakeUpService.class);
        intent.putExtra(WakeUpService.START_WAKE_UP_EXTRA, "wakeUp");
//        startService(intent);
    }
    /**
     * 生成对话View，然后将View添加到容器中 同时更新滚动条
     */
    public View addConversationCell(String dictationResult, int type)
    {
        // TODO Auto-generated method stub
        ConversationCell cell = null;
        if (type == ConversationCell.LEFT_CONVERSATION)
        {
            cell = new ConversationCell(SpeechMainActivity.this,
                    ConversationCell.LEFT_CONVERSATION);
            lastLeftCell = cell;
        }
        else if (type == ConversationCell.RIGHT_CONVERSATION)
        {
            if (lastRightCell != null)
            {
                lastRightCell.editRightIv.setVisibility(View.INVISIBLE);
                lastRightCell.editRightIv.setEnabled(false);
            }
            if (lastRightCell != null)
                lastRightCell.unregisterCallback();
            cell = new ConversationCell(SpeechMainActivity.this,
                    ConversationCell.RIGHT_CONVERSATION);
            lastRightCell = cell;
            lastRightCell.setCallback(this);
        }
        View rootView = cell.rootView;
        ImageView tvBg = cell.conversationBg;
        rootView.setLayoutParams(new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        int conversationRealHeight = 0;
        if (type == ConversationCell.RIGHT_CONVERSATION)
            conversationRealHeight = getRealHeight(cell.conversationContent,
                    cell.getSingleLineWidth(dictationResult, type), 0,
                    cell.editRightIv.getMeasuredWidth()
            );
        else
            conversationRealHeight = getRealHeight(cell.conversationContent,
                    cell.getSingleLineWidth(dictationResult, type), 0);
        // contentContainer.addView(rootView);
        addViewToWindow(rootView);
        tvBg.setMinimumHeight((int) (conversationRealHeight * 1.05));
        tvBg.setMaxHeight((int) (conversationRealHeight * 1.2));
        // 200毫秒之后更新滚动条
        recognizerHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                int scrollDistance = contentContainer.getMeasuredHeight()
                        - contentContainer.getTop();
                scrollView.scrollTo(0, scrollDistance);
                // scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 200);
        return rootView;
    }
    /**
     * 将View组件添加到视图窗口
     */
    public void addViewToWindow(final View rootView)
    {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                contentContainer.addView(rootView);
            }
        });
        // 200毫秒之后更新滚动条
        recognizerHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 200);
    }
    /**
     * 获取组件的实际高度,实际宽度向上取整
     *
     * @param singleLineWidth 组件屏幕显示的最大宽度，像素表示
     * @param extraSpace      如果是右侧语音听写部分，编辑图片也需要占据一定宽度空间
     */
    private int getRealHeight(TextView conversationContent, int singleLineWidth,
                              int extraHeight, int... extraSpace)
    {
        // TODO Auto-generated method stub
        int realHeight = 0;
        // 默认放大倍数
        float scaleValue = 1.0f;
        // 行间距
        conversationContent
                .measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measureHeight =
                conversationContent.getMeasuredHeight() + (extraSpace.length >
                        0 ? extraSpace[0] : 0);
        int measureWidth = conversationContent.getMeasuredWidth();
        scaleValue = (float) Math.ceil(measureWidth * 1.0 / singleLineWidth);
        realHeight = (int) (scaleValue * measureHeight);
        return (realHeight + extraHeight);
    }
    public int getSessionStatus()
    {
        return sessionStatus;
    }
    public void setSessionStatus(int sessionStatus)
    {
        SpeechMainActivity.sessionStatus = sessionStatus;
    }
    private boolean checkNetStatus()
    {
        if (!NetInfo.isNetAvailable(this))
        {
            Toast toast = Toast.makeText(this, "当前没有可用网络", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }
        return true;
    }
    /**
     * 语音听些时候开始动画
     */
    void startDictationAnimation()
    {
        /**
         * 能够看到点击效果
         */
        controlBt.setBackgroundResource(R.drawable.control_selector);
        recognizerHandler.postDelayed(new Runnable()
        {
            @Override public void run()
            {
                controlBt.setVisibility(View.INVISIBLE);
            }
        }, 200);
        controlIv.setVisibility(View.VISIBLE);
        frameAnimation.start();
    }
    /**
     * 结束语音听写
     */
    void stopDictationAnimation()
    {
        frameAnimation.stop();
        frameAnimation.selectDrawable(0);
        rotateAnimation.cancel();
        controlBt.setVisibility(View.VISIBLE);
        controlIv.setVisibility(View.INVISIBLE);
//        controlBt.setBackgroundResource(R.drawable.control_selector);
    }
    /**
     * 语音处理正在进行中，等待语音处理返回结果
     */
    void analyseAnimation()
    {
        frameAnimation.stop();
        frameAnimation.selectDrawable(0);
        controlBt.setVisibility(View.VISIBLE);
        controlIv.setVisibility(View.INVISIBLE);
        controlBt.setBackgroundResource(
                R.drawable.control_selector);
        controlBt.startAnimation(rotateAnimation);
    }
    /**
     * 处理语音动画
     */
    private void disposeAnimation()
    {
        int dictationStatus =
                recognizerWrapper.getDictationStatus();
        int synthesizerStatus =
                synthesizerWrapper.getSynthesizerStatus();
        // 当前正在进行语音合成,停止语音合成，同时返回
        if (synthesizerStatus !=
                SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER)
        {
            synthesizerWrapper.stopSpeak();
            synthesizerWrapper
                    .setSynthesizerStatus(
                            SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER);
            return;
        }
        //当前正在进行语音听写
        switch (dictationStatus)
        {
            // 开始语音听写
            case SpeechRecognizerWrapper.DICTATION_OVER:
            case SpeechRecognizerWrapper.DICTATION_ERROR:
                // 只能在开启会话时候调用
                if (getSessionStatus() ==
                        SpeechMainActivity.SESSION_OVER)
                {
                    setSessionStatus(
                            SpeechMainActivity.SESSION_ONGOING);
                }
                recognizerWrapper.setSpeechRecognizerHandler(recognizerHandler);
                synthesizerWrapper.setSynthesizerHandler(synthezierHandler);
                recognizerWrapper.startDictation();
                break;
            case SpeechRecognizerWrapper.DICTATION_RECORD_BEGIN:
            case SpeechRecognizerWrapper.DICTATION_RECORD_OVER:
                recognizerWrapper.stopDictation();
                break;
            default:
                break;
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        // 由于采用的是帧布局方式，button位于image的上面
        if (
                event.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            switch (v.getTag().toString())
            {
                case HELP_TAG:
                    startActivity(new Intent(this, HelpActivity.class));
                    break;
                case ANIMATION_TAG:
                    if (checkNetStatus())
                    {
                        disposeAnimation();
                    }
                    break;
                case SETTING_TAG:
                    startActivity(new Intent(this, SettingActivity.class));
                    break;
            }
        }
        return false;
    }
    /**
     * 应用程序推出时候，相应的进程也退出
     */
    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        resetStatus();
//        int pid = Process.myPid();
//        setSessionStatus(SESSION_OVER);
//        Process.killProcess(pid);
    }
    @Override public boolean handleMessage(Message msg)
    {
        handleResMessage(msg, true);
        return true;
    }
    /**
     * 将语音处理程序独立出来
     *
     * @param local 是否在SpeechMainActivity中调用的 是否建立action的标志
     */
    public void handleResMessage(Message msg, boolean local)
    {
        if (msg.arg1 == SpeechHandler.SPEECH_RECOGNIZER_FLAG)
        {
            Log.e(TAG, "语音听写" + msg.what);
            switch (msg.what)
            {
                case SpeechRecognizerWrapper.DICTATION_RECORD_BEGIN:
                    startDictationAnimation();
                    break;
                case SpeechRecognizerWrapper.DICTATION_ERROR:
                    if (msg.arg2 == SpeechHandler.ALLOW_DRAW)
                    {
                        synthesizerWrapper.startSpeak(msg.obj.toString());
                    }
                    stopDictationAnimation();
                    setSessionStatus(SESSION_OVER);
                    break;
                case SpeechRecognizerWrapper.DICTATION_OVER:
                    if (msg.arg2 == SpeechHandler.ALLOW_DRAW)
                    {
                        addConversationCell(msg.obj.toString(), ConversationCell
                                .RIGHT_CONVERSATION);
                    }
                    stopDictationAnimation();
                    if (local)
                    {
                        supAction =
                                (SupAction) actionFactory
                                        .buildAction(PinyinUtils
                                                .filterPunctuation(
                                                        msg.obj.toString()));
                        if (supAction == null)
                        {
                            synthesizerWrapper.startSpeak(MISUNDERSTANDING);
                            setSessionStatus(SESSION_OVER);
                        }
                        else
                        {
                            supAction.handleDictation(this, PinyinUtils
                                    .filterPunctuation(msg
                                            .obj
                                            .toString()));
                        }
                    }
                    break;
                case SpeechRecognizerWrapper.DICTATION_RECORD_OVER:
                case SpeechRecognizerWrapper.DICTATION_RECOGNIZING:
//                    analyseAnimation();
                    break;
            }
        }
        else if (msg.arg1 == SpeechHandler.SPEECH_SYNTHESIER_FLAG)
        {
//            stopDictationAnimation();
            Log.e(TAG, "语音合成" + msg.what);
            switch (msg.what)
            {
                case SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_BEGIN:
                    if (msg.arg2 == SpeechHandler.ALLOW_DRAW)
                    {
                        addConversationCell(msg.obj.toString(),
                                ConversationCell.LEFT_CONVERSATION);
                    }
                    break;
                case SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_ONGOING:
                    break;
                case SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_PAUSE:
                    break;
                case SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER:
                    break;
            }
        }
    }
    @Override public void onEditItemClicked(View v, View contentView)
    {
        if (v.getId() == R.id.edit_conversation_right)
        {
            contentContainer.removeView(lastLeftCell.rootView);
            if (getSessionStatus() != SESSION_OVER)
            {
                synthesizerWrapper.stopSpeak();
                recognizerWrapper.stopDictation();
            }
            return;
        }
        switch (v.getId())
        {
            case R.id.rightEnsureOption:
                setSessionStatus(SESSION_ONGOING);
                contentContainer.removeView(lastLeftCell.rootView);
                lastLeftCell = null;
                supAction =
                        (SupAction) actionFactory
                                .buildAction(PinyinUtils
                                        .filterPunctuation(
                                                ((TextView) contentView)
                                                        .getText()
                                                        .toString
                                                                ()));
                if (supAction == null)
                {
                    synthesizerWrapper.startSpeak(MISUNDERSTANDING);
                    setSessionStatus(SESSION_OVER);
                }
                else
                {
                    supAction.handleDictation(this, PinyinUtils
                            .filterPunctuation(((TextView) contentView).getText()
                                    .toString()));
                }
                break;
        }
    }
    /**
     * 接收油价的广播
     *
     * @author Administrator
     */
    private class QueryOilBroadcast extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            setSessionStatus(SESSION_OVER);
            // TODO Auto-generated method stub
            if (intent.getStringExtra("status").equals(
                    "success"))
            {
//				SpeechSynthesizerWrapper.startSpeak("油价请求成功");
//				Log.e("oilPrices", intent.getParcelableArrayListExtra
//						(OilService.EXTRA_OIL_PRICES).toString());
                ArrayList<OilPrice> oilPrices = intent
                        .getParcelableArrayListExtra(OilService
                                .EXTRA_OIL_PRICES);
                StringBuilder oilInfo = new StringBuilder("\t\t\t" + intent
                        .getStringExtra(OilService.EXTRA_PROVINCE) + "\n");
                for (OilPrice oilPrice : oilPrices)
                {
                    oilInfo.append(oilPrice.getOilType() + "汽油价格：" + oilPrice
                            .getOilPrice() + "元\n");
                }
                synthesizerWrapper.startSpeak(oilInfo.toString());
            }
            else
            {
                synthesizerWrapper.startSpeak("油价请求失败");
            }
        }
    }

    /**
     * 查询天气的接收广播
     */
    private class QueryWeatherBroadcast extends BroadcastReceiver
    {

        @Override public void onReceive(Context context, Intent intent)
        {
            if (intent.getStringExtra("status").equals("success"))
            {
                ArrayList<Weather> weatherList = intent
                        .getParcelableArrayListExtra(WeatherService
                                .EXTRA_WEATHERS);
                /**
                 * 只播放当天和明天的天气预报
                 */
                StringBuilder weatherBuilder = new StringBuilder();
                Weather today = weatherList.get(0);
                weatherBuilder.append(today.getCity() + "天气:");
                weatherBuilder
                        .append("今天天气" + today.getWeather() + "，" + today
                                .getTemperature() + "°" + today.getWind() +
                                "," +
                                "当前温度" +
                                today
                                        .getCurrent_temp() + "°");
                if(weatherList.size()>1) {
                    Weather tomorrow = weatherList.get(1);
                    weatherBuilder.append(";明天天气" + tomorrow.getWeather() + "," +
                            "" + tomorrow.getTemperature() + "°" + tomorrow.getWind
                            ());
                }
                synthesizerWrapper.startSpeak(weatherBuilder.toString().trim());
            }
            else
            {
                synthesizerWrapper.startSpeak("天气请求失败");
            }
        }
    }

    private class PlayMusicBroadcast extends BroadcastReceiver
    {

        @Override public void onReceive(Context context, Intent intent)
        {
            setSessionStatus(SESSION_OVER);
//            dismissDialog();
            dismissProgressDialog();
            if (intent.getStringExtra("status").toString().equals("success"))
            {
                ArrayList<String> musics = intent.getStringArrayListExtra
                        (SearchMusicService.EXTRA_AUDIOS);
                String randomMusic = musics.get(new Random().nextInt(musics
                        .size()));
                synthesizerWrapper.startSpeak("正在打开音乐播放器");
//                intent.setAction(Intent.ACTION_ALL_APPS);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setDataAndType(Uri.parse("file://" + randomMusic),
                        "audio/*");
                startActivity(intent);
            }
            else
            {
                synthesizerWrapper.startSpeak("没有可播放的音频文件");
            }
        }
    }
    /**
     * 重置，在结束Activity时候调用
     */
    private void resetStatus()
    {
        recognizerWrapper.setDictationStatus(
                SpeechRecognizerWrapper.DICTATION_OVER);
        synthesizerWrapper
                .setSynthesizerStatus(
                        SpeechSynthesizerWrapper.SPEECH_SYNTHESIZER_OVER);
        setSessionStatus(SESSION_OVER);
    }
}
