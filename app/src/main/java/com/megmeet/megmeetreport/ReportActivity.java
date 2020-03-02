package com.megmeet.megmeetreport;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.megmeet.megmeetreport.bean.SQLBean;
import com.megmeet.megmeetreport.bean.VersionBean;
import com.megmeet.megmeetreport.fragment.HIKFragment;
import com.megmeet.megmeetreport.fragment.ReportFragment;
import com.megmeet.megmeetreport.net.PersonalProtocol;
import com.megmeet.megmeetreport.permission.PermissionHelper;
import com.megmeet.megmeetreport.permission.PermissionInterface;
import com.megmeet.megmeetreport.sql.Parameters;
import com.megmeet.megmeetreport.sql.SQLConnect;
import com.megmeet.megmeetreport.util.InformationUtils;
import com.megmeet.megmeetreport.util.RetrofitUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportActivity extends AppCompatActivity implements PermissionInterface {
    private static final int REPORTCOUNTS = 111;
    private static final int NULLMAC = 222;
//    private String url = "https://192.168.151.140:8075/WebReport/ReportServer?op=fs_load&cmd=login&fr_username=MesAdmin&fr_password=Megmeet190225WywCC&validity=-1&callback=callback\"";
    private String url = "https://10.3.1.11:8075/WebReport/ReportServer?op=fs_load&cmd=login&fr_username=MesAdmin&fr_password=Megmeet190225WywCC&validity=-1&callback=callback\"";
    ArrayList<Fragment> fragments;
    private int mUpdateTime = 30 * 60 * 1000;  //数据更新时间
    private int scrollTime = 8 * 60 * 1000;  //数据更新时间
    private int countFragment = 1;          //获取看板的页数
    private ReportAdapter reportAdapter;
    private PermissionHelper mPermissionHelper;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECEIVE_BOOT_COMPLETED};

    ViewPager viewpager;
    private WebView webView;
    private Handler handlerRunnable;
    private Handler handlerScroll;
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            initDatas();
            handlerRunnable.postDelayed(updateRunnable, mUpdateTime);
        }
    };
    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (reportAdapter != null && reportAdapter.getCount() > 1) {
                int childCount = reportAdapter.getCount();
                int currentItem = viewpager.getCurrentItem();
                if (currentItem < childCount - 1) {
                    viewpager.setCurrentItem(currentItem + 1);
                } else {
                    viewpager.setCurrentItem(0);
                }
                handlerScroll.postDelayed(scrollRunnable, scrollTime);
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPORTCOUNTS:
                    ArrayList<SQLBean> sqlBeans = (ArrayList<SQLBean>) msg.obj;
                    fragments = new ArrayList<>();
                    for (int i = 0; i < sqlBeans.size(); i++) {
                        int hik = sqlBeans.get(i).getHas_hik_version();
                        Log.e("len", sqlBeans.toString() + "HIK:" + hik);
                        Fragment fragment;
                        if (hik == 1) {
                            fragment = HIKFragment.newInstance(sqlBeans.get(i));
                        } else {
                            fragment = ReportFragment.newInstance(sqlBeans.get(i));
                            Log.e("len", "fragment");
                        }
                        int updateTime = sqlBeans.get(i).getUpdate_time();
                        if (updateTime > 0) {
                            mUpdateTime = updateTime * 60 * 1000;
                        }
                        fragments.add(fragment);
                    }
                    if (reportAdapter == null) {
                        reportAdapter = new ReportAdapter(getSupportFragmentManager(), fragments);
                    }
                    viewpager.setAdapter(reportAdapter);
                    if (sqlBeans.size() > 1) {
                        handlerScroll.postDelayed(scrollRunnable, scrollTime);
                    }
                    countFragment = sqlBeans.size();
//                    handlerRunnable.postDelayed(scrollRunnable, scrollTime);
                    break;
                case NULLMAC:
                    Toast.makeText(ReportActivity.this, "MAC地址没有维护：" + InformationUtils.getInformationUtils(ReportActivity.this).getMacAddress(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report);
        viewpager = findViewById(R.id.viewpager);
        webView = findViewById(R.id.webview);
        handlerRunnable = new Handler();
        handlerScroll = new Handler();
//        handlerRunnable.post(updateRunnable);
        //动态申请权限
        askPermission();
    }

    private void askPermission() {
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    private void initDatas() {
        //通过MAC地址获取数据
        new Thread() {
            @Override
            public void run() {
                String sql = "exec fm_get_kanban_by_mac_address ?";
                Parameters parameters = new Parameters().add(1, InformationUtils.getInformationUtils(ReportActivity.this).getMacAddress());
                ArrayList<SQLBean> sqlBeans = SQLConnect.connectSQL(sql, parameters);
                Log.e("len", InformationUtils.getInformationUtils(ReportActivity.this).getMacAddress());
                for (int i = 0; i < sqlBeans.size(); i++) {
                    Log.e("len", sqlBeans.get(i).getHas_hik_version() + sqlBeans.get(i).toString());
                }
                if (sqlBeans.size() > 0) {
                    Message message = new Message();
                    message.what = REPORTCOUNTS;
                    message.obj = sqlBeans;
                    handler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = NULLMAC;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
//            checkUpdate();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return permissions;
    }

    @Override
    public void requestPermissionsSuccess() {
        //权限请求用户已经全部允许
        checkUpdate();
//        loadReport();
    }

    @Override
    public void requestPermissionsFail() {

    }

    private void checkUpdate() {
        new Thread() {
            @Override
            public void run() {
                Retrofit retrofit = RetrofitUtil.getDefaultInstance(ReportActivity.this).getRetrofit();
                PersonalProtocol personalProtocol = retrofit.create(PersonalProtocol.class);
                Call<VersionBean> versionInfo = personalProtocol.getVersionInfo();
                versionInfo.enqueue(new Callback<VersionBean>() {
                    @Override
                    public void onResponse(Call<VersionBean> call, Response<VersionBean> response) {
                        if (response.isSuccessful()) {
                            if (response.errorBody() != null) {
                                loadReport();
                            } else {
                                try {
                                    final String serverVersion = response.body().getVersion();
                                    PackageInfo packInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String clientVersionName = packInfo.versionName;    //本地版本
                                    if (!serverVersion.equals(clientVersionName) && serverVersion.compareTo(clientVersionName) > 0) {
//                                        loadReport();
                                        new AlertDialog.Builder(ReportActivity.this)
                                                .setTitle("提示")
                                                .setMessage("有新版本" + serverVersion + "，是否下载?")
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                        loadReport();
                                                    }
                                                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                downLoadApp(serverVersion);
                                            }
                                        }).create().show();
                                    } else {
                                        loadReport();
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                    loadReport();
                                }
                            }
                        } else {
                            loadReport();
                        }
                    }

                    @Override
                    public void onFailure(Call<VersionBean> call, Throwable t) {
                        loadReport();
                    }
                });
            }
        }.start();
    }

    private void downLoadApp(final String serverVersion) {   //下载软件
        final String path = Environment.getExternalStorageDirectory() + File.separator + "report/report" + serverVersion + ".apk";
        Retrofit retrofit = RetrofitUtil.getDefaultInstance(ReportActivity.this).getRetrofit();
        retrofit.create(PersonalProtocol.class)
                .getDownFile()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String filePath = Environment.getExternalStorageDirectory() + File.separator + "/report/";
                        String fileName = "report" + serverVersion + ".apk";
                        InputStream in = null;
                        FileOutputStream out = null;
                        byte[] buf = new byte[2048];
                        int len;
                        File dir = new File(filePath);
                        if (!dir.exists()) {// 如果文件不存在新建一个
                            dir.mkdirs();
                        }
                        if (response.body() != null) {
                            try {
                                in = response.body().byteStream();
                                File file = new File(dir, fileName);
                                out = new FileOutputStream(file);
                                while ((len = in.read(buf)) != -1) {
                                    out.write(buf, 0, len);
                                }
                                installApp(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
//                            Toast.makeText(ReportActivity.this, "服务器上新版本的软件不见了，请联系管理员", Toast.LENGTH_SHORT).show();
                            loadReport();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        loadReport();
                    }
                });
    }

    public void installApp(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                data = Uri.fromFile(file);
            }
            intent.setDataAndType(data, "application/vnd.android.package-archive");
            startActivity(intent);
        } else {
            Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
            loadReport();
        }
    }

    class ReportWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            handlerRunnable.post(updateRunnable);
            webView.setVisibility(View.GONE);
            viewpager.setVisibility(View.VISIBLE);
            webView.removeAllViews();
            webView.destroy();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                webView.getSettings()
//                        .setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//            }
            handler.proceed();
        }

    }

    private void loadReport() {
        webView.loadUrl(url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new ReportWebClient());
        handlerRunnable = new Handler();
    }
}
