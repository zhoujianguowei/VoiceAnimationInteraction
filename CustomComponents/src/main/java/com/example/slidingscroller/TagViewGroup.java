package com.example.slidingscroller;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedHashMap;
public class TagViewGroup extends ViewGroup
{

    private int mViewHspace;
    private int mViewVspace;
    private int color;
    public TagViewGroup(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public TagViewGroup(Context context, AttributeSet set)
    {
        super(context, set);
        TypedArray array = context.obtainStyledAttributes(set,
                R.styleable.tag_view_group);
        mViewHspace = array.getDimensionPixelSize(
                R.styleable.tag_view_group_viewHspace, 8);
        mViewVspace = array.getDimensionPixelSize(
                R.styleable.tag_view_group_viewVspace, 10);
        color = array.getColor(R.styleable.tag_view_group_backgroundColor,
                0xcc77777);
        array.recycle();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // size which child required
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        /**
         * keep width,height of the view
         */
        int lineWidth = 0, lineHeight = 0;
        int width = 0, height = 0, cCount = getChildCount();
        int childWidth = 0, childHeight = 0;
        View child = null;
        MarginLayoutParams childParams = null;
        for (int i = 0; i < cCount; i++)
        {
            child = getChildAt(i);
            childParams = (MarginLayoutParams) child.getLayoutParams();
            childWidth = child.getMeasuredWidth() + childParams.leftMargin
                    + childParams.rightMargin;
            childHeight = child.getMeasuredHeight() + childParams.topMargin
                    + childParams.bottomMargin;
            if (lineWidth + childWidth > widthSize)
            {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                height += (lineHeight + mViewVspace);
                lineHeight = childHeight;
            }
            else
            {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1)
            {
                height += lineHeight;
            }
            lineWidth = Math.min(lineWidth + mViewHspace, widthSize);
        }
        setBackgroundColor(color);
        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY ? widthSize
                : width), (heightMode == MeasureSpec.EXACTLY ? heightSize
                : height));
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int width = getWidth();
        int lineWidth = 0, lineHeight = 0;
        View child = null;
        MarginLayoutParams params = null;
        int childWidth = 0, childHeight = 0, cCount = getChildCount();
        // 存储每每一行最高的组件长度
        LinkedHashMap<Integer, Integer> maxLineHeight = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < cCount; i++)
        {
            child = getChildAt(i);
            if (child.getVisibility() == View.GONE)
                continue;
            params = (MarginLayoutParams) child.getLayoutParams();
            childWidth = params.leftMargin + params.rightMargin
                    + child.getMeasuredWidth();
            childHeight = params.topMargin + params.bottomMargin
                    + child.getMeasuredHeight();
            if (lineWidth + childWidth > width)
            {
                maxLineHeight.put(i, lineHeight);
                lineWidth = childWidth;
                lineHeight = childHeight;
            }
            else
            {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            lineWidth = Math.min(lineWidth + mViewHspace, width);
        }
        Integer[] maxLineHeightPos = new Integer[maxLineHeight.size()];
        maxLineHeight.keySet().toArray(maxLineHeightPos);
        lineWidth = 0;
        lineHeight = 0;
        int cL = 0, cT = 0, cR = 0, cB = 0;
        /**
         * set the position of child
         */
        for (int i = 0, j = 0; i < cCount; i++)
        {
            child = getChildAt(i);
            if (child.getVisibility() == View.GONE)
                continue;
            params = (MarginLayoutParams) child.getLayoutParams();
            childWidth = params.leftMargin + params.rightMargin
                    + child.getMeasuredWidth();
            childHeight = params.topMargin + params.bottomMargin
                    + child.getMeasuredHeight();
            if (j < maxLineHeightPos.length)
                if (i == maxLineHeightPos[j])
                {
                    lineWidth = 0;
                    lineHeight += (maxLineHeight.get(maxLineHeightPos[j]) +
                            mViewVspace);
                    j++;
                }
            cL = lineWidth + params.leftMargin;
            cT = lineHeight + params.topMargin;
            cR = cL + child.getMeasuredWidth();
            cB = cT + child.getMeasuredHeight();
            child.layout(cL, cT, cR, cB);
            lineWidth += (childWidth + mViewHspace);
        }
    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        // TODO Auto-generated method stub
        return new MarginLayoutParams(getContext(), attrs);
    }
    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        // TODO Auto-generated method stub
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p)
    {
        // TODO Auto-generated method stub
        return p;
    }
}
