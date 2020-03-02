package com.megmeet.megmeetreport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.megmeet.megmeetreport.bean.SQLBean;
import com.megmeet.megmeetreport.bean.VersionBean;
import com.megmeet.megmeetreport.fragment.ReportFragment;
import com.megmeet.megmeetreport.net.PersonalProtocol;
import com.megmeet.megmeetreport.permission.PermissionHelper;
import com.megmeet.megmeetreport.permission.PermissionInterface;
import com.megmeet.megmeetreport.sql.Parameters;
import com.megmeet.megmeetreport.sql.SQLConnect;
import com.megmeet.megmeetreport.util.InformationUtils;
import com.megmeet.megmeetreport.util.RetrofitUtil;

import java.io.File;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportHtmlActivity extends AppCompatActivity implements PermissionInterface {
    private static final int LOADREPORTBYMAC = 0;
    private static final int NOMAINTAINMAC = 1;
    private static final int DEALVERSION = 2;
    private static final int SERVERNOAPP = 3;
    private String url = "https://10.3.1.11:8075/WebReport/ReportServer?op=fs_load&cmd=login&fr_username=MesAdmin&fr_password=Megmeet190225WywCC&validity=-1&callback=callback\"";
    ArrayList<Fragment> fragments = new ArrayList<>();
    private int mUpdateTime = 3 * 60 * 1000;  //数据更新时间
    private int scrollTime = 3 * 60 * 1000;  //切换界面时间
    private int countFragment = 1;          //获取看板的页数
    private WebView webView;

    ViewPager viewpager;
    private Handler handlerRunnable;
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            initDatas();
            handler.postDelayed(updateRunnable, mUpdateTime);
        }
    };
    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (reportAdapter != null) {
                int childCount = reportAdapter.getCount();
                int currentItem = viewpager.getCurrentItem();
                if (currentItem < childCount - 1) {
                    viewpager.setCurrentItem(currentItem + 1);
                } else {
                    viewpager.setCurrentItem(0);
                }
                handler.postDelayed(scrollRunnable, scrollTime);
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case LOADREPORTBYMAC:
                    ArrayList<SQLBean> sqlBeans = (ArrayList<SQLBean>) msg.obj;
                    for (int i = 0; i < sqlBeans.size(); i++) {
                        ReportFragment reportFragment = ReportFragment.newInstance(sqlBeans.get(i));
                        int updateTime = sqlBeans.get(i).getUpdate_time();
                        if (updateTime > 0) {
                            mUpdateTime = updateTime * 60 * 1000;
                        }
                        fragments.add(reportFragment);
                    }
                    if (reportAdapter == null) {
                        reportAdapter = new ReportAdapter(getSupportFragmentManager(), fragments);
                    }
                    viewpager.setAdapter(reportAdapter);
                    countFragment = sqlBeans.size();
                    handlerRunnable.postDelayed(scrollRunnable, scrollTime);
                    break;
                case NOMAINTAINMAC:
                    toastError("MAC地址为：" + InformationUtils.getInformationUtils(ReportHtmlActivity.this).getMacAddress() + "的设备没有维护需要显示的看板，请联系管理员添加。");
                    break;
                case DEALVERSION:
                    VersionBean versionBean = (VersionBean) msg.obj;
                    if (versionBean != null) {
                        String serviceVersion = versionBean.getVersion();
                        String locationVersion = null;
                        try {
                            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            locationVersion = packageInfo.versionName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.e("len", locationVersion + "**" + serviceVersion);
                        if (!TextUtils.isEmpty(locationVersion) && serviceVersion.compareTo(locationVersion) > 0) {   //有更新
                            //下载安装
                            downLoadAndInstall(serviceVersion, versionBean.getAddress());
                        } else {          //没有更新
                            loadReport();
                        }
                    } else {
                        loadReport();
                    }
                    break;
                case SERVERNOAPP:
                    loadReport();
                    break;
            }
        }
    };
    private LinearLayout linearLayout;
    private TextView textViewUpdate;
    private ProgressBar progressBar;
    private String downFilePath;
    private PermissionHelper mPermissionHelper;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report);
        viewpager = findViewById(R.id.viewpager);
        webView = findViewById(R.id.webview);
        linearLayout = findViewById(R.id.linearlayout_update);
        textViewUpdate = findViewById(R.id.text_update);
        progressBar = findViewById(R.id.progress_update);
        progressBar.setMax(100);
        WindowManager windowManager = getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int width = defaultDisplay.getWidth();
        int height = defaultDisplay.getHeight();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width / 4, height / 24);
        progressBar.setLayoutParams(layoutParams);
        //动态申请权限
        askPermission();
//        checkUpdate();        //检查更新
    }

    private void askPermission() {
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    public void toastError(String message) {
        View toastRoot = ReportHtmlActivity.this.getLayoutInflater().inflate(R.layout.toast, null);
        TextView txt = (TextView) toastRoot.findViewById(R.id.txt_toast);
        txt.setBackgroundResource(R.color.red);
        txt.setTextColor(Color.WHITE);
        txt.setText(message);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastRoot);
        toast.show();
    }

    private void downLoadAndInstall(String version, String address) {
        String baseUrl = Environment.getExternalStorageDirectory() + File.separator + "MegmeetReport";
        //先删除文件夹下面的文件
        File dirs = new File(baseUrl);
        if (dirs.exists() && dirs.isDirectory()) {
            File[] files = dirs.listFiles();
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
        Log.e("len", "base:" + baseUrl);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        downFilePath = baseUrl + version + ".apk";
        Log.e("len", "DownFile:" + downFilePath);
        linearLayout.setVisibility(View.VISIBLE);
        FileDownloader.setup(this);
        final FileDownloader fileDownloader = new FileDownloader();
        fileDownloader.getImpl().create(address)
                .setPath(downFilePath)
                .setForceReDownload(true)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        if (totalBytes > 0) {
                            textViewUpdate.setText("正在下载..." + (soFarBytes / totalBytes) * 100 + "%");
                            progressBar.setProgress((int) (soFarBytes / totalBytes) * 100);
                            progressBar.setMax(100);
                            progressBar.setSecondaryProgress(R.color.colorAccent);
                        }
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.e("len", "进度：" + (soFarBytes * 100 / totalBytes));
                        textViewUpdate.setText("正在下载..." + (soFarBytes * 100 / totalBytes) + "%");
                        progressBar.setProgress((int) (soFarBytes * 100 / totalBytes));
                        progressBar.setMax(100);
                        progressBar.setSecondaryProgress(R.color.colorAccent);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        textViewUpdate.setText("下载完成");
                        linearLayout.setVisibility(View.GONE);
                        //安装新App
                        File file = new File(downFilePath);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri uri = FileProvider.getUriForFile(ReportHtmlActivity.this, getPackageName() + "fileprovider", file);
                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        }
                        startActivity(intent);

//                        File file = new File(downFilePath);
//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            Uri uri = FileProvider.getUriForFile(ReportActivity.this, "com.megmeet.megmeetreport.fileprovider", file);
//                            intent.setDataAndType(uri, "application/vnd.android.package-archive");
//                        } else {
//                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//                        }
//                        startActivity(intent);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        textViewUpdate.setText("下载失败" + e.getMessage());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    private ReportAdapter reportAdapter;

    private void checkUpdate() {
        new Thread() {
            @Override
            public void run() {
                Retrofit retrofit = RetrofitUtil.getDefaultInstance(ReportHtmlActivity.this).getRetrofit();
                PersonalProtocol personalProtocol = retrofit.create(PersonalProtocol.class);
                Call<VersionBean> versionInfo = personalProtocol.getVersionInfo();
                versionInfo.enqueue(new Callback<VersionBean>() {
                    @Override
                    public void onResponse(Call<VersionBean> call, Response<VersionBean> response) {
                        loadReport();
                    }

                    @Override
                    public void onFailure(Call<VersionBean> call, Throwable t) {

                    }
                });
//                String path = "http://192.168.0.103:8018/mes/report/report.json";
//                Log.e("len", "START");
//                HttpURLConnection urlConnection = null;
//                try {
//                    URL url = new URL(path);
//                    urlConnection = (HttpURLConnection) url.openConnection();
//                    urlConnection.setRequestMethod("GET");
//                    urlConnection.setConnectTimeout(5 * 1000);
//                    Log.e("len", urlConnection.getResponseCode() + "&&&" + urlConnection.getRequestMethod());
//                    if (urlConnection.getResponseCode() == 200) {
//                        InputStream is = urlConnection.getInputStream();
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        byte[] buffer = new byte[is.available()];
//                        int len = 0;
//                        while (-1 != (len = is.read(buffer))) {
//                            baos.write(buffer, 0, len);
//                            baos.flush();
//                        }
//                        String backGson = baos.toString();
//                        Log.e("len", backGson);
//                        Gson gson = new Gson();
//                        VersionBean versionBean = gson.fromJson(backGson, VersionBean.class);
//                        Message message = new Message();
//                        message.arg1 = DEALVERSION;
//                        message.obj = versionBean;
//                        handler.sendMessage(message);
//                    } else {   //获取不到服务器数据，直接
//                        Message message = new Message();
//                        message.arg1 = SERVERNOAPP;
//                        handler.sendMessage(message);
//                    }
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                    Log.e("len", "ProtocolException:" + e.getMessage());
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                    Log.e("len", "MalformedURLException:" + e.getMessage());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("len", "IOException:" + e.getMessage());
//                } finally {
//                    urlConnection.disconnect();
//                }
            }
        }.start();
    }

    private void initDatas() {
        new Thread() {
            @Override
            public void run() {
                //通过MAC地址获取数据
                String sql = "exec fm_get_kanban_by_mac_address ?";
                Parameters parameters = new Parameters().add(1, InformationUtils.getInformationUtils(ReportHtmlActivity.this).getMacAddress());
                ArrayList<SQLBean> sqlBeans = SQLConnect.connectSQL(sql, parameters);
                if (sqlBeans.size() > 0) {
                    Message message = new Message();
                    message.arg1 = LOADREPORTBYMAC;
                    message.obj = sqlBeans;
                    handler.sendMessage(message);
                } else {
                    Log.e("len", "NULLNULL");
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
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
    }

    @Override
    public void requestPermissionsFail() {

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
            handler.proceed();
        }
    }

    private void loadReport() {
        webView.loadUrl(url);
        Log.e("len", url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new ReportWebClient());
        handlerRunnable = new Handler();
    }
}
