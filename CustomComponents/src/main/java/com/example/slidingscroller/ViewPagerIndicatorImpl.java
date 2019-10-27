package com.example.slidingscroller;
import android.view.View;
public interface ViewPagerIndicatorImpl
{

    public static final int LEFT_TO_RIGNT = 0;
    public static final int RIGHT_TO_LEFT = 1;
    /**
     * @param item           current page
     * @param scrollDistance distance of translation
     */
    public void itemPagerIndicatorScroll(View item, int currentItemIndex,
                                         int orientation, int scrollDistance,
                                         boolean isFinished);
}
