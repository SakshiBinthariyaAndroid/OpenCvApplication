package com.example.opencvapplication;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private static final String TAG = "MainActivity";

    private Mat mRgba;

    private CameraBridgeViewBase mOpenCvCameraView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.my_camera_view);


        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            mOpenCvCameraView.setCameraPermissionGranted();
            // mOpenCvCameraView.setCameraIndex(1);
            mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }


        if (OpenCVLoader.initDebug()) {

            Toast.makeText(this, "OpenCv configured successfully", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "OpenCv doesn’t configured successfully", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause--------------------");

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume--------------------");

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy--------------------");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        Log.d(TAG, "onCameraViewStarted--------------------");


    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped--------------------");
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

// if any contour exist...
      /*  if (mRgba.size().height > 0 && mRgba.size().width > 0)
        {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) mRgba.get(0, idx)[0])
            {
                Imgproc.drawContours(mRgba, contours, idx, new Scalar(250, 0, 0));
            }
        }*/

        Log.d(TAG, "onCameraFrame--------------------");
        mRgba = inputFrame.rgba();
        Mat touchedRegionHsv = new Mat();


        Mat blurred = new Mat();

        // Blur an image using a Gaussian filter
        Imgproc.GaussianBlur(mRgba, blurred, new Size(7, 7), 1);

        Imgproc.cvtColor(blurred, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);


//// InRange Thresholding

        Mat thresh = new Mat();


        Core.inRange(touchedRegionHsv, new Scalar(29, 86, 6),
                new Scalar(64, 255, 255), thresh);


//Creating destination matrix
        Mat erode = new Mat();  //(src.rows(), src.cols(), src.type());
//Preparing the kernel matrix object
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size((2 * 2) + 1, (2 * 2) + 1));
//Applying erosion on the Image
        Imgproc.erode(thresh, erode, kernel);

        Mat dilate = new Mat();
        Imgproc.dilate(erode, dilate, kernel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            markOuterContour(dilate, mRgba);
        }


        return mRgba;


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                mOpenCvCameraView.setCameraPermissionGranted();
                //  mOpenCvCameraView.setCameraIndex(1);
                mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
                mOpenCvCameraView.setCvCameraViewListener(this);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void markOuterContour(final Mat processedImage,
                                 final Mat originalImage) {
        // Find contours of an image
        final List<MatOfPoint> allContours = new ArrayList<>();
        Imgproc.findContours(
                processedImage,
                allContours,
                new Mat(processedImage.size(), processedImage.type()),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE
        );

        // Filter out noise and display contour area value
        final List<MatOfPoint> filteredContours = allContours.stream()
                .filter(contour -> {
                    final double value = Imgproc.contourArea(contour);
                    final Rect rect = Imgproc.boundingRect(contour);

                    final boolean isNotNoise = value > 1000;

                    return isNotNoise;
                }).collect(Collectors.toList());

        // Mark contours
        Imgproc.drawContours(
                originalImage,
                filteredContours,
                -1, // Negative value indicates that we want to draw all of contours
                // new Scalar(124, 252, 0), // Green color
                new Scalar(50, 50, 50), // Green color
                5
        );


    }
}