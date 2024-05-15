package com.xiaomi.mainapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";
    private EditText etPhoneNumber;
    private EditText etVerificationCode;
    private TextView tvGetVerificationCode;
    private Button btnLogin;
    private CountDownTimer countDownTimer;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhoneNumber = findViewById(R.id.et_phone_number);
        etVerificationCode = findViewById(R.id.et_verification_code);
        tvGetVerificationCode = findViewById(R.id.tv_get_verification_code);
        btnLogin = findViewById(R.id.login_btn);
        TextView lgoin_back = findViewById(R.id.lgoin_back);
        lgoin_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MyFragment.class);
                startActivity(intent);
            }
        });

        tvGetVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhoneNumberComplete()) {
                    tvGetVerificationCode.setEnabled(false);
                    startCountDownTimer();
                    // 实例化 VerificationCodeLogin 类
                    VerificationCodeLogin verificationCodeLogin = new VerificationCodeLogin();
                    String phoneNumber = etPhoneNumber.getText().toString().trim(); // 获取手机号
                    verificationCodeLogin.getVerificationCode(phoneNumber);
                } else {
                    Toast.makeText(LoginActivity.this, "请输⼊完整⼿机号", Toast.LENGTH_LONG).show();

                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhoneNumberComplete() && isVerificationCodeComplete()) {
                    if (isPrivacyAgreed()) {
                        String phoneNumber = etPhoneNumber.getText().toString().trim();
                        String verificationCode = etVerificationCode.getText().toString().trim();
                        loginWithVerificationCode(phoneNumber, verificationCode);

                    } else {
                        Toast.makeText(LoginActivity.this, "先同意隐私协议才可以登录哟~", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "请输⼊完整⼿机号和验证码", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isPhoneNumberComplete() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        return phoneNumber.length() == 11;
    }

    private boolean isVerificationCodeComplete() {
        String verificationCode = etVerificationCode.getText().toString().trim();
        return verificationCode.length() == 6;
    }

    private boolean isPrivacyAgreed() {
        return true;
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvGetVerificationCode.setText(String.format(Locale.getDefault(), "%ds 后重新获取", seconds));
            }

            @Override
            public void onFinish() {
                tvGetVerificationCode.setEnabled(true);
                tvGetVerificationCode.setText("获取验证码");
            }
        }.start();
    }
    private void sendCodeRequest(String phone) {
        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{\"phone\":\"" + phone + "\"}";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url("https://hotfix-service-prod.g.mi.com/weibo/api/auth/sendCode")
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "获取验证码错误", Toast.LENGTH_SHORT).show();
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            System.out.println("请求成功：" + responseBody);
                        } else {
                            System.out.println("请求失败：" + response.code());
                        }
                    }
                });
            }
        });
        // 启动线程
        thread.start();
    }
    public void loginWithVerificationCode(String phoneNumber, String verificationCode) {
        OkHttpClient client = new OkHttpClient();
        // 构造请求体
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("phone", phoneNumber);
            jsonBody.put("smsCode", verificationCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 创建一个新的线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // 发送登录请求
                String loginEndpoint = "https://hotfix-service-prod.g.mi.com/weibo/api/auth/login";
                RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(loginEndpoint)
                        .post(requestBody)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                int code = jsonResponse.getInt("code");
                                Log.d(TAG, "code:" + code);
                                if (code == 200) {
                                    String token = jsonResponse.getString("data");
                                    Log.d(TAG, "gettoken" + token);
                                  saveTokenLocally(token);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 在主线程上保存token
                                            saveTokenLocally(token);
                                        }
                                    });
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
                                            MyFragment myFragment = new MyFragment();
                                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                            transaction.replace(R.id.container, myFragment);
                                            transaction.commit();
                                        }
                                    });
                                } else {
                                    String msg = jsonResponse.getString("msg");
                                    Log.d(TAG, "Failure" + msg);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginActivity.this, "验证码错误", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "Failure" + response.code());
                        }
                    }

                });
            }
        });
        // 启动线程
        thread.start();
    }
    private void saveTokenLocally(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        Log.d(TAG, "saveTokenLocally: " + token);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}