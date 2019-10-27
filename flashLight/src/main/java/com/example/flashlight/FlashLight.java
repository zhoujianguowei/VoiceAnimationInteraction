package com.example.flashlight;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 手电筒开启程序，android 7.0以后是通过CameraManager进行管理的
 */
public class FlashLight extends Activity implements OnClickListener {
    RelativeLayout root;
    Camera camera;
    ImageButton flashLight;
    ImageButton sos;
    Drawable[] controlDrawbles = null;
    Parameters parameters;
    volatile boolean continueSos;
    Handler sosHandler;
    final int FLASH_LIGHT_ON = 1;
    final int FLASH_LIGHT_OFF = -1;
    volatile boolean isLightOnGoging = true;
    NotificationManager mNotificationManager;
    private CameraManager cameraManager;
    private String targetCameraId;

    /**
     * android 26版本以及以后的通知设置参数
     *
     * @param savedInstanceState
     */
    private static final int FLASH_NOTIFICATION_ID = 0x002;
    private static final String FLASH_CHANNELID = "flash_channel_id";
    private static final String FLASH_CHANNEL_NAME = "flash_channel_name";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Resources resources = getResources();
        controlDrawbles = new Drawable[]{
                resources.getDrawable(R.drawable.flash_light_off),
                resources.getDrawable(R.drawable.flash_light_on),
                resources.getDrawable(R.drawable.sos_off),
                resources.getDrawable(R.drawable.sos_on),
                resources.getDrawable(R.drawable.background),
                resources.getDrawable(R.drawable.background_on)};
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        root = (RelativeLayout) findViewById(R.id.root);
        flashLight = (ImageButton) findViewById(R.id.flashLight);
        flashLight.setTag("open");
        sos = (ImageButton) findViewById(R.id.sos);
        sos.setTag("close");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        LayoutParams flashLightParams = (LayoutParams) flashLight
                .getLayoutParams();
        LayoutParams sosParams = (LayoutParams) sos.getLayoutParams();
        flashLightParams.setMargins(0, screenHeight * 1 / 2, 0, 0);
        sosParams.setMargins(0, screenHeight * 4 / 5, 0, 0);
        flashLight.setLayoutParams(flashLightParams);
        sos.setLayoutParams(sosParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = new String[0];
            try {
                cameraIds = cameraManager.getCameraIdList();
                for (String id : cameraIds) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && flashAvailable
                            && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {

                        targetCameraId = id;
                        //打开或关闭手电筒
                        cameraManager.setTorchMode(id, true);
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        }
        flashLight.setImageDrawable(controlDrawbles[1]);

        flashLight.setOnClickListener(this);
        sos.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        judge(v);

    }

    private void judge(View v) {
        if (!(v instanceof ImageButton))
            return;
        ImageButton controlIb = (ImageButton) v;

        continueSos = false;
        //只有两个按钮点击事件一个是flashLight，另一个是sos
        if (v.equals(flashLight)) {
            sos.setImageDrawable(controlDrawbles[2]);
            sos.setTag("close");
            if (v.getTag().equals("close")) {
                turnOnFlashLight();
                v.setTag("open");
                controlIb.setImageDrawable(controlDrawbles[1]);
                root.setBackground(controlDrawbles[5]);

            } else {
                turnOffFlashLight();
                v.setTag("close");
                controlIb.setImageDrawable(controlDrawbles[0]);
                root.setBackground(controlDrawbles[4]);

            }
        } else if (v.equals(sos)) {
            flashLight.setImageDrawable(controlDrawbles[0]);
            flashLight.setTag("close");
            if (v.getTag().equals("close")) {
                v.setTag("open");
                controlIb.setImageDrawable(controlDrawbles[3]);
                continueSos = true;
                sosHandler = new Handler() {
                    public void handleMessage(Message msg)

                    {
                        if (!continueSos)
                            return;
                        switch (msg.arg1) {
                            case FLASH_LIGHT_ON:
                                turnOnFlashLight();
                                root.setBackground(controlDrawbles[5]);
                                break;
                            case FLASH_LIGHT_OFF:
                                turnOffFlashLight();
                                root.setBackground(controlDrawbles[4]);
                                break;
                            default:
                                break;
                        }
                    }
                };
                new Thread() {
                    public void run() {
                        while (continueSos) {
                            Message msg = Message.obtain();
                            msg.arg1 = FLASH_LIGHT_ON;
                            sosHandler.sendMessage(msg);
                            try {
                                Thread.sleep(600);
                            } catch (Exception e) {
                                // TODO: handle exception
                                System.out.println("exception:"
                                        + e.getMessage());
                            }
                            Message message = Message.obtain();
                            message.arg1 = FLASH_LIGHT_OFF;
                            sosHandler.sendMessage(message);
                            try {
                                Thread.sleep(300);
                            } catch (Exception e) {
                                // TODO: handle exception
                                System.out.println("exception:"
                                        + e.getMessage());
                            }

                        }
                        Message message = Message.obtain();
                        message.arg1 = FLASH_LIGHT_OFF;
                        sosHandler.sendMessage(message);
                    }

                    ;
                }.start();

            } else {
                v.setTag("close");
                continueSos = false;
                turnOffFlashLight();
                controlIb.setImageDrawable(controlDrawbles[2]);
                root.setBackground(controlDrawbles[4]);

            }

        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        turnOffFlashLight();
        if (camera != null) {
            camera.release();
            camera = null;
        }
        mNotificationManager.cancel(FLASH_NOTIFICATION_ID);
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public NotificationChannel createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(FLASH_CHANNELID, FLASH_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
        channel.enableVibration(false);
        return channel;
    }

    /**
     * 发送通知
     *
     * @param iconResource
     * @param title
     * @param contentText
     */
    private void sendNotification(int iconResource, String title, String contentText) {
        Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(mNotificationManager);
            builder.setChannelId(FLASH_CHANNELID);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                new Intent(this, FlashLight.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(iconResource)
                .setAutoCancel(true).setTicker("FlashLight")
                .setContentTitle(title).setContentText(contentText)
                .setContentIntent(pendingIntent);
        mNotificationManager.notify(FLASH_NOTIFICATION_ID, builder.build());

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (continueSos || isLightOnGoging) {
            sendNotification(R.drawable.ic_launcher_flash_light, "手电筒", "手电筒正在使用");
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void turnOnFlashLight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (parameters != null && camera != null) {
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            }
        } else {
            try {
                cameraManager.setTorchMode(targetCameraId, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        isLightOnGoging = true;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            continueSos = false;
            turnOffFlashLight();
        }
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void turnOffFlashLight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (parameters != null && camera != null) {
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
            }
        } else {
            try {
                cameraManager.setTorchMode(targetCameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        isLightOnGoging = false;
//        mNotificationManager.cancelAll();
    }
}
