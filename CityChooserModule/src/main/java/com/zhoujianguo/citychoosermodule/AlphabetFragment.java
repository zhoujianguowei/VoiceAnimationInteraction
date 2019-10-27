package com.klicen.citychoosermodule;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.TreeSet;
/**
 * Created by Administrator on 2015/4/19.
 */
public class AlphabetFragment extends Fragment implements View.OnClickListener
{

    private AlphabetItemOnClickListener listener;
    LinearLayout alphabetLlContainer;
    public void setAlphabteItemOnClickListener(
            AlphabetItemOnClickListener listener)
    {
        this.listener = listener;
    }
    public LinearLayout getAlphabetLlContainer()
    {
        return alphabetLlContainer;
    }
    private volatile boolean isInit;
    private TreeSet<String> groups = new TreeSet<>();
    /**
     * 是否初始化
     */
    public boolean isInit()
    {
        return isInit;
    }
    public void setInit(boolean isInit)
    {
        this.isInit = isInit;
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        View alphaView = inflater.inflate(R.layout.fragment_alphabet, null);
        alphabetLlContainer = (LinearLayout) alphaView
                .findViewById(R.id.alphabet_ll_container);

       /* alphaView.setLayoutParams(new LinearLayout.LayoutParams((int) (
                        ScreenSize.getScreenWidth() * 1.0 /
                                18), ViewGroup.LayoutParams.MATCH_PARENT));*/
        return alphaView;
    }
    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (!isInit)
        {
            isInit = true;
            updateView(groups);
        }
    }
    public interface AlphabetItemOnClickListener
    {

        /**
         * @param view
         * @param type  province or city type
         */
        public static final int CITY_FLAG = 1;
        public static final int PROVINCE_FLAG = 2;
        public void onAlphabetItemClick(View view, int type);
    }
    @Override
    public void onClick(View v)
    {
        if (listener != null)
        {
            if (getActivity() instanceof CityChooseActivity)
            {
                CityChooseActivity activity =
                        (CityChooseActivity) getActivity();
                if (activity.isProvinceFragmentShow())
                {
                    listener.onAlphabetItemClick(v,
                            AlphabetItemOnClickListener.PROVINCE_FLAG);
                }
                else
                {
                    listener.onAlphabetItemClick(v,
                            AlphabetItemOnClickListener.CITY_FLAG);
                }
            }
            listener.onAlphabetItemClick(v, -1);
        }
        else if (getActivity() instanceof AlphabetItemOnClickListener)
        {
            ((AlphabetItemOnClickListener) getActivity()).onAlphabetItemClick
                    (v, -1);
        }
    }
    //更新字母表视图
    public void updateView(final TreeSet<String> alphabet)
    {
        /**
         * onActivityCreated()没有回调完成
         *
         */
        if (!isInit || alphabet.isEmpty())
        {
            groups = alphabet;
            return;
        }
        alphabetLlContainer.removeAllViews();
        int i = 0;
        String[] alphabetArray = new String[alphabet.size()];
        alphabet.toArray(alphabetArray);
        for (; i < alphabetArray.length; i++)
        {
            TextView letterTv = new TextView(getActivity());
            letterTv.setText(alphabetArray[i] + "");
            if (alphabetArray.length < 20)
            {
                letterTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources()
                                .getDimension(R.dimen.little));
            }
            else
            {
                letterTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources()
                                .getDimension(R.dimen.least));
            }
            if (i == 0)
            {
                letterTv.setTextColor(Color.BLACK);
                TextPaint paint = letterTv.getPaint();
                paint.setFakeBoldText(true);
                letterTv.setGravity(Gravity.CENTER);
                alphabetLlContainer.addView(letterTv);
                letterTv.setPadding(5, 5, 5, 5);
                letterTv.setOnClickListener(AlphabetFragment.this);
                continue;
            }
            letterTv.setTextColor(
                    getResources()
                            .getColor(R.color.siding_alpha_color_blue));
            letterTv.setGravity(Gravity.CENTER);
            alphabetLlContainer.addView(letterTv);
            letterTv.setOnClickListener(AlphabetFragment.this);
        }
        TextView endTv = new TextView(getActivity());
        endTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.little));
        endTv.setTextColor(
                getResources()
                        .getColor(R.color.siding_alpha_color_blue));
        endTv.setGravity(Gravity.CENTER);
        endTv.setText("#");
        alphabetLlContainer.addView(endTv);
        endTv.setOnClickListener(AlphabetFragment.this);
    }
    /**
     * 根据字母信息更新字母表视图
     */
    public void setSelectedItem(String selectedLetter)
    {
        int childCount = alphabetLlContainer.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            TextView child = (TextView) alphabetLlContainer.getChildAt(i);
            TextPaint paint = child.getPaint();
            if (child.getText().toString().equals(selectedLetter))
            {
                child.setTextColor(Color.BLACK);
                paint.setFakeBoldText(true);
            }
            else
            {
                child.setTextColor(getResources()
                        .getColor(R.color.siding_alpha_color_blue));
                paint.setFakeBoldText(false);
            }
        }
    }
    /**
     * 根据位置信息更新字母表
     */
    public void setSelectedItem(int firstVisibleItem)
    {
        int childCount = alphabetLlContainer.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            TextView child = (TextView) alphabetLlContainer.getChildAt(i);
            TextPaint textPaint = child.getPaint();
            if (i == firstVisibleItem)
            {
                child.setTextColor(Color.BLACK);
                textPaint.setFakeBoldText(true);
            }
            else
            {
                child.setTextColor(getResources().getColor(R.color
                        .siding_alpha_color_blue));
                textPaint.setFakeBoldText(false);
            }
        }
    }
}
