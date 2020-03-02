package com.megmeet.megmeetreport.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2018/4/25.
 */

public class BaiduTTSUtil {
    private static String AppId = "16493320";
    private static String ApiKey = "jzxSwH0XW3wUnsavx5YOeg8n";
    private static String secretKey = "5QZ0LFU5t4fd9u39nro30IcXCFnwihOW";
    private static String destPath = Environment.getExternalStorageDirectory() + "/voice/";
    private static String speechSource = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
    private static String textSource = "bd_etts_text.dat";
    ;

    private SpeechSynthesizer mSpeechSynthesizer;
    private Context mContext;

    public BaiduTTSUtil(SpeechSynthesizer speechSynthesizer, Context context) {
        mSpeechSynthesizer = speechSynthesizer;
        mContext = context;
        File dir = new File(destPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        copyFromAssetsToSdcard(mContext, false, speechSource, destPath + speechSource);
        copyFromAssetsToSdcard(mContext, false, textSource, destPath + textSource);
    }

    public void initSpeech() {
        if (mSpeechSynthesizer == null) {
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        }
        mSpeechSynthesizer.setContext(mContext);

        int i2 = mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, destPath + textSource);

        int i3 = mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, destPath + speechSource);
        mSpeechSynthesizer.setAppId(AppId);
        mSpeechSynthesizer.setApiKey(ApiKey, secretKey);
        mSpeechSynthesizer.auth(TtsMode.MIX);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "2");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "3");
        int i1 = mSpeechSynthesizer.initTts(TtsMode.MIX);
        int i = mSpeechSynthesizer.loadModel(destPath + textSource, destPath + speechSource);
        if (i == 0 && i1 == 0) {
            Log.e("len", "baiduSuccess");
        }
    }


    private void copyFromAssetsToSdcard(Context context, boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("len", e.getMessage() + "11111");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("len", e.getMessage() + "22222");
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("len", e.getMessage() + "33333");
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("len", e.getMessage() + "44444");
                }
            }
        }
        File fil = new File(dest);
        if (fil.exists()) {
            Log.e("len", "TRUE:" + fil.getAbsolutePath());
        } else {

            Log.e("len", "FAIL:" + fil.getAbsolutePath());
        }

    }
}
