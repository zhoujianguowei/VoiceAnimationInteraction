package com.example.slidingscroller;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
public class MyLinearLayout extends LinearLayout
{

    private ArrayList<View> decendants;// 指定父组件的所有子孙节点
    private int xDown;
    private int yDown;
    private int xMove;
    private int yMove;
    public MyLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public MyLinearLayout(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }
    // 对于一次类型事件，仅仅调用一次ACTION_DOWN一次，ACTION_MOVE一次
    // 一旦返回true，本次点击不会再次调用。
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        boolean isIntercepted = false;
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            xDown = (int) ev.getRawX();
            yDown = (int) ev.getRawY();
        }
        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            isIntercepted = true;
            View child = null;
            getDecendants(this);
            for (int i = 0; i < decendants.size(); i++)
            {
                child = decendants.get(i);
                if ((child instanceof AbsListView
                        || child instanceof ScrollView ||
                        child instanceof HorizontalScrollView)
                        && checkArea(child, ev))
                {
                    xMove = (int) ev.getRawX();
                    yMove = (int) ev.getRawY();
                    int xDistance = xMove - xDown;
                    int yDistance = yMove - yDown;
                    // 水平滑动距离大于垂直滑动距离，不加拦截
                    if (Math.abs(xDistance) < Math.abs(yDistance))
                    {
                        isIntercepted = false;
                        break;
                    }
                }
            }
        }
        return isIntercepted;
    }
    // 得到指定父容器的所有子孙组件
    private void getDecendants(ViewGroup container)
    {
        if (decendants == null)
            decendants = new ArrayList<View>();
        decendants.clear();
        for (int i = 0; i < container.getChildCount(); i++)
        {
            View child = container.getChildAt(i);
            decendants.add(child);
            if (child instanceof ViewGroup)
            {
                ViewGroup group = (ViewGroup) child;
                getDecendants(group);
            }
        }
        // TODO Auto-generated method stub
    }
    /**
     * 触摸点是否发生在指定View区域内
     */
    private boolean checkArea(View view, MotionEvent ev)
    {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int xPoint = (int) ev.getRawX();
        int yPoint = (int) ev.getRawY();
        int xStop = location[0] + view.getMeasuredWidth();
        int yStop = location[1] + view.getMeasuredHeight();
        if (location[0] < xPoint && xPoint < xStop && location[1] < yPoint
                && yPoint < yStop)
            return true;
        return false;
    }
}
