package com.example.hoauy.studyapp;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;




/**
     1. 정의

      카메라 flash 를 키는 방법 및 소스


     2. 주의점

     안드로이드 6.0 (api 23 이상 ) 부터는 더 편하게 하는 방법이 있음.


    3. 참고 주소

     https://gh0st.tistory.com/19

 */






public class CameraFlashActivity extends AppCompatActivity {

    String TAG = "CameraFlashActivity";
    boolean flashState;
    Button btn_CameraFlash;
    Camera camera;


    @Override
    protected void onResume() {
        super.onResume();

        if (camera == null) {
            try {

                flashState = false;
                // 처음에는 flash 사용되기 전. : off 상태
                // flash 사용을 위해 버튼을 누르면 true 로 바뀜. : on 상태
                camera = Camera.open();

            } catch (RuntimeException e) {
                Toast.makeText(getApplicationContext(), "Camera open failed", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                delayedFinish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {

            flashState = false;
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameraflash);

        if(!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){

            Toast.makeText(getApplicationContext(),"플래시 사용이 불가능한 기기 입니다.",Toast.LENGTH_LONG).show();
            return;

        }


        btn_CameraFlash = (Button)findViewById(R.id.btn_Cameraflash);
        btn_CameraFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flashLight();

            }
        });

    }
    // onCreate() 하단


    public void flashLight(){

        if(!flashState){

            try {

                flashState = true;

                if(camera==null){
                    camera = Camera.open();
                }

                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

                camera.setParameters(params);
                camera.startPreview();


            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), "CameraOpenFail", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
        // flash 상태 off 인 상태인 경우 --> flash 를 켜야 하는 경우

        else {

                flashState = false;
                camera.stopPreview();
                camera.release();
                camera = null;


        }
        // flash 상태 on 인 경우 --> flash 를 꺼야 하는 경우.

    }
    // flashLight() 하단

    public void delayedFinish(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);

    }
   // delayedFinish() 하단


}
