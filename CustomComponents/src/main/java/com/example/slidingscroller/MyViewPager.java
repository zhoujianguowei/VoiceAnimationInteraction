package com.example.slidingscroller;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
public class MyViewPager extends LinearLayout implements OnTouchListener
{

    MyViewPagerAdapter adapter;
    OnPageChangedListener itemChangedListener;// 内容栏改变时，调用该接口
    List<LinearLayout> linearLayoutList;
    LinearLayout currentLayout;
    LinearLayout firstLayout;
    MarginLayoutParams firstLayoutParams = null;
    private int measuredWidth; // 每个LinearLayout的宽度
    private int xDown;
    private int yDown;
    private int xMove;
    private int yMove;
    private int xUp;
    private int yUp;
    private volatile boolean isScrolling;
    private volatile boolean isPageChanged;
    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;
    private int scrollVelocityBound;
    private int scrollDistanceBound;
    private VelocityTracker velocityTracker;
    private int scrollDistanceUnit;
    Context context;
    private int currentPageIndex;
    private Handler mHandler;
    private boolean isConfigureMarginWidth;
    int leftMargin = 0;
    public MyViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
        ScreenUtility.initParams(context);
        // TODO Auto-generated constructor stub
    }
    public MyViewPager(Context context)
    {
        super(context);
        this.context = context;
        ScreenUtility.initParams(context);
        // TODO Auto-generated constructor stub
    }
    public void setAdapter(MyViewPagerAdapter adapter)
    {
        this.adapter = adapter;
        init(this.adapter.contentList);
    }
    private void init(List<View> contentList)
    {
        linearLayoutList = new ArrayList<LinearLayout>(contentList.size());
        for (int i = 0; i < contentList.size(); i++)
        {
            LinearLayout linearLayout = new MyLinearLayout(context);
            linearLayout.addView(contentList.get(i));
            addView(linearLayout);
            linearLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            linearLayout.setOnTouchListener(this);
            linearLayoutList.add(linearLayout);
        }
        currentLayout = linearLayoutList.get(0);
        scrollDistanceBound = ScreenUtility.getScreenWidth() / 5;
        scrollVelocityBound = ScreenUtility.getScreenWidth() / 3;
        scrollDistanceUnit = ScreenUtility.getScreenWidth() / 10;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        createVelocity(event);
        int xDistance = 0;
        int yDistance = 0;
        int mActivePointer =
                event.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        velocityTracker.computeCurrentVelocity(1000);
        System.out.println("action:" + event.getAction() + "  pointerId:"
                + mActivePointer);
        System.out.println("action:" + event.getActionMasked());
        System.out.println("xCor:" + event.getRawX() + "  yCor:" + event.getRawY());
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xDown = (int) event.getRawX();
                yDown = (int) event.getRawY();
                currentLayout = linearLayoutList.get(currentPageIndex);
                if (!isConfigureMarginWidth)
                    configureMarginWidth(currentLayout.getMeasuredWidth());
                firstLayout = (LinearLayout) linearLayoutList.get(0);
                firstLayoutParams = (MarginLayoutParams) firstLayout
                        .getLayoutParams();
                // 初始化最左边页面的边界线
                firstLayoutParams.leftMargin = -currentPageIndex * measuredWidth;
                firstLayout.setLayoutParams(firstLayoutParams);
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = (int) event.getRawX();
                yMove = (int) event.getRawY();
                xDistance = xMove - xDown;
                yDistance = yMove - yDown;
                if (Math.abs(xDistance) < Math.abs(yDistance))
                    break;
                if (xDistance > 0 && currentPageIndex == 0 || xDistance < 0
                        && currentPageIndex == linearLayoutList.size() - 1)
                    break;
                if (Math.abs(xDistance) > measuredWidth)
                    xDistance = xDistance > 0 ? measuredWidth : -measuredWidth;
                firstLayoutParams.leftMargin = -currentPageIndex * measuredWidth;
                firstLayoutParams.leftMargin += xDistance;
                firstLayout.setLayoutParams(firstLayoutParams);
                if (itemChangedListener != null)
                    itemChangedListener.onPageStateChanged(currentPageIndex,
                            xDistance, false);
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("xVelocity:" + velocityTracker.getXVelocity());
                xDistance = firstLayoutParams.leftMargin - leftMargin;
                yDistance = yMove - yDown;
                if (Math.abs(xDistance) < Math.abs(yDistance))
                    return true;
                if (xDistance > 0 && currentPageIndex == 0 || xDistance < 0
                        && currentPageIndex == linearLayoutList.size() - 1)
                    return true;
                if (shouldScroll(xDistance, yDistance))
                {
                    isPageChanged = true;
                    isScrolling = true;
                    if (xDistance >= 0)
                    {
                        scroll(measuredWidth - xDistance);
                    }
                    else
                    {
                        scroll(-xDistance - measuredWidth);
                    }
                }
                else
                {
                    isScrolling = true;
                    scroll(-xDistance);
                }
                recycleVelocityTracker();
                break;
        }
        return true;
    }
    private void recycleVelocityTracker()
    {
        // TODO Auto-generated method stub
        velocityTracker.recycle();
        velocityTracker = null;
    }
    private void scroll(final int distance)
    {
        // TODO Auto-generated method stub
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                // TODO Auto-generated method stub
                // super.handleMessage(msg);
                firstLayoutParams.leftMargin += msg.arg1;
                firstLayout.setLayoutParams(firstLayoutParams);
                int moveDistance = firstLayoutParams.leftMargin - leftMargin;
                if (itemChangedListener != null)
                    if (msg.arg2 == -1)
                    {
                        itemChangedListener.onPageStateChanged(
                                currentPageIndex, moveDistance, true);
                        int previousPage = currentPageIndex;
                        isScrolling = false;
                        int xDistance = firstLayoutParams.leftMargin
                                - leftMargin;
                        if (isPageChanged)
                        {
                            if (xDistance > 0)
                                currentPageIndex--;
                            else
                            {
                                currentPageIndex++;
                            }
                        }
                        itemChangedListener.onPageChanged(previousPage,
                                currentPageIndex);
                        isPageChanged = false;
                    }
                    else
                        itemChangedListener.onPageStateChanged(
                                currentPageIndex, moveDistance, false);
            }
        };
        new Thread()
        {
            public void run()
            {
                // arg1代表移动的距离,arg2代表是否结束,-1表示结束。
                int moveDistance = 0;
                while (Math.abs(moveDistance) < Math.abs(distance))
                {
                    Message msg = Message.obtain();
                    if (distance < 0)
                    {
                        if (Math.abs(distance) - moveDistance > scrollDistanceUnit)
                        {
                            msg.arg1 = -scrollDistanceUnit;
                            msg.arg2 = 1;
                        }
                        else
                        {
                            msg.arg1 = Math.abs(moveDistance)
                                    - Math.abs(distance);
                            msg.arg2 = -1;
                        }
                    }
                    else
                    {
                        if (distance - moveDistance > scrollDistanceUnit)
                        {
                            msg.arg1 = scrollDistanceUnit;
                            msg.arg2 = 1;
                        }
                        else
                        {
                            msg.arg1 = distance - moveDistance;
                            msg.arg2 = -1;
                        }
                    }
                    // 休眠50毫秒，便于查看滑动效果
                    mHandler.sendMessage(msg);
                    try
                    {
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        // TODO: handle exception
                    }
                    moveDistance += scrollDistanceUnit;
                }
            }
        }.start();
    }
    // 滑动距离大于屏幕1/5或者移动速度大于scrollVelocityBound,
    // 并且当前没有滑动的情况,并且水平移动距离大于垂直距离时开始滑动
    private boolean shouldScroll(int distance, int yDistance)
    {
        // TODO Auto-generated method stub
        int moveDistance = Math.abs(distance);
        if (Math.abs(moveDistance) > scrollDistanceBound
                && Math.abs(distance) > Math.abs(yDistance))
            if (!isScrolling)
                return true;
            else
                return false;
        return false;
    }
    /**
     * 设置每个LinearLayout布局的宽度
     */
    private void configureMarginWidth(int measuredWidth)
    {
        // TODO Auto-generated method stub
        MarginLayoutParams params = null;
        this.measuredWidth = measuredWidth;
        for (LinearLayout linearLayout : linearLayoutList)
        {
            params = (MarginLayoutParams) linearLayout.getLayoutParams();
            params.width = measuredWidth;
        }
    }
    private void createVelocity(MotionEvent ev)
    {
        if (velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        // 当前页面正在滚动，拦截所有触摸事件
        if (isScrolling)
            return true;
        // 初始化参数，同时将hasActionMove置为false
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            xDown = (int) ev.getRawX();
            yDown = (int) ev.getRawY();
            xMove = 0;
            yMove = 0;
            xUp = 0;
            yUp = 0;
            currentLayout = linearLayoutList.get(currentPageIndex);
            if (!isConfigureMarginWidth)
                configureMarginWidth(currentLayout.getMeasuredWidth());
            firstLayout = (LinearLayout) linearLayoutList.get(0);
            firstLayoutParams = (MarginLayoutParams) firstLayout
                    .getLayoutParams();
            // 初始化最左边页面的边界线
            firstLayoutParams.leftMargin = -currentPageIndex * measuredWidth;
            firstLayout.setLayoutParams(firstLayoutParams);
            leftMargin = firstLayoutParams.leftMargin;
        }
        return super.onInterceptTouchEvent(ev);
    }
    public void setCurrentItem(int item)
    {
        if (isScrolling)
            throw new IllegalStateException("正在滚动中，无法更改");
        if (item >= linearLayoutList.size())
            throw new IndexOutOfBoundsException("数组访问非法");
        if (item == currentPageIndex)
            return;
        // 要求从右向左滑动
        isScrolling = true;
        firstLayoutParams.leftMargin = -currentPageIndex * measuredWidth;
        scroll(item * measuredWidth);
        currentPageIndex = item;
    }
    public void setOnItemChangedListeer(
            OnPageChangedListener itemChangedListener)
    {
        this.itemChangedListener = itemChangedListener;
    }
}
