package com.example.slidingscroller;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
public class SlidingMain extends Activity implements OnPageChangedListener
{

    MyViewPager viewPager;
    FadeViewPagerIndicator viewPagerIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.overriten_sidebar);
        viewPager = (MyViewPager) findViewById(R.id.viewpager);
        List<View> viewList = new ArrayList<View>();
        for (int i = 0; i < 3; i++)
        {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            tv.setText("这是页面" + (i + 1));
            if (i == 0)
                tv.setBackgroundColor(Color.RED);
            else if (i == 1)
                tv.setBackgroundColor(Color.GREEN);
            else
                tv.setBackgroundColor(Color.BLUE);
            viewList.add(tv);
        }
        viewPager.setAdapter(new MyViewPagerAdapter(this, viewList));
        viewPager.setOnItemChangedListeer(this);
    }
    @Override
    public void onPageStateChanged(int currentItemIndex, int scrollDistance,
                                   boolean isFinished)
    {
        if (viewPagerIndicator == null)
            return;
        if (scrollDistance > 0)
            viewPagerIndicator.itemPagerIndicatorScroll(null, currentItemIndex,
                    ViewPagerIndicatorImpl.LEFT_TO_RIGNT, scrollDistance,
                    isFinished);
        else
            viewPagerIndicator.itemPagerIndicatorScroll(null, currentItemIndex,
                    ViewPagerIndicatorImpl.RIGHT_TO_LEFT, scrollDistance,
                    isFinished);
    }
    @Override
    public void onPageChanged(int previousPage, int currentPage)
    {
        // TODO Auto-generated method stub
    }
}
