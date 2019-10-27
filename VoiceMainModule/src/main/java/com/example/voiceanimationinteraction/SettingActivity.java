package com.example.voiceanimationinteraction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.klicen.citychoosermodule.CityChooseActivity;

import utils.Constant;
/**
 * Created by Administrator on 2015/6/4.
 */
public class SettingActivity extends SupActivity implements View.OnClickListener
{

    TextView vadBox;
    TextView vadEox;
    TextView volume;
    TextView speed;
    TextView changeCity;
    String[] vadArray;
    String[] speedVolumeArray;
    int vadBoxIndex = 0;
    int vadEoxIndex = 0;
    int volumeIndex;
    int speedIndex;
    Dialog dialog;
    Dialog tipDialog;
    boolean shouldRestart;
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.setting_layout);
        vadBox = (TextView) findViewById(R.id.setting_vad_box);
        vadEox = (TextView) findViewById(R.id.setting_vad_eox);
        volume = (TextView) findViewById(R.id.setting_volume);
        speed = (TextView) findViewById(R.id.setting_speed);
        changeCity = (TextView) findViewById(R.id.setting_change_city);
        findViewById(R.id.setting_back).setOnClickListener(this);
        vadBox.setOnClickListener(this);
        vadEox.setOnClickListener(this);
        volume.setOnClickListener(this);
        speed.setOnClickListener(this);
        changeCity.setOnClickListener(this);
        configureContent();
        vadArray = getResources().getStringArray(R.array.time_array);
        speedVolumeArray = getResources().getStringArray(R.array
                .speed_volume_array);
    }
    private void configureContent()
    {
        vadBox.setText("前端点超时\n");
        vadBox.append(Html.fromHtml("<h7><small><font color='#cccccc'>" +
                Integer.parseInt(Constant
                        .getVadBox()) / 1000 + "秒</font></small></h7>"));
        vadEox.setText("后端点超时\n");
        vadEox.append(Html.fromHtml("<h7><small><font color='#cccccc'>" +
                Integer.parseInt(Constant
                        .getVadEox()) / 1000 + "秒</font></small></h7>"));
        volume.setText("朗读声音\n");
        volume.append(
                Html.fromHtml("<h7><small><font color='#cccccc'>" + Constant
                        .getVolume() + "</font></small></h7>"));
        speed.setText("朗读语速\n");
        speed.append(
                Html.fromHtml("<h7><small><font color='#cccccc'>" + Constant
                        .getSpeed() + "</font></small></h7>"));
        changeCity.setText("切换城市\n");
        changeCity.append(Html.fromHtml("<h7><small><font color='#cccccc'>" +
                Constant
                        .getCurrentProvince() + Constant.getCurrentCity() +
                "</font></small></h7>"));
    }
    @Override public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.setting_vad_box:
                String vadBoxTime = Integer.parseInt(Constant.getVadBox())
                        / 1000 + "秒";
                for (int i = 0; i < vadArray.length; i++)
                    if (vadArray[i].equals(vadBoxTime))
                    {
                        vadBoxIndex = i;
                        break;
                    }
                dialog = new AlertDialog.Builder(this).setTitle("前端点超时")
                        .setSingleChoiceItems
                                (vadArray, vadBoxIndex,
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override public void onClick(
                                                    DialogInterface dialog,
                                                    int which)
                                            {
                                                String
                                                        chooseItem =
                                                        vadArray[which];
                                                String time = Integer.parseInt
                                                        (chooseItem
                                                                .substring(0,
                                                                        chooseItem
                                                                                .length() -
                                                                                1)) *
                                                        1000 + "";
                                                Constant.setVadBox(time);
                                                dialog.cancel();
                                                configureContent();
                                                if (vadBoxIndex != which)
                                                {
                                                    shouldRestart = true;
                                                }
                                            }
                                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.setting_vad_eox:
                String vadEoxTime = Integer.parseInt(Constant.getVadBox())
                        / 1000 + "秒";
                for (int i = 0; i < vadArray.length; i++)
                    if (vadArray[i].equals(vadEoxTime))
                    {
                        vadEoxIndex = i;
                        break;
                    }
                dialog = new AlertDialog.Builder(this).setTitle("后端点超时")
                        .setSingleChoiceItems
                                (vadArray, vadEoxIndex,
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override public void onClick(
                                                    DialogInterface dialog,
                                                    int which)
                                            {
                                                String
                                                        chooseItem =
                                                        vadArray[which];
                                                String time = Integer.parseInt
                                                        (chooseItem
                                                                .substring(0,
                                                                        chooseItem
                                                                                .length() -
                                                                                1)) *
                                                        1000 + "";
                                                Constant.setVadEox(time);
                                                dialog.cancel();
                                                if (vadEoxIndex != which)
                                                {
                                                    shouldRestart = true;
                                                }
                                                configureContent();
                                            }
                                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.setting_volume:
                String volume = Constant.getVolume();
                for (int i = 0; i < speedVolumeArray.length; i++)
                    if (speedVolumeArray[i].equals(volume))
                    {
                        volumeIndex = i;
                        break;
                    }
                dialog = new AlertDialog.Builder(this).setTitle("音量大小")
                        .setSingleChoiceItems
                                (speedVolumeArray, volumeIndex,
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override public void onClick(
                                                    DialogInterface dialog,
                                                    int which)
                                            {
                                                String
                                                        chooseItem =
                                                        speedVolumeArray[which];
                                                Constant.setVolume(chooseItem);
                                                dialog.cancel();
                                                configureContent();
                                                if (volumeIndex != which)
                                                {
                                                    shouldRestart = true;
                                                }
                                            }
                                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.setting_speed:
                String speed = Constant.getSpeed();
                for (int i = 0; i < speedVolumeArray.length; i++)
                    if (speedVolumeArray[i].equals(speed))
                    {
                        speedIndex = i;
                        break;
                    }
                dialog = new AlertDialog.Builder(this).setTitle("语速大小")
                        .setSingleChoiceItems
                                (speedVolumeArray, speedIndex,
                                        new DialogInterface.OnClickListener()
                                        {
                                            @Override public void onClick(
                                                    DialogInterface dialog,
                                                    int which)
                                            {
                                                String
                                                        chooseItem =
                                                        speedVolumeArray[which];
                                                Constant.setSpeed(chooseItem);
                                                dialog.cancel();
                                                if (speedIndex != which)
                                                {
                                                    shouldRestart = true;
                                                }
                                                configureContent();
                                            }
                                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.setting_change_city:
                startActivityForResult(new Intent(this, CityChooseActivity
                        .class), 0x11);
                break;
            case R.id.setting_back:
                shouldRestart(shouldRestart);
                break;
        }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode,
                                              Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
        {
            return;
        }
        String province = data.getStringExtra(CityChooseActivity
                .EXTRA_PROVINCE).replace("省", "").trim().replace("市", "");
        String city = data.getStringExtra(CityChooseActivity.EXTRA_CITY)
                .replace("市", "").trim();
        if (province.equals("北京") || province.equals("上海") || province.equals
                ("重庆") || province.equals("天津"))
        {
            city = "";
        }
        Constant.setCurrentProvince(province);
        Constant.setCurrentCity(city);
        configureContent();
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        shouldRestart(shouldRestart);
        return super.onKeyDown(keyCode, event);
    }
    private void shouldRestart(final boolean shouldRestart)
    {
        if (shouldRestart)
        {
            tipDialog = new AlertDialog.Builder(this).setTitle("设置")
                    .setMessage("设置选项需要重启才能生效")
                    .setNegativeButton("取消",
                            new Dialog.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which)
                                {
                                    finish();
                                }
                            })
                    .setPositiveButton(
                            "重启",
                            new Dialog.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which)
                                {
                                    tipDialog.cancel();
                                    Constant.finishAllActivities(shouldRestart);
                                }
                            }).show();
        }
        else
        {
            finish();
        }
    }
}
