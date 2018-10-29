package com.channelst.headimgclip;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 主activity
 * 包括启动拍照,从系统相册中选择图片,裁剪图片然后显示等功能
 * 这里的权限管理使用了鸿洋大神的mpermission
 * 具体可查看http://blog.csdn.net/lmj623565791/article/details/50709663
 * @author 北京青牛软件南方基地_码农罗晶晶 2017-11-06
 */
public class MainActivity extends AppCompatActivity {


    public static final String HEAD_ICON_DIC = Environment
            .getExternalStorageDirectory()
            + File.separator + "headIcon";
    protected final String TAG = getClass().getSimpleName();
    private File headIconFile = null;// 相册或者拍照保存的文件
    private File headClipFile = null;// 裁剪后的头像
    private String headFileNameStr = "headIcon.jpg";
    private String clipFileNameStr = "clipIcon.jpg";
    private String headFileSuffix = ".jpg";
    Uri imageUri;
    private final String IMAGE_TYPE = "image/*";

    //权限相关
    public static final int REQUEST_EXTERNAL_STORAGE = 103;
    public static final String RECORD_STORAGE_GRANTED = "RECORD_STORAGE_GRANTED";
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_CAMERA = 104;
    public static final String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private boolean isCameraPermissionGranted;
    private boolean isStoragePermissionGranted;

    private final int CHOOSE_PHOTO_REQUEST_CODE = 0;
    private final int TAKE_PHOTO_REQUEST_CODE = 1;
    private final int CLIP_PHOTO_BY_SYSTEM_REQUEST_CODE = 2;
    private final int CLIP_PHOTO_BY_SELF_REQUEST_CODE = 3;

    @BindView(R.id.take_photo)
    Button takePhoto;
    @BindView(R.id.choose_photo)
    Button choosePhoto;
    @BindView(R.id.head_img)
    ImageView headImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //权限请求与判断
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED
                || hasReadStoragePermission != PackageManager.PERMISSION_GRANTED) {

            MPermissions.requestPermissions(this,
                    REQUEST_EXTERNAL_STORAGE, PERMISSIONS_STORAGE);
        } else {
            initHeadIconFile();
        }

    }

    @PermissionGrant(REQUEST_EXTERNAL_STORAGE)
    public void requestStorageSuccess() {
        //Toast.makeText(this, "GRANT Storage Success!", Toast.LENGTH_SHORT).show();
        //welcomeHandler.sendEmptyMessageDelayed(LOADING_SUCCESS_MSG, 1000);
        isStoragePermissionGranted = true;
        /*SharedPreferencesUtil.putBoolean(this, Constants.RECORD_STORAGE_GRANTED,true);*/
    }

    @PermissionDenied(REQUEST_EXTERNAL_STORAGE)
    public void requestStorageFailed() {
        isStoragePermissionGranted = false;
        //Toast.makeText(this, "DENY Storage PERMISSION!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @PermissionGrant(REQUEST_CAMERA)
    public void requestCameraSuccess() {
        //Toast.makeText(this, "GRANT Storage Success!", Toast.LENGTH_SHORT).show();
        //welcomeHandler.sendEmptyMessageDelayed(LOADING_SUCCESS_MSG, 1000);
        isCameraPermissionGranted = true;
        /*SharedPreferencesUtil.putBoolean(this, Constants.RECORD_STORAGE_GRANTED,true);*/
    }

    @PermissionDenied(REQUEST_CAMERA)
    public void setRequestCameraFailed() {
        isCameraPermissionGranted = false;
        Toast.makeText(this, "DENY camera PERMISSION!", Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick({R.id.take_photo, R.id.choose_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.take_photo:
                int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);

                if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                    MPermissions.requestPermissions(this,
                            REQUEST_CAMERA, PERMISSIONS_CAMERA);
                } else {
                    openCamera();
                }
                break;
            case R.id.choose_photo:
                choosePhoto();
                break;
        }
    }

    /**
     * 从系统图库中选择图片
     */
    private void choosePhoto() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {

            Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
            openAlbumIntent.setType(IMAGE_TYPE);
            startActivityForResult(openAlbumIntent, CHOOSE_PHOTO_REQUEST_CODE);

        }
    }

    /**
     * 打开系统摄像头拍照获取图片
     */
    private void openCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                imageUri = Uri.fromFile(headIconFile);
            } else {
                //FileProvider为7.0新增应用间共享文件,在7.0上暴露文件路径会报FileUriExposedException
                //为了适配7.0,所以需要使用FileProvider,具体使用百度一下即可
                imageUri = FileProvider.getUriForFile(this,
                        "com.channelst.headimgclip.fileprovider", headIconFile);//通过FileProvider创建一个content类型的Uri
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            Log.e(TAG, "openCamera()---intent" + intent);
        }
    }

    private void initHeadIconFile() {
        headIconFile = new File(HEAD_ICON_DIC);
        Log.e(TAG, "initHeadIconFile()---headIconFile.exists() : " + headIconFile.exists());
        if (!headIconFile.exists()) {
            boolean mkdirs = headIconFile.mkdirs();
            Log.e(TAG, "initHeadIconFile()---mkdirs : " + mkdirs);

        }
        headIconFile = new File(HEAD_ICON_DIC, headFileNameStr);
        headClipFile = new File(HEAD_ICON_DIC, clipFileNameStr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult()---requestCode" + requestCode
                + ", resultCode : " + resultCode);
        switch (requestCode) {
            case CLIP_PHOTO_BY_SYSTEM_REQUEST_CODE:
                Log.d(TAG,"调用系统剪辑照片后返回.........");
                if (resultCode == RESULT_OK) {
                    Bitmap bm = BitmapFactory.decodeFile(headClipFile.getAbsolutePath());

                    headImg.setImageBitmap(bm);
                    Log.e(TAG, "onActivityResult()---bm : " + bm);
                } else {
                    Log.e(TAG, "onActivityResult()---resultCode : " + resultCode);
                }
                break;
            case TAKE_PHOTO_REQUEST_CODE:
                Log.i(TAG,"拍照后返回.........");
                if (resultCode == RESULT_OK) {
                    //拍照后返回,调用系统裁剪,系统裁剪无法裁剪成圆形
                    //clipPhotoBySystem(imageUri);
                    //调用自定义裁剪
                    clipPhotoBySelf(headIconFile.getAbsolutePath());
                }
                break;
            case CHOOSE_PHOTO_REQUEST_CODE:
                Log.i(TAG, "从相册选取照片后返回....");
                if (resultCode == RESULT_OK) {

                    if (data != null) {
                        String filePath = "";
                        Uri originalUri = data.getData(); // 获得图片的uri
                        Log.i(TAG, "originalUri : " + originalUri);
                        if (originalUri != null) {
                            filePath = GetImagePath.getPath(this,originalUri);
                        }
                        Log.i(TAG, "filePath : " + filePath);

                        if (filePath != null && filePath.length() > 0) {
                            //clipPhotoBySystem(originalUri);
                            //调用自定义裁剪
                            clipPhotoBySelf(filePath);
                        }
                    }

                }
                break;
            case CLIP_PHOTO_BY_SELF_REQUEST_CODE:
                Log.i(TAG, "从自定义切图返回..........");
                if (resultCode == RESULT_OK) {

                    Bitmap bm = BitmapFactory.decodeFile(headClipFile.getAbsolutePath());
                    headImg.setImageBitmap(bm);
                    Log.i(TAG, "onActivityResult()---bm : " + bm);

                } else {
                    Log.i(TAG, "onActivityResult()---resultCode : " + resultCode);
                }
                break;
        }
    }

    /**
     * 系统裁剪图片
     * 调用系统裁剪无法裁剪成圆形
     *
     * @param inputUri
     */
    public void clipPhotoBySystem(Uri inputUri) {
        Log.i(TAG, "clipPhoneBySystem()---uri : " + inputUri);
        if (inputUri == null) {
            Log.i(TAG, "clipPhoneBySystem()--The uri is not exist.");
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        //sdk>=24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //图片输入源
            intent.setDataAndType(inputUri, IMAGE_TYPE);
            //裁剪后图片输出
            Uri outPutUri = Uri.fromFile(headClipFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁框重叠
            //临时授权该Uri所代表的文件的读权限,不加入该flag将导致无法加载图片
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //临时授权该Uri所代表的文件的写权限,不加入该flag将导致无法加载图片
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        } else {
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                String url = GetImagePath.getPath(this, inputUri);//这个方法是处理4.4以上图片返回的Uri对象不同的处理方法
                intent.setDataAndType(Uri.fromFile(new File(url)), IMAGE_TYPE);
            } else {
                intent.setDataAndType(inputUri, IMAGE_TYPE);
            }
            //裁剪后图片输出
            Uri outPutUri = Uri.fromFile(headClipFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        }


        //intent.setDataAndType(Uri.fromFile(headIconFile), "image/*");
        //是否裁剪
        intent.putExtra("crop", "true");
        //设置xy的裁剪比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置输出的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        //是否缩放
        intent.putExtra("scale", true);
        //是否返回图片数据，可以不用，直接用uri就可以了
        intent.putExtra("return-data", false);
        //设置输入图片格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //启动
        startActivityForResult(intent, CLIP_PHOTO_BY_SYSTEM_REQUEST_CODE);
    }

    /**
     * 调用自定义切图方法
     *
     * @param filePath
     */
    protected void clipPhotoBySelf(String filePath) {
        Log.i(TAG, "通过自定义方式去剪辑这个照片");
        //进入裁剪页面,此处用的是自定义的裁剪页面而不是调用系统裁剪
        Intent intent = new Intent(this, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_ORIGINAL, filePath);
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_AFTER_CROP,
                headClipFile.getAbsolutePath());

        startActivityForResult(intent, CLIP_PHOTO_BY_SELF_REQUEST_CODE);

    }

}
