package com.example.slidingscroller;
import android.content.Context;
import android.view.View;

import java.util.List;
public class MyViewPagerAdapter
{

    List<View> contentList;
    List<String> titleList;
    Context context;
    public MyViewPagerAdapter(Context context, List<View> contentList,
                              List<String> titleList)
    {
        this(context, contentList);
        this.titleList = titleList;
    }
    public MyViewPagerAdapter(Context context, List<View> contentList)
    {
        this.context = context;
        this.contentList = contentList;
    }
}
