package com.example.hoauy.studyapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Button btn_takePicture;

    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surfaceview);

        imageView = (ImageView)findViewById(R.id.imageView);

        btn_takePicture = (Button)findViewById(R.id.btn_takePicture);
        btn_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null,null, pictureCallback);
            }
        });

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        camera = Camera.open();

        try {

            Camera.Parameters params = camera.getParameters();
            params.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
            // 카메라 켜짐과 동시에 flash 를 키는 부분
            // flash 를 off 상태로 카메라를 시작하고 싶으면 이 부분 제거하면 됨.

            params.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);

            camera.setDisplayOrientation(90);
            // 카메라 화면이 90도 돌아가있음 --> 그래서 90도 돌려서 초기 설정함.

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        Camera.Parameters params = camera.getParameters();
        params.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
        camera.setParameters(params);
        camera.stopPreview();
        camera.release();
        camera = null;

    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmaporigin = BitmapFactory.decodeByteArray(data, 0, data.length);
            //원본 비트맵 파일. 초기 셋팅과 마찬가지로 90도 돌아가서 결과물(이미지)이 나온다.

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            // 90도 돌아가서 찍히는 것 회전시키기.
            // 전면 카메라의 경우 좌우가 반전되서 찍힘

            Bitmap bitmap = Bitmap.createBitmap(bitmaporigin, 0, 0,
                    bitmaporigin.getWidth(), bitmaporigin.getHeight(), matrix, true);

            imageView.setImageBitmap(bitmap);

            surfaceView.setVisibility(View.GONE);


        }
    };

//    < 참고 사항 >
//
//    1. 위의 결과물인 bitmap 을 파일로 저장하고 싶을 경우 아래 코드를 사용하면 됨.
//
//    2. 단, 이미지를 저장 할 filepath 를 지정하지 못하고 내장 메모리에 자동 저장됨.
//
//    String outUriStr =  MediaStore.Images.Media.insertImage(getContentResolver(),//이미지 파일 생성
//    bitmap, img_name, "Captured Image using Camera.");
//
//    uri 로 바꾸는 법 : Uri.parse(outUriStr)
//
//    Context.getContentResolver().delete(uri, null, null); --> uri (content:// 형식) 의 파일을 지우는 방법


}
