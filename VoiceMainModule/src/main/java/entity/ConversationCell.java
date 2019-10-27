package entity;
import android.content.Context;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.voiceanimationinteraction.R;
import com.klicen.citychoosermodule.ScreenSize;

import utils.Constant;
/**
 * 实例化对话框
 *
 * @author Administrator
 */
public class ConversationCell implements View.OnClickListener
{

    private Context context;
    private static String TAG = ConversationCell.class.getSimpleName();
    public final static int LEFT_CONVERSATION = 1;
    public final static int RIGHT_CONVERSATION = 2;
    public int extraTopAndBottom;
    public View rootView;
    /**
     * 背景颜色图片
     */
    public ImageView conversationBg;
    private int editRightIvWidth;
    /**
     * 谈话内容
     */
    public TextView conversationContent;
    public ImageView editRightIv;
    private Button rightEnsureOption;
    private Button rightCancelOption;
    private LinearLayout rightOptionContainer;
    private EditText conversationContentEt;
    private Callback editCallBack;
    public void setCallback(Callback callback)
    {
        this.editCallBack = callback;
    }
    public void unregisterCallback()
    {
        editCallBack = null;
        System.gc();
    }
    /**
     * 根据参数type选择实例化xml文件
     *
     * @param type 1实例化左边对话框，2实例化右边对话框
     */
    public ConversationCell(Context context, int type)
    {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (type)
        {
            case LEFT_CONVERSATION:
                rootView =
                        layoutInflater.inflate(R.layout.main_conversation_left,
                                null);
                conversationBg = (ImageView) rootView
                        .findViewById(R.id.leftConversationBg);
                conversationContent = (TextView) rootView
                        .findViewById(R.id.converstaionContentLeft);
                break;
            case RIGHT_CONVERSATION:
                rootView = layoutInflater.inflate(
                        R.layout.main_converstation_right, null);
                conversationBg = (ImageView) rootView
                        .findViewById(R.id.rightConversationBg);
                conversationContent = (TextView) rootView
                        .findViewById(R.id.converstaionContentRight);
                editRightIv = (ImageView) rootView.findViewById(R.id
                        .edit_conversation_right);
                rightOptionContainer = (LinearLayout) rootView.findViewById(R.id
                        .rightOptionContainer);
                rightEnsureOption = (Button) rootView.findViewById(R.id
                        .rightEnsureOption);
                rightCancelOption = (Button) rootView.findViewById(R.id
                        .rightCancelOption);
                LinearLayout.LayoutParams rightEnsureParams =
                        (LinearLayout.LayoutParams) rightEnsureOption
                                .getLayoutParams();
                LinearLayout.LayoutParams rightCancelParams =
                        (LinearLayout.LayoutParams) rightCancelOption
                                .getLayoutParams();
                rightEnsureParams.height =
                        (int) (Constant.screenWidth * 1.0 / 10);
                rightCancelParams.height = rightEnsureParams.height;
                rightEnsureOption.setLayoutParams(rightEnsureParams);
                rightCancelOption.setLayoutParams(rightCancelParams);
                conversationContentEt = (EditText) rootView.findViewById(R.id
                        .conversationContentEt);
                conversationContentEt.setOnFocusChangeListener(
                        new View.OnFocusChangeListener()
                        {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus)
                            {
                                if (!hasFocus)
                                {
                                    InputMethodManager
                                            methodManager =
                                            (InputMethodManager) conversationBg
                                                    .getContext().getSystemService
                                                            (Context
                                                                    .INPUT_METHOD_SERVICE);
                                    methodManager.hideSoftInputFromWindow
                                            (conversationBg
                                                    .getWindowToken(), 0);
                                }
                            }
                        });
                conversationContentEt.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count,
                                                  int after)
                    {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count)
                    {
                    }
                    @Override public void afterTextChanged(Editable s)
                    {
                        updateConversationBackground(RIGHT_CONVERSATION);
                    }
                });
                RelativeLayout.LayoutParams rightParams =
                        (RelativeLayout.LayoutParams) editRightIv
                                .getLayoutParams();
                TextPaint textPaint = conversationContent.getPaint();
                rightParams.height = (int) (textPaint.descent() -
                        textPaint.ascent());
                editRightIvWidth = ScreenSize.getScreenWidth() / 12;
                editRightIv.setLayoutParams(rightParams);
                editRightIv.setOnClickListener(this);
                rightEnsureOption.setOnClickListener(this);
                rightCancelOption.setOnClickListener(this);
                break;
            default:
                break;
        }
        // System.out.println("leftSingleLineWidth:" + leftSingleLineWidth + "
        // extra:"
        // + extraTopAndBottom);
    }
    private void updateConversationBackground(int type)
    {
        int realHeight = getRealHeight(conversationContentEt, getSingleLineWidth
                        (conversationContentEt.getText().toString(), type),
                extraTopAndBottom);
        conversationBg.setMinimumHeight((int) (realHeight * 1.05));
        conversationBg.setMaxHeight((int) (realHeight * 1.1));
    }
    /**
     * TextView的最大宽度
     *
     * @param dictation 文本内容
     * @param type      左边语音合成文本框|右边语音听写文本框
     */
    public synchronized int getSingleLineWidth(String dictation, int type)
    {
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) conversationContent
                        .getLayoutParams();
        int singleLineWidth =
                Constant.screenWidth - params.leftMargin
                        - params.rightMargin -
                        conversationContent.getPaddingLeft()
                        - conversationContent.getPaddingRight();
        conversationContent.setText(dictation);
        if (type == RIGHT_CONVERSATION)
        {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
            singleLineWidth = singleLineWidth - (int) TypedValue.applyDimension
                    (TypedValue
                            .COMPLEX_UNIT_DIP, 100, metrics);
        }
        extraTopAndBottom = params.topMargin + params.bottomMargin
                + conversationContent.getPaddingTop()
                + conversationContent.getPaddingBottom();
        return singleLineWidth;
    }
    public void startEditContent()
    {
        rightOptionContainer.setVisibility(View.VISIBLE);
        editRightIv.setVisibility(View.INVISIBLE);
        String dictationResult = conversationContent.getText().toString();
        conversationContentEt.setText(dictationResult);
        if (dictationResult.endsWith("。") || dictationResult.endsWith("?")
                || dictationResult.endsWith("？") || dictationResult.endsWith("。"))
        {
            conversationContentEt
                    .setSelection(dictationResult.length() - 1);
        }
        else
        {
            conversationContentEt.setSelection(dictationResult.length());
        }
        conversationContent.setVisibility(View.INVISIBLE);
        conversationContentEt.setVisibility(View.VISIBLE);
        /**
         * 弹出软键盘
         */
        InputMethodManager inputManager = (InputMethodManager) conversationBg
                .getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(conversationContentEt, 0);
    }
    public void finishEditContent(boolean isSet)
    {
        rightOptionContainer.setVisibility(View.GONE);
        editRightIv.setVisibility(View.VISIBLE);
        if (isSet)
            conversationContent.setText(conversationContentEt.getText());
        conversationContent.setVisibility(View.VISIBLE);
        conversationContentEt.setVisibility(View.INVISIBLE);
    }
    @Override public void onClick(View v)
    {
        Log.i(TAG,"width:"+conversationContentEt.getWidth()+",height:"+conversationContentEt.getHeight());
        switch (v.getId())
        {
            case R.id.edit_conversation_right:
                startEditContent();
               /* conversationContentEt
                        .getLayoutParams().width = conversationContentParams.width;
                conversationContentEt
                        .getLayoutParams().height = conversationContentParams.height;*/
                break;
            case R.id.rightEnsureOption:
                finishEditContent(true);
                if (editCallBack != null)
                {
                    editCallBack.onEditItemClicked(v, conversationContent);
                }
                break;
            case R.id.rightCancelOption:
                finishEditContent(false);
                if (editCallBack != null)
                {
                    editCallBack.onEditItemClicked(v, conversationContent);
                }
                break;
        }
    }
    /**
     * 获取组件的实际高度,实际宽度向上取整
     *
     * @param singleLineWidth 组件屏幕显示的最大宽度，像素表示
     */
    public int getRealHeight(TextView conversationContent, int singleLineWidth,
                             int extraHeight)
    {
        // TODO Auto-generated method stub
        int realHeight = 0;
        // 默认放大倍数
        float scaleValue = 1.0f;
        // 行间距
        conversationContent.measure(0, 0);
        int measureHeight = conversationContent.getMeasuredHeight();
        int measureWidth = conversationContent.getMeasuredWidth();
        if (measureWidth / singleLineWidth < 1)
        // realHeight = measureHeight;
        {
            scaleValue = 1.0f;
        }
        // 处理边界情况
        else if (1.0f * measureWidth / singleLineWidth - measureWidth
                / singleLineWidth < 0.04)
        {
            scaleValue = measureWidth / singleLineWidth;
        }
        else
        {
            scaleValue = measureWidth / singleLineWidth + 1;
        }
        realHeight = (int) (scaleValue * measureHeight);
        return (realHeight + extraHeight);
    }
    public interface Callback
    {

        void onEditItemClicked(View v, View contentView);
    }
}
