package com.megmeet.megmeetreport.util;

import android.content.Context;

import com.megmeet.megmeetreport.base.UrlBase;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {
    private static RetrofitUtil retrofitUtil;
    private static Context mContext;

    private RetrofitUtil() {

    }

    public static RetrofitUtil getDefaultInstance(Context context) {
        mContext = context;
        if (retrofitUtil == null) {
            retrofitUtil = new RetrofitUtil();
        }
        return retrofitUtil;
    }

    public Retrofit getRetrofit() {
        Retrofit build = new Retrofit.Builder().baseUrl(UrlBase.baseUrlIkahe)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return build;
    }
}
