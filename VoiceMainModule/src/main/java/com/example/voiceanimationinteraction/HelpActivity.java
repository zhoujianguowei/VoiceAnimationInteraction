package com.example.voiceanimationinteraction;
import android.os.Bundle;
import android.view.Window;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;

import adapter.HelpGridViewAdapter;
public class HelpActivity extends SupActivity
{

    GridView gridView;
    int[] drawableSources = new int[]{R.mipmap.help_dial, R.mipmap.help_message,
            R.mipmap.help_contact, R.mipmap.help_app, R.mipmap.help_music, R
            .mipmap.help_weather, R.mipmap.help_map, R.mipmap.help_switch, R
            .mipmap.help_oil};
    String[] helpLableArray =
            new String[]{"打电话", "发短信", "联系人", "打开应用", "音乐", "天气",
                    "地图导航", "打开设置",
                    "油价查询"};
    String[] helpContentArray = new String[]{"打电话给张三", "发短信给李四", "查询李四", "打开相机",
            "播放音乐", "今天天气怎么样",
            "绵阳到成都怎么走", "打开wifi,关闭wifi",
            "安徽今日油价"};
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.help_layout);
        gridView = (GridView) findViewById(R.id.help_gridview);
        ArrayList<HashMap<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < drawableSources.length; i++)
        {
            HashMap<String, Object> map = new HashMap<>();
            map.put("icon", drawableSources[i]);
            map.put("label", helpLableArray[i]);
            map.put("content", helpContentArray[i]);
            mapList.add(map);
        }
        gridView.setAdapter(new HelpGridViewAdapter(this, mapList));
    }
}
