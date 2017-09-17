package com.austinthompson.easysudoku;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;


public class ShowCameraResult extends AppCompatActivity {

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_camera_result);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        Intent intent = getIntent();
        imageUrl = intent.getStringExtra(MainActivity.STRING_URL);


        int rotate = -1;
        try {
            rotate = setImageOrientation(imageUrl);
        } catch(IOException e) {
            Log.d("ShowCameraResult", e.getMessage());
        }

        //BitmapFactory.Options bmO = new BitmapFactory.Options();
        //bmO.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // if I need this code, add bmO as argument below

        // create bitmap from file, convert bitmap to mat, convert mat to grayscale
        Bitmap bm = BitmapFactory.decodeFile(imageUrl);
        Mat imgMat = new Mat();
        Utils.bitmapToMat(bm, imgMat);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgMat, imgMat, new Size(11,11), 0);
        Imgproc.adaptiveThreshold(imgMat,imgMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
        Core.bitwise_not(imgMat, imgMat);

        // convert mat to bitmap, rotate bitmap for portrait
        Utils.matToBitmap(imgMat, bm);
        Matrix mat = new Matrix();
        mat.setRotate(rotate, (float)bm.getWidth() / 2, (float)bm.getHeight() / 2);
        Bitmap bmRotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);

        // display bitmap in imageview
        ImageView iv = (ImageView)findViewById(R.id.imageView);
        iv.setImageBitmap(bmRotated);

    }

    private int setImageOrientation(String imgPath) throws IOException {
        ExifInterface exif = new ExifInterface(imgPath);
        String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = exifOrientation != null ? Integer.parseInt(exifOrientation) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationAngle = 270;
                break;
        }
        return rotationAngle;
    }
}