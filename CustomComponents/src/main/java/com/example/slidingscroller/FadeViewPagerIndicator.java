package com.example.slidingscroller;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
public class FadeViewPagerIndicator extends LinearLayout implements
        ViewPagerIndicatorImpl
{

    private boolean loadOnce;
    private int itemWidth;
    private int childCount;
    private int currentItemIndex;
    TextView animationTv;
    LayoutParams animationParams;
    /**
     * alpha value of color
     */
    int initColor = 0x0B984B;
    int middleColor = 0xC0DCCD;
    float redInterpolator;
    float greenInterpolator;
    float blueInterpolator;
    int animLeft = 0;
    // int red = 0x0B;
    // int green = 0x98;
    // int blue = 0x4B;
    int animationColor = middleColor;
    public FadeViewPagerIndicator(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public FadeViewPagerIndicator(Context context, AttributeSet attr)
    {
        super(context, attr);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        // super.onLayout(changed, l, t, r, b);
        if (!loadOnce)
        {
            childCount = getChildCount();
            int parentWidth = getWidth();
            if (childCount <= 1)
                throw new IllegalArgumentException("至少需要两个孩子节点");
            else if (childCount <= 4)
                itemWidth = parentWidth / childCount;
            else
                itemWidth = parentWidth / 4;
            redInterpolator = ((initColor >> 16) - (middleColor >> 16))
                    / (itemWidth / 2.0f);
            greenInterpolator =
                    (((initColor & 0x00FFFF) >> 8) - ((middleColor & 0x00FFFF) >> 8))
                            / (itemWidth / 2.0f);
            blueInterpolator = ((initColor & 0x0000FF) - (middleColor & 0x0000FF))
                    / (itemWidth / 2.0f);
            View child = null;
            LayoutParams layoutParams = null;
            int mL = getLeft(), mT = getTop();
            for (int i = 0; i < childCount; i++)
            {
                child = getChildAt(i);
                ((TextView) child).setGravity(Gravity.CENTER);
                if (!(child instanceof TextView))
                    throw new IllegalArgumentException("孩子节点必须为TextView");
                layoutParams = (LayoutParams) child.getLayoutParams();
                layoutParams.width = itemWidth;
                layoutParams.height = getHeight();
                child.setLayoutParams(layoutParams);
                child.layout(mL, mT, mL + itemWidth, mT + layoutParams.height);
                mL += itemWidth;
            }
            mL = getLeft();
            animationTv = new TextView(getContext());
            animationTv.setLayoutParams(new LayoutParams(itemWidth,
                    LayoutParams.MATCH_PARENT));
            animationTv.setBackgroundColor(getResources().getColor(
                    R.color.tv_item_back));
            TextView currentTv = (TextView) getChildAt(currentItemIndex);
            animationTv.setText(currentTv.getText());
            animationTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    currentTv.getTextSize());
            animationTv
                    .setTextColor(getResources().getColor(R.color.initColor));
            animationTv.setGravity(Gravity.CENTER);
            addView(animationTv);
            animationTv.layout(mL, mT, itemWidth + mL, mT + getHeight());
            animationParams = (LayoutParams) animationTv.getLayoutParams();
            animLeft = animationTv.getLeft();
            animationParams.width = itemWidth;
            animationParams.leftMargin = 0;
            loadOnce = true;
        }
    }
    @Override
    public void itemPagerIndicatorScroll(View item, int itemIndex,
                                         int orientation, int scrollDistance,
                                         boolean isFinished)
    {
        // TODO Auto-generated method stub
        if (itemIndex < 0 || itemIndex > childCount)
            throw new IllegalArgumentException("页面参数不正确");
        int itemScrollDistance = (int) (1.0f * itemWidth
                / ScreenUtility.getScreenWidth() * scrollDistance);
        float rate = Math.abs(Math.abs(itemScrollDistance) - itemWidth / 2);
        int red = (int) ((animationColor >> 16) + rate * redInterpolator);
        int green = (int) (((animationColor & 0x00ffff) >> 8) + rate
                * greenInterpolator);
        int blue = (int) ((animationColor & 0x0000ff) + rate * blueInterpolator);
        System.out.println("color:" + Integer.toHexString(animationColor));
        if (orientation == LEFT_TO_RIGNT)
        {
            if (itemIndex == 0)
                return;
            TextView rightTv = ((TextView) getChildAt(itemIndex - 1));
            if (Math.abs(itemScrollDistance) >= itemWidth / 2)
            {
                animationTv.setText(rightTv.getText());
            }
        }
        else if (orientation == RIGHT_TO_LEFT)
        {
            if (itemIndex == childCount)
                return;
            // TranslateAnimation tranAni=new TranslateAnimation()
            TextView leftTv = ((TextView) getChildAt(itemIndex + 1));
            if (Math.abs(itemScrollDistance) >= itemWidth / 2)
                animationTv.setText(leftTv.getText());
        }
        int mL = animLeft - itemScrollDistance;
        animationTv.setTextColor(Color.argb(0xff, red, green, blue));
        animationTv.layout(mL, animationTv.getTop(), mL + itemWidth,
                animationTv.getBottom());
        if (isFinished)
            animLeft = animationTv.getLeft();
        System.out.println("animLeft:" + animLeft);
    }
}
