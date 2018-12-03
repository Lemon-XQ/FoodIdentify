package com.lemonxq_laplace.pregnantmonitor.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @author: Lemon-XQ
 * @date: 2018/2/22
 */

public class PhotoUtil {
    private final String tag = PhotoUtil.class.getSimpleName();

    /**
     * 裁剪图片成功后返回
     **/
    public static final int INTENT_CROP = 2;
    /**
     * 拍照成功后返回
     **/
    public static final int INTENT_TAKE = 3;
    /**
     * 拍照成功后返回
     **/
    public static final int INTENT_SELECT = 4;

    public static final String CROP_FILE_NAME = "crop_file.jpg";

    // 裁剪输出尺寸
    public int outputX = 200;
    public int outputY = 200;
    // 裁剪框尺寸
    public float aspectX = 1;
    public float aspectY = 1;

    /**
     * PhotoUtils对象
     **/
    private OnPhotoResultListener onPhotoResultListener;

    public PhotoUtil(){ }

    public PhotoUtil(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    /**
     * 拍照
     * @param activity
     * @return
     */
    public void takePicture(Activity activity) {
        try {
            //每次选择图片把之前的图片删除
            clearCropFile(buildUri(activity));
            File file = new File(activity.getExternalCacheDir(), "img.jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(activity,"com.lemonxq_laplace.pregnantmonitor.fileprovider", file));
            }else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, buildUri(activity));
            }
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildUri(activity));
            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_TAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 选择一张图片
     * 图片类型，这里是image/*，当然也可以设置限制
     * 如：image/jpeg等
     *
     * @param activity Activity
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Activity activity) {
        try {
            //每次选择图片把之前的图片删除
            clearCropFile(buildUri(activity));

            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_SELECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建uri
     *
     * @param activity
     * @return
     */
    private Uri buildUri(Activity activity) {
        if (Util.isSdcardExisting()) {
            return Uri.fromFile(Environment.getExternalStorageDirectory()).buildUpon().appendPath(CROP_FILE_NAME).build();
        } else {
            return Uri.fromFile(activity.getCacheDir()).buildUpon().appendPath(CROP_FILE_NAME).build();
        }
    }

    protected boolean isIntentAvailable(Activity activity, Intent intent) {
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private boolean crop(Activity activity, Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", aspectX);
        cropIntent.putExtra("aspectY", aspectY);
        cropIntent.putExtra("outputX", outputX);
        cropIntent.putExtra("outputY", outputY);
        //裁剪时是否保留图片的比例，这里的比例是1:1
//        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        Uri cropuri = buildUri(activity);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropuri);
        if (!isIntentAvailable(activity, cropIntent)) {
            return false;
        } else {
            try {
                activity.startActivityForResult(cropIntent, INTENT_CROP);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 返回结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (onPhotoResultListener == null) {
            Log.e(tag, "onPhotoResultListener is not null");
            return;
        }

        switch (requestCode) {
            //拍照
            case INTENT_TAKE:
                Uri cropUri;
                //设置文件保存路径这里放在跟目录下
                File picture = new File(activity.getExternalCacheDir() + "/img.jpg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    cropUri = FileProvider.getUriForFile(activity, "com.lemonxq_laplace.pregnantmonitor.fileprovider", picture);
                }else {
                    //裁剪照片
                    cropUri = Uri.fromFile(picture);
                }
                if (crop(activity, cropUri)) {
                    return;
                }
                onPhotoResultListener.onPhotoCancel();
//                if (new File(buildUri(activity).getPath()).exists()) {
//                    if (crop(activity, buildUri(activity))) {
//                        return;
//                    }
//                    onPhotoResultListener.onPhotoCancel();
//                }
                break;

            //选择图片
            case INTENT_SELECT:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    if (crop(activity, imageUri)) {
                        return;
                    }
                }
                onPhotoResultListener.onPhotoCancel();
                break;

            //截图
            case INTENT_CROP:
                if (resultCode == Activity.RESULT_OK && new File(buildUri(activity).getPath()).exists()) {
                    onPhotoResultListener.onPhotoResult(buildUri(activity));
                }
                break;
        }
    }

    /**
     * 删除文件
     *
     * @param uri
     * @return
     */
    public boolean clearCropFile(Uri uri) {
        if (uri == null) {
            return false;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                Log.i(tag, "Cached crop file cleared.");
            } else {
                Log.e(tag, "Failed to clear cached crop file.");
            }
            return result;
        } else {
            Log.w(tag, "Trying to clear cached crop file but it does not exist.");
        }

        return false;
    }

    /**
     * Uri转化为Bitmap文件
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap decodeUriAsBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (is == null){
            throw new RuntimeException("stream is null");
        }else{
            try {
                byte[] data=readStream(is);
                if(data!=null){
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        try {
//            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
        return bitmap;
    }

    /*
     * 得到图片字节流 数组大小
     * */
    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 保存Bitmap到本地
     * @param bitmap
     * @param path
     */
    public static void saveBitmapFile(Bitmap bitmap, String path) {
        File file = new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到图片存储路径
     * @param context
     * @return
     * @throws IOException
     */
    public static final String getImgPath(Context context) throws IOException
    {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath =context.getCacheDir().getPath();
        }
        File file = new File(cachePath+"/thumb");
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
            }
        }
        return cachePath+File.separator+"thumb";
    }

    public static byte[] bitmap2Bytes(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * [回调监听类]
     **/
    public interface OnPhotoResultListener {
        void onPhotoResult(Uri uri);

        void onPhotoCancel();
    }

    public OnPhotoResultListener getOnPhotoResultListener() {
        return onPhotoResultListener;
    }

    public void setOnPhotoResultListener(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    /**
     * 压缩图片，等比例缩放
     */
    public static Bitmap compressPhoto(Bitmap bitmap,int width,int height){
        if(bitmap==null){
            return null;
        }
        // 计算比例
        float scaleX = (float)width/bitmap.getWidth();
        float scaleY = (float)height/bitmap.getHeight();
        // 新的宽高
        int newW = 0;
        int newH = 0;
        if(scaleX > scaleY){
            newW = (int)(bitmap.getWidth()*scaleX);
            newH = (int)(bitmap.getHeight()*scaleX);
        }else{
            newW = (int)(bitmap.getWidth()*scaleY);
            newH = (int)(bitmap.getHeight()*scaleY);
        }
        return Bitmap.createScaledBitmap(bitmap,newW,newH,true);
    }
}
