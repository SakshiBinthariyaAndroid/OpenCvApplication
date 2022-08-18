package com.example.opencvapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.charset.CoderResult;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final int MY_CAMERA_REQUEST_CODE = 100;
  //  int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT;
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    private static String TAGs = "MainActivity";
    JavaCameraView javaCameraView;

    Mat mRGBA, mRGBAT;

    BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                }
            }
        }
    };

    static {

        if (OpenCVLoader.initDebug()) {
            Log.d(TAGs, "OpenCv configured successfully");
        } else {
            Log.d(TAGs, "OpenCv doesn’t configured successfully");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaCameraView=(JavaCameraView) findViewById(R.id.my_camera_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera(javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

    }


   /* private void activateOpenCVCameraView() {
        // everything needed to start a camera preview
        //mOpenCvCameraView = binding.cameraView
        javaCameraView.setCameraPermissionGranted();
       // mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY)


        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        //  javaCameraView.setCvCameraViewListener((CameraBridgeViewBase.CvCameraViewListener) MainActivity.this);
        javaCameraView.setCvCameraViewListener(this);

        javaCameraView.enableView();
    }

*/
    @Override
    public void onCameraViewStarted(int width, int height) {

        mRGBA= new Mat(width,height, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        /*mRGBA=inputFrame.rgba();
        mRGBAT=mRGBA.t();
        Core.flip(mRGBA.t(),mRGBAT,12);
        Imgproc.resize(mRGBAT,mRGBAT,mRGBA.size());
        return mRGBAT;*/

        //Core.transpose(mRGBA, mRGBAT);


       mRGBA = inputFrame.rgba();
        return mRGBA;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();


        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAGs, "OpenCv configured successfully");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            Log.d(TAGs, "OpenCv doesn’t configured successfully");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this,baseLoaderCallback);
        }

    }

    // callback to be executed after the user has givenapproval or rejection via system prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera){
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }



}
