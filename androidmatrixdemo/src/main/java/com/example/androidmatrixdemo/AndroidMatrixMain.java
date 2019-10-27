package com.example.androidmatrixdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
public class AndroidMatrixMain extends AppCompatActivity
        implements View.OnClickListener
{
    ImageView originalImage;
    TextView rotate;
    TextView translate;
    TextView scale;
    float translateX = 50;
    float translateY = 30;
    float rotateDegree = 30;
    float scaleMagnitude = 1.1F;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_matrix_main);
        originalImage = (ImageView) findViewById(R.id.beauty);
        originalImage.setDrawingCacheEnabled(true);
        translate = (TextView) findViewById(R.id.translate);
        rotate = (TextView) findViewById(R.id.rotate);
        scale = (TextView) findViewById(R.id.scale);
        translate.setOnClickListener(this);
        rotate.setOnClickListener(this);
        scale.setOnClickListener(this);
    }
    @Override
    public void onClick(View v)
    {
        originalImage.setDrawingCacheEnabled(true);
        Bitmap originalBitmap = originalImage.getDrawingCache();
        switch (v.getId())
        {
            case R.id.translate:
                translateImage(originalBitmap);
                break;
            case R.id.rotate:
                rotateImage(originalBitmap);
                break;
            case R.id.scale:
                scaleImage(originalBitmap);
                break;
        }
        if (originalBitmap.isRecycled() == false)
        {
            originalBitmap.recycle();
        }
        originalImage.setDrawingCacheEnabled(false);
    }
    /**
     * all graph transformation is based on the center of original graph
     */
    private void translateImage(Bitmap originalBitmap)
    {
        Matrix translateMatrix = new Matrix();
        translateMatrix.preTranslate(translateX, translateY);
        Bitmap afterBitmap = Bitmap.createBitmap(originalBitmap.getWidth(),
                originalBitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(afterBitmap);
        canvas.drawBitmap(originalBitmap, translateMatrix, new Paint());
        originalImage.setImageBitmap(afterBitmap);
    }
    private void rotateImage(Bitmap originalBitmap)
    {
        Matrix rotateMatrix = new Matrix();
        int realWidth = originalBitmap.getWidth();
        int realHeight = originalBitmap.getHeight();
        int imageRawx = originalImage.getLeft();
        int imageRawY = originalImage.getTop();
        //注意执行顺序,scale默认是以原点作为旋转中心，为了使得旋转依靠图形中心，
        // 所以首先应该将图像中心平移到原点，然后旋转，最后平移对应的距离。
        rotateMatrix.preTranslate(-(imageRawx + realWidth / 2),
                -(imageRawY + realHeight / 2));
        rotateMatrix.postRotate(rotateDegree);
        rotateMatrix.postTranslate(imageRawx + realWidth / 2,
                imageRawY + realHeight / 2);
        Bitmap afterBitmap = Bitmap.createBitmap(realWidth, realHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(afterBitmap);
        canvas.drawBitmap(originalBitmap, rotateMatrix, new Paint());
        originalImage.setImageBitmap(afterBitmap);
    }
    private void scaleImage(Bitmap originalBitmap)
    {
        Matrix scaleMatrix = new Matrix();
        int realWidth = originalBitmap.getWidth();
        int realHeight = originalBitmap.getHeight();
        int imageRawx = originalImage.getLeft();
        int imageRawY = originalImage.getTop();
        scaleMatrix.preTranslate(-(imageRawx + realWidth / 2),
                -(imageRawY + realHeight / 2));
        scaleMatrix.postScale(scaleMagnitude, scaleMagnitude);
        scaleMatrix.postTranslate(imageRawx + realWidth / 2,
                imageRawY + realHeight / 2);
        Bitmap afterBitmap = Bitmap.createBitmap(realWidth, realHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(afterBitmap);
        canvas.drawBitmap(originalBitmap, scaleMatrix, new Paint());
        originalImage.setImageBitmap(afterBitmap);
    }
}
