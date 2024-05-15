package com.xiaomi.mainapplication;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import android.util.Log;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class VerificationCodeLogin {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String ENDPOINT = "https://hotfix-service-prod.g.mi.com/weibo/api/auth/sendCode";
    private static final String TAG = "VerificationCodeLogin";

    public void getVerificationCode(final String phoneNumber) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                try {
                    JSONObject requestBodyJson = new JSONObject();
                    requestBodyJson.put("phone", phoneNumber);

                    RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, requestBodyJson.toString());

                    Request request = new Request.Builder()
                            .url(ENDPOINT)
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        // 请求成功
                        String responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);
                        int code = responseJson.getInt("code");
                        String msg = responseJson.getString("msg");
                        boolean data = responseJson.getBoolean("data");

                        if (code == 200 && data) {
                            // 成功获取验证码
                            Log.d(TAG, "验证码已发送");
                        } else {
                            // 获取验证码失败
                            Log.d(TAG, "获取验证码失败：" + msg);
                        }
                    } else {
                        // 请求失败
                        Log.d(TAG, "请求失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 异常处理
                    Log.d(TAG, "发生异常：" + e.getMessage());
                }
            }
        });

        // 启动线程
        thread.start();
    }
}
