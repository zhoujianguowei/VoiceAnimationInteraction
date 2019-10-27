package com.example.voiceanimationinteraction;
import android.app.Activity;
import android.os.Bundle;

import utils.Constant;
/**
 * Created by Administrator on 2015/6/5.
 */
public class SupActivity extends Activity
{

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Constant.pushActivity(this);
    }
    @Override protected void onDestroy()
    {
        super.onDestroy();
        Constant.popActivity(this);
    }
}
