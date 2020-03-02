package com.megmeet.megmeetreport.net;

import com.megmeet.megmeetreport.bean.VersionBean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface PersonalProtocol {

    @GET("mes/report/report.json")
    Call<VersionBean> getVersionInfo();

    @GET("mes/report/report.apk")
    Call<ResponseBody> getDownFile();


    @GET("mes/kanban/kanban.json")
    Call<VersionBean> getLocationVersionInfo();

    @GET("mes/kanban/kanban.apk")
    Call<ResponseBody> getLocationDownFile();
}