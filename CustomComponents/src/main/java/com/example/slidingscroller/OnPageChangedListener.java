package com.example.slidingscroller;
public interface OnPageChangedListener
{

    /**
     * 当前页面发生滚动时候回调该函数
     *
     * @param currentItemIndex 当前页面索引
     * @param scrollDistance   当前页面滚动距离
     */
    public void onPageStateChanged(int currentItemIndex,
                                   int scrollDistance, boolean isFinished);
    /**
     * 当前页面发生改变时候调用
     */
    public void onPageChanged(int previousPage, int currentPage);
}
