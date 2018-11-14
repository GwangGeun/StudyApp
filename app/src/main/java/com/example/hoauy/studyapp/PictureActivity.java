package com.example.hoauy.studyapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;



/**

 1. 정의

 카메라 & 앨범에서 사진 촬영 후 --> 발생 한 이미지 crop --> crop 된 이미지 갤러리에 저장



 2. 주의점

 (1) 모든 부분에서 발생하는 이미지의 결과물에 대해 uri 를 사용할 것 / bitmap 사용 시 화질이 깨짐

 (2) cropImage() 를 보면 nougat 이후에는 모든 부분에 permission 을 달아줘야함. 자세한 내용은

     하단 메소드 부분 참고.

 (3) crop 된 이미지를 저장하고 갤러리를 강제로 갱신시켜줘야 앨범에서 crop 된 이미지의 결과물이 보임

     ( fileprovider 로 추출한 결과물은 하단 addImageToGallery() 이 부분에 들어가있는 소스를
       통해서만 갱신이 가능. )

 (4) 카메라 촬영 후 발생한 이미지 파일을 저장 -->  해당 파일을 불러와서 crop --> crop 된 이미지 파일 저장

         1)                                                                 2)

    : 1) 에서 발생한 이미지 파일은 삭제 --> why? 사용자에게는 crop 된 이미지 파일만 필요하니까 !



 3. 참고 사이트

 (1) nougat os 이상 문제가 많음 --> 유의점 잘 나와있는 곳

     http://programmar.tistory.com/4

     https://github.com/DJDrama/CameraNOSTest/blob/master/app/src/main/java/dongster/cameranostest/MainActivity.java


 (2) 사진 저장후, 갤러리 갱신 하는 법 (해당링크 댓글 참고)

     http://www.androidside.com/plugin/mobile/board.php?bo_table=B49&wr_id=160324


 */

public class PictureActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgMain;
    private Button btnCamera, btnAlbum;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private Uri photoUri;
    String mCurrentPhotoPath;

    File crop_image;
    // crop 된 이미지가 저장될 파일
    File camera_image;
    // 카메라로 촬영한 원본 이미지가 저장될 파일
    // 이 파일은 crop 된 이미지 발생 이후 삭제 됨
    // why ? 카메라 촬영 후, 이미지를 저장 해놔야 crop 할 때, 저장되어 있는 이미지를 불러옴
    //       crop 된 이미지만 갤러리에 저장할 예정이므로 원본 파일은 삭제


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        initView();

    }



    private void initView() {
        imgMain = (ImageView) findViewById(R.id.img_test);
        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnAlbum = (Button) findViewById(R.id.btn_album);

        btnCamera.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile(1);
            // 카메라로 촬영한 이미지를 저장할 파일을 생성하려 할 경우에는 "인자" 가 "1" 이여야 한다.
            // 왜 구분 지었을까 ? crop 된 이미지는 갤러리에 저장 but 카메라로 촬영한 원본이미지는 저장하지 않고 삭제 할 것이므로 !

        } catch (IOException e) {
            Toast.makeText(PictureActivity.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(PictureActivity.this,
                    "com.example.hoauy.studyapp.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private File createImageFile(int a) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyymmddhhmmssSSS", Locale.KOREAN).format(new Date());
        String imageFileName = "test_" + timeStamp + "_";


        String folder = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/DCIM/StudyApp";
        File storageDir = new File(folder);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        if(a==1){

            camera_image = File.createTempFile(imageFileName, ".jpg", storageDir);
            mCurrentPhotoPath = "file:" + camera_image.getAbsolutePath();

            return camera_image;

        }
        // 카메라로 촬영 시 발생하는 이미지에 대해 저장할 파일 생성 --> 삭제 할 것임
        // why ? crop 된 이미지만 갤러리에 저장할 예정이므로.
        else {
            crop_image = File.createTempFile(imageFileName, ".jpg", storageDir);
            mCurrentPhotoPath = "file:" + crop_image.getAbsolutePath();

            return crop_image;
        }
        // 크랍 이미지를 저장할 파일 생성

    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                takePhoto();
                break;
            case R.id.btn_album:
                goToAlbum();
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }

            photoUri = data.getData();
            cropImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            cropImage();


        } else if (requestCode == CROP_FROM_CAMERA) {


            if(camera_image != null){
                camera_image.delete();
                // crop 된 이미지만 저장할 예정 --> camera 로 촬영한 원본 이미지는 삭제
            }


            addImageToGallery(crop_image.toString(), PictureActivity.this);
            // 목적 : crop 된 이미지 저장 -->  Gallery 에 바로 보여지지 않음 --> 위의 method 실행 시, 파일 저장과 동시에 Gallery 에 바로 보여짐

            imgMain.setImageURI(photoUri);

        }
    }

    //Android N crop image
    public void cropImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;

            try {
                croppedFileName = createImageFile(2);
                // crop 된 이미지를 저장할 파일을 생성하려 할 경우에는 "인자" 가 "1" 을 제외한 int 형의 숫자여야 함.

            } catch (IOException e) {
                e.printStackTrace();
            }

            String temp =   Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DCIM/StudyApp";

            File folder = new File(temp);
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(PictureActivity.this,
                    "com.example.hoauy.studyapp.fileprovider", tempFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));



            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }


    public static void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

}