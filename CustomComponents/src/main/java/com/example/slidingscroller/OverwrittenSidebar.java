package com.example.slidingscroller;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
public class OverwrittenSidebar extends RelativeLayout implements
        OnTouchListener
{

    Context context;
    /**
     * 初始化标志
     */
    private boolean loadOnce;
    private int contentWidth;
    private int contentHeight;
    /**
     * 当触摸点落在0-leftover范围内时，侧边栏才能产生
     */
    private int leftoverDistance;
    private int menuPadding;
    private int leftMargin;
    private int scrollDistanceUnit;
    int scrollDistanceBound;
    private boolean isLeftMenuVisible;
    private boolean isScrolling;
    private LinearLayout menuLayout;
    private LayoutParams menuParams;
    private int xDown;
    private int yDown;
    private int xMove;
    private int yMove;
    private int xUp;
    private int yUp;
    public static final int DO_NOTHING = 0;
    public static final int SCROLL_TO_MENU = 1;
    public static final int SCROLL_TO_CONTENT = 2;
    private Handler mHandler;
    private int sidebarStatus = DO_NOTHING;
    public OverwrittenSidebar(Context context, AttributeSet set)
    {
        super(context, set);
        this.context = context;
        ScreenUtility.initParams(context);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce)
        {
            if (getChildCount() != 2)
                throw new IllegalArgumentException("孩子节点必须为两个");
            View child1 = getChildAt(0);
            contentWidth = child1.getMeasuredWidth();
            contentHeight = child1.getMeasuredHeight();
            /**
             * child2就是菜单内容
             */
            View child2 = getChildAt(1);
            removeAllViews();
            LinearLayout contentLayout = new MyLinearLayout(getContext());
            menuLayout = new MyLinearLayout(getContext());
            contentLayout.addView(child1);
            menuLayout.addView(child2);
            addView(contentLayout);
            addView(menuLayout);
            LayoutParams contentParams = (LayoutParams) contentLayout
                    .getLayoutParams();
            contentParams.width = contentWidth;
            contentParams.height = contentHeight;
            contentLayout.setLayoutParams(contentParams);
            menuParams = (LayoutParams) menuLayout.getLayoutParams();
            init();
            loadOnce = true;
            contentLayout.setOnTouchListener(this);
            menuLayout.setOnTouchListener(this);
        }
    }
    private void init()
    {
        int screenWidth = ScreenUtility.getScreenWidth();
        leftoverDistance = screenWidth / 70;
        menuPadding = screenWidth / 3;
        scrollDistanceUnit = screenWidth / 5;
        scrollDistanceBound = screenWidth / 5;
        menuParams.width = contentWidth - menuPadding;
        menuParams.height = contentHeight;
        menuParams.leftMargin = -menuParams.width;
        leftMargin = menuParams.leftMargin;
        menuLayout.setLayoutParams(menuParams);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        if (isScrolling)
            return true;
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            xDown = (int) ev.getRawX();
            yDown = (int) ev.getRawY();
            leftMargin = menuParams.leftMargin;
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        int xDistance = 0, yDistance = 0;
        System.out.println("action:" + event.getAction());
        System.out.println("actionMasked:" + event.getActionMasked());
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                xMove = (int) event.getRawX();
                yMove = (int) event.getRawY();
                menuParams.leftMargin = leftMargin;
                xDistance = xMove - xDown;
                yDistance = yMove - yDown;
                if (Math.abs(xDistance) <= Math.abs(yDistance))
                    return true;
                menuParams.leftMargin += xDistance;
                if (menuParams.leftMargin > 0)
                    menuParams.leftMargin = 0;
                if (menuParams.leftMargin < -menuParams.width)
                    menuParams.leftMargin = -menuParams.width;
                menuLayout.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                xDistance = xMove - xDown;
                yDistance = yMove - yDown;
                if (Math.abs(xDistance) <= Math.abs(yDistance))
                    return true;
                xUp = (int) event.getRawX();
                yUp = (int) event.getRawY();
                sidebarStatus = getSidebarStatus(xDistance, yDistance);
                /**
                 * 组件实际移动的水平距离
                 */
                xDistance = menuParams.leftMargin - leftMargin;
                switch (sidebarStatus)
                {
                    case DO_NOTHING:
                        scroll(-xDistance);
                        break;
                    case SCROLL_TO_MENU:
                        scroll(menuParams.width - xDistance);
                        isLeftMenuVisible = true;
                        break;
                    case SCROLL_TO_CONTENT:
                        scroll(-xDistance - menuParams.width);
                        isLeftMenuVisible = false;
                        break;
                }
                break;
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
    private int getSidebarStatus(int xDistance, int yDistance)
    {
        // TODO Auto-generated method stub
        if (Math.abs(xDistance) > Math.abs(yDistance)
                && Math.abs(xDistance) > scrollDistanceBound)
        {
            if (!isLeftMenuVisible && xDistance > 0)
            {
                return SCROLL_TO_MENU;
            }
            else if (isLeftMenuVisible && xDistance < 0)
            {
                return SCROLL_TO_CONTENT;
            }
        }
        return DO_NOTHING;
    }
}
