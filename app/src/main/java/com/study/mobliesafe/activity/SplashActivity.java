package com.study.mobliesafe.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.study.mobliesafe.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int UPDATE_VERSION = 100;
    private static final int ENTER_HOME = 101;
    private static final int E_ERROR = 102;
    private TextView tv_version_name;
    private int mLocalVersionCode;
    private String mVersionDes;
    private String mDownloadUrl;
    private ProgressDialog mProgressDialog;
    private int permissionRequestCode = 22;
    private String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String title = "相机权限不可用";
    String content = "由于上传照片需要获取相机相关权限，获取相册功能；\n否则，您将无法正常使用相机功能";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_VERSION:{
                    //弹出对话框
                    showUpdateDialog();
                    break;
                }
                case ENTER_HOME:{
                    //进入主界面
                    enterHome();
                    break;
                }
                case E_ERROR:{
                    //Toast提示出现异常
                    break;
                }
            }

        }

    };

    private void showUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("版本更新");
        builder.setMessage(mVersionDes);
        builder.setPositiveButton("立刻更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //更新,立即下载apk，downloadUrl
                downloadApk();
            }
        });
        builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //进入主界面
                enterHome();
            }
        });

        builder.show();
    }

    private void downloadApk() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            mProgressDialog = new ProgressDialog(SplashActivity.this);
            String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath()+ File.separator+"mobilesafe.apk";

            //
            RequestParams params = new RequestParams(mDownloadUrl);
            //设置下载后的保存路径
            params.setSaveFilePath(path);
            Log.d(TAG, "下载路径" + path);
            //下载完成后自动为文件命名
            params.setAutoRename(true);
            //HttpUtils
            org.xutils.common.Callback.Cancelable cancelable = x.http().get(params, new org.xutils.common.Callback.ProgressCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    Log.i(TAG, "下载成功");
                    mProgressDialog.dismiss();
                    //安装apk
                    installApk(result);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    //下载失败
                    Log.i(TAG, "下载失败");
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    //取消下载
                    Log.i(TAG, "取消下载");
                    mProgressDialog.dismiss();
                }

                @Override
                public void onFinished() {
                    //下载结束
                    Log.i(TAG, "结束下载");
                    mProgressDialog.dismiss();
                }

                @Override
                public void onWaiting() {
                    //等待下载
                    Log.i(TAG, "等待下载");
                }

                @Override
                public void onStarted() {
                    //刚刚开始下载
                    Log.i(TAG, "开始下载");
                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    //下载中
                    Log.i(TAG, "下载中...");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setMessage("正在下载中...");
                    mProgressDialog.show();
                    mProgressDialog.setMax((int) total);
                    mProgressDialog.setProgress((int) current);
                }
            });

        }
    }

    /**
     * 安卓Apk
     * @param file 此处是 mobliesafe.apk
     */
    private void installApk(File file) {
        Log.d("test1", "我运行了");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 7.0+以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "我再这");
            //与manifest中定义的provider中的authorities="com.shawpoo.app.fileprovider"保持一致
            Uri apkUri = FileProvider.getUriForFile(SplashActivity.this, "com.study.mobliesafe.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivityForResult(intent,0);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0){
            switch (requestCode){
                case 0:{
                    enterHome();
                }
                break;
            }
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * 进入HomeActivity
     */
    private void enterHome() {
        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
        startActivity(intent);
        //关闭Splash界面
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initUI();
        //动态权限获取
        initPermission(permission,title,content);
        initData();
    }

    /**
     * @param permissions   权限列表
     * @param title         AletDialog标题
     * @param content       AletDialog提示内容
     */
    private void initPermission(String[] permissions, String title, String content) {
        //M = 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++ ){
                if (ContextCompat.checkSelfPermission(SplashActivity.this,permission[i])!= PackageManager.PERMISSION_GRANTED){
                    permissionList.add(permission[i]);
                }
            }
            //如果permissionList不为空,show出对话框，提示应该给予权限
            if (!permissionList.isEmpty()){
                showDialogTipUserRequestPermission(permissionList,title,content);
            }
        }
    }


    /**
     * @param permissionList        把String[] permissions 赋值给List<>
     * @param title
     * @param content
     */
    private void showDialogTipUserRequestPermission(final List<String> permissionList, String title, String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission(permissionList);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    /**
     * @param permissionList
     */
    private void startRequestPermission(List<String> permissionList) {
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SplashActivity.this,permissions,permissionRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == permissionRequestCode){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (grantResults.length > 0){
                    for (int i=0; i <grantResults.length ; i++){
                        int grantResult = grantResults[i];
                        switch (grantResult){
                            case PackageManager.PERMISSION_GRANTED:
                                break;
                            case PackageManager.PERMISSION_DENIED:
                                Toast.makeText(SplashActivity.this,permission[i]+"权限获取失败",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void initData() {
        tv_version_name.setText(getVersionName());
        mLocalVersionCode = getVersionCode();
        checkVersion();

    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://10.0.2.2/update.json")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    versionJSONObject(responseData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void versionJSONObject(String jsonData){
        Message msg = Message.obtain();
        long startTime = System.currentTimeMillis();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
//                Update update = new Update();
//                update.setVersionCode(jsonObject.getString("versionCode"));
//                update.setVersionName(jsonObject.getString("versionName"));
//                update.setVersionDes(jsonObject.getString("versionDes"));
//                update.setDownloadUrl(jsonObject.getString("downloadUrl"));
            String versionName = jsonObject.getString("versionName");
            String versionCode = jsonObject.getString("versionCode");
            mVersionDes = jsonObject.getString("versionDes");
            mDownloadUrl = jsonObject.getString("downloadUrl");
            Log.i(TAG, "versionCode : " + versionCode);
            Log.i(TAG, "msg" + mLocalVersionCode);
            Log.i(TAG, "downloadUrl" + mDownloadUrl);

            if(mLocalVersionCode < Integer.parseInt(versionCode)){
                //提示更新,弹出对话框（UI）
                msg.what = UPDATE_VERSION;
            }else {
                msg.what = ENTER_HOME;
                //进入主界面
            }
        }catch (Exception e){
            e.printStackTrace();
            msg.what = E_ERROR;
        }finally {
            long endTime = System.currentTimeMillis();
            long Time = endTime - startTime;
                try {
                    if (Time < 1500) {
                        Thread.sleep(1500 - Time);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        mHandler.sendMessage(msg);
    }


    /**
     * @return
     */
    private int getVersionCode() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @return
     */
    private String getVersionName() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
    }
}
