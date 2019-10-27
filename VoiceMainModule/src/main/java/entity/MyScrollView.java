package entity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ScrollView;

import com.zhoujianguo.assistantTool.Queue;

import java.util.ArrayList;
/**
 * 重写ScrollView,解决ScrollView中嵌套AbsListView滑动问题
 *
 * @author Administrator
 */
public class MyScrollView extends ScrollView
{

    ArrayList<AbsListView> listViews;
    private boolean childrenStateChanged;//add or remove child
    private float downX, downY, moveX, moveY, upX, upY;
    private int touchSlop = 0;
    private static final float VERTICAL_GRADIENT = 1;
    private boolean downwardScroll = false;  //下拉滚动
    private boolean upwardScroll = false;  //上拉滚动
    public boolean isUpwardScroll()
    {
        return upwardScroll;
    }
    public void setUpwardScroll(boolean upwardScroll)
    {
        this.upwardScroll = upwardScroll;
    }
    public boolean isDownwardScroll()
    {
        return downwardScroll;
    }
    public void setDownwardScroll(boolean downwardScroll)
    {
        this.downwardScroll = downwardScroll;
    }
    @Override public void addView(View child, int width, int height)
    {
        childrenStateChanged = true;
        super.addView(child, width, height);
    }
    @Override public void removeView(View view)
    {
        childrenStateChanged = true;
        super.removeView(view);
    }
    public MyScrollView(Context context)
    {
        super(context);
    }
    public MyScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    @Override public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (touchSlop == 0)
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        return super.dispatchTouchEvent(ev);
    }
    private void resetCoordination()
    {
        downX = downY = moveX = moveY = upX = upY = 0;
    }
    private boolean isFirstItemVisibleTotal(AbsListView lv)
    {
        if (lv.getChildCount() == 0)
            return true;
        int firstVisibleItem = lv.getFirstVisiblePosition();
        if (firstVisibleItem > 0)
            return false;
//        int[] firstChildLocation = new int[2];
//        int[] lvLocation = new int[2];
        View firstChild = lv.getChildAt(0);
//        firstChild.getLocationOnScreen(firstChildLocation);
//        lv.getLocationOnScreen(lvLocation);
        return firstChild.getTop() >= 0 - touchSlop;
    }
    /**
     * listview的最后一个item完全显示出来
     */
    private boolean isLastItemVisibleTotal(AbsListView lv)
    {
        if (lv.getChildCount() == 0)
            return true;
        final ListAdapter adapter = lv.getAdapter();
        if (null == adapter || adapter.isEmpty())
        {
            return true;
        }
        final int lastItemPosition = adapter.getCount() - 1;
        final int lastVisiblePosition = lv.getLastVisiblePosition();
        int[] lvLocation = new int[2];
        int[] lastChildLocation = new int[2];
        if (lastVisiblePosition >= lastItemPosition)
        {
           /* final int childIndex =
                    lastVisiblePosition - lv.getFirstVisiblePosition();
            final int index = Math.min(childIndex, childCount - 1);*/
            final int childCount = lv.getChildCount();
            final View lastChild = lv.getChildAt(childCount - 1);
            lv.getLocationOnScreen(lvLocation);
            lastChild.getLocationOnScreen(lastChildLocation);
            int lvAbsoluteYCoordination = lvLocation[1] + lv.getHeight();
            int lastChildAbsoluteYCoordination =
                    lastChildLocation[1] + lastChild.getHeight();
            if (lastChild != null)
            {
                return lastChildAbsoluteYCoordination <=
                        lvAbsoluteYCoordination + touchSlop;
            }
        }
        return false;
//        int location[]=new int[2];
//        lv.getLocationInWindow();
    }
    @Override public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        boolean intercepted = true;
        int action = ev.getActionMasked();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                resetCoordination();
                if (listViews == null || childrenStateChanged)
                    listViews = getDecendantsListView(this);
                childrenStateChanged = false;
                downX = ev.getRawX();
                downY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                moveX = ev.getRawX();
                moveY = ev.getRawY();
                float distanceX = moveX - downX;
                float distanceY = moveY - downY;
                float gradient = Math.abs(distanceY / distanceX);
                if (gradient <= VERTICAL_GRADIENT)
                    break;
                if (listViews != null && listViews.size() > 0)
                {
                    for (AbsListView lv : listViews)
                    {
                        /*//下拉刷新listview的时候不要拦截
                        if (distanceY >= touchSlop && checkArea(ev, lv))
                            interceted = false;
                        *//*
                        上拉listview的时候，如果没有到达listview的底部，不拦截，如果到达，进行拦截
                         *//*
                        if (distanceY <= -touchSlop && checkArea(ev, lv))
                            interceted = isLastItemVisibleTotal(lv) ? true : false;*/
                        if (checkArea(ev, lv))
                        {
                            intercepted = false;
                            if (distanceY <= -touchSlop)
                            {
                                /**
                                 * 当前的item已经滑动到listview的底部，并且还在向上滑动
                                 * 拦截该MotionEvent,同时ScrollView滚动
                                 */
                                if (upwardScroll && isLastItemVisibleTotal(lv))
                                {
                                    intercepted = true;
                                    smoothScrollBy((int) distanceX, (int) distanceY);
                                }
                            }
                            else if (distanceY >= touchSlop)
                            {
                                /**
                                 * 当前的item是ListView的第一个item并且还在向下滑动
                                 */
                                if (downwardScroll && isFirstItemVisibleTotal(lv))
                                {
                                    intercepted = true;
                                    smoothScrollBy((int) distanceX, (int) distanceY);
                                }
                            }
                        }
                    }
                }
                break;
        }
//        Log.i("myscrollview " + ev.getActionMasked(), "intercpted:" + interceted);
        return intercepted == true ? super.onInterceptTouchEvent(ev) : false;
    }
    public static ArrayList<AbsListView> getDecendantsListView(ViewGroup container)
    {
        ArrayList<AbsListView> descendants = new ArrayList<AbsListView>();
        int firstChildCount = container.getChildCount();
        if (firstChildCount == 0)
            return descendants;
        Queue<View> myQueue = new Queue<View>();
        myQueue.enQueue(container);
        while (!myQueue.isEmpty())
        {
            View view = myQueue.deQueue();
            // 该组件是ViewGroup
            if (view instanceof AbsListView)
                descendants.add((AbsListView) (view));
            else if (view instanceof ViewGroup)
            {
                ViewGroup viewGroup = (ViewGroup) view;
                // myQueue.enQueqe(viewGroup);
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++)
                    if (viewGroup.getChildAt(i) instanceof ViewGroup)
                        myQueue.enQueue(viewGroup.getChildAt(i));
            }
        }
        return descendants;
    }
    private boolean checkArea(MotionEvent ev, AbsListView lv)
    {
        int[] location = new int[2];
        lv.getLocationInWindow(location);
        float rawX = ev.getRawX();
        float rawY = ev.getRawY();
        /**
         * be lenient about the range
         */
        if (rawX >= location[0] - touchSlop &&
                rawX <= location[0] + lv.getWidth() + touchSlop &&
                rawY >= location[1] - touchSlop &&
                rawY <= location[1] + lv.getHeight() + touchSlop)
            return true;
        return false;
    }
}