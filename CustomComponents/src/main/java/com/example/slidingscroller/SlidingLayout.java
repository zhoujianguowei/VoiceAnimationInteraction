package com.example.slidingscroller;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
public class SlidingLayout extends LinearLayout implements OnTouchListener
{

    private int scrollVelocityBound;
    private int scrollDistanceBound;
    private int scrollDistanceUnit;
    /**
     * ACTION_DOWN落点边界
     */
    private int leftBorder;
    LinearLayout menuLayout;
    volatile boolean isScrolling;
    LayoutParams menuParams;
    LayoutParams contentParams;
    Handler mHandler;
    int measuredWidth;
    int menuPadding;
    int xDown;
    int yDown;
    int xMove;
    int yMove;
    int xUp;
    int yUp;
    VelocityTracker velocityTracker;
    int leftMargin;
    /**
     * 记录滑动过程中最大水平速度
     */
    int maxXvelocity;
    int pointerId;
    static final int SCROll_NOTHING = 0;
    static final int SCROLL_TO_MENU = 1;
    static final int SCROll_TO_CONTENT = 2;
    public SlidingLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public SlidingLayout(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }
    private void initParams()
    {
        // TODO Auto-generated method stub
        ScreenUtility.initParams(getContext());
        measuredWidth = getMeasuredWidth();
        menuPadding = measuredWidth / 3;
        scrollVelocityBound = ScreenUtility.getScreenWidth() * 2;
        scrollDistanceBound = ScreenUtility.getScreenWidth() / 5;
        scrollDistanceUnit = ScreenUtility.getScreenWidth() / 5;
        leftBorder = ScreenUtility.getScreenWidth() / 70;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        if (changed)
        {
            initParams();
            // 如果子类的个数不是2，报错
            if (getChildCount() != 2)
                throw new IllegalArgumentException("子类组件个数只能为2");
            // 将子类封装成LinearLayout
            if (menuLayout == null)
            {
                setHorizontalGravity(HORIZONTAL);
                menuLayout = new LinearLayout(getContext());
                LinearLayout contentLayout = new LinearLayout(getContext());
                View leftChild = getChildAt(0);
                View rightChild = getChildAt(1);
                removeAllViews();
                menuLayout.addView(leftChild);
                contentLayout.addView(rightChild);
                addView(menuLayout);
                addView(contentLayout);
                menuParams = (LayoutParams) menuLayout.getLayoutParams();
                LayoutParams contentParams = (LayoutParams) contentLayout
                        .getLayoutParams();
                menuParams.width = measuredWidth - menuPadding;
                menuParams.leftMargin = -menuParams.width;
                menuParams.height = LayoutParams.MATCH_PARENT;
                contentParams.width = measuredWidth;
                contentParams.height = LayoutParams.MATCH_PARENT;
                menuLayout.setLayoutParams(menuParams);
                contentLayout.setLayoutParams(contentParams);
                menuLayout.setOnTouchListener(this);
                contentLayout.setOnTouchListener(this);
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            xDown = (int) ev.getRawX();
            yDown = (int) ev.getRawY();
            pointerId = ev.getPointerId(0);
            maxXvelocity = 0;
            leftMargin = menuParams.leftMargin;
        }
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        int xDistance = 0;
        int yDistance = 0;
        System.out.println("action:" + event.getAction());
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                menuParams.leftMargin = leftMargin;
                xMove = (int) event.getRawX();
                yMove = (int) event.getRawY();
                xDistance = xMove - xDown;
                yDistance = yMove - yDown;
                isScrolling = true;
                menuParams.leftMargin += xDistance;
                if (menuParams.leftMargin < -menuParams.width)
                    menuParams.leftMargin = -menuParams.width;
                if (menuParams.leftMargin > 0)
                    menuParams.leftMargin = 0;
                menuLayout.setLayoutParams(menuParams);
                velocityTracker.computeCurrentVelocity(1000);
                int xVelocity = (int) velocityTracker.getXVelocity();
                if (Math.abs(xVelocity) > maxXvelocity)
                {
                    maxXvelocity = Math.abs(xVelocity);
                }
                break;
            case MotionEvent.ACTION_UP:
                xUp = (int) event.getRawX();
                yUp = (int) event.getRawY();
                xDistance = xMove - xDown;
                yDistance = yMove - yDown;
                // 实际移动的xDistance应该是menuParams.leftMargin-leftMargin
                switch (getScrollState(leftMargin, xDistance, yDistance))
                {
                    case SCROll_NOTHING:
                        scroll(-(menuParams.leftMargin - leftMargin));
                        break;
                    case SCROLL_TO_MENU:
                        scroll(menuParams.width -
                                (menuParams.leftMargin - leftMargin));
                        break;
                    case SCROll_TO_CONTENT:
                        scroll(-menuParams.width -
                                (menuParams.leftMargin - leftMargin));
                        recycleVelocityTracker();
                        break;
                }
        }
        return true;
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
                menuParams.leftMargin += msg.arg1;
                menuLayout.setLayoutParams(menuParams);
                if (msg.arg2 == -1)
                {
                    isScrolling = false;
                }
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
                        Thread.sleep(50);
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
    private int getScrollState(int leftMargin, int xDistance, int yDistance)
    {
        // TODO Auto-generated method stub
        // 滑动到内容页面
        if (Math.abs(xDistance) > Math.abs(yDistance)
                && (Math.abs(xDistance) > scrollDistanceBound || velocityTracker
                .getXVelocity(pointerId) > scrollVelocityBound))
        {
            if (leftMargin < 0 && xDistance > 0)
                return SCROLL_TO_MENU;
            else if (leftMargin == 0 && xDistance < 0)
                return SCROll_TO_CONTENT;
        }
        return SCROll_NOTHING;
    }
    private void createVelocityTracker(MotionEvent ev)
    {
        if (velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
    }
    private void recycleVelocityTracker()
    {
        if (velocityTracker != null)
            velocityTracker.recycle();
        velocityTracker = null;
    }
}
