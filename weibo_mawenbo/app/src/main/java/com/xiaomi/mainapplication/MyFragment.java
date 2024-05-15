package com.xiaomi.mainapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyFragment extends Fragment {
    private static final String TAG = "MyFragment";

    private ImageView profilePhoto;
    private TextView user_name;
    private TextView user_content;
    private TextView logout;
    private TextView fans_count;
    private View.OnClickListener profilePhotoClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);
        RelativeLayout containerLayout = rootView.findViewById(R.id.container);
        logout = rootView.findViewById(R.id.layout);
        user_name = rootView.findViewById(R.id.user_name);
        fans_count = rootView.findViewById(R.id.fans_count);
        profilePhoto = rootView.findViewById(R.id.profile_photo);
        user_content = rootView.findViewById(R.id.user_content);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        Log.d(TAG, "token: " + token);

        boolean shouldSendRequest = true;

        if (!token.isEmpty() && shouldSendRequest) {
            sendGetRequest("Bearer " + token);
            Log.d(TAG, "sendGetRequest");
            shouldSendRequest = false;
            logout.setVisibility(View.VISIBLE);
        } else {
            logout.setVisibility(View.INVISIBLE);
            profilePhotoClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            };
            profilePhoto.setOnClickListener(profilePhotoClickListener);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("token");
                editor.commit();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 将所有视图引用置为null
        profilePhoto = null;
        user_name = null;
        fans_count = null;
        user_content = null;
        logout = null;

        // 移除对profilePhoto的点击监听器
        if (profilePhoto != null && profilePhotoClickListener != null) {
            profilePhoto.setOnClickListener(null);
            profilePhotoClickListener = null;
        }
    }

    public void sendGetRequest(String authorization) {
        OkHttpClient client = new OkHttpClient();
        // 创建请求对象
        Request request = new Request.Builder()
                .url("https://hotfix-service-prod.g.mi.com/weibo/api/user/info")
                .addHeader("Authorization", authorization)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        int code = json.getInt("code");
                        Log.d(TAG, "code: " + code);
                        if (code == 200) {
                            JSONObject data = json.getJSONObject("data");
                            Log.d(TAG, "data: " + data);
                            final String username = data.getString("username");
                            Log.d(TAG, "username: " + username);
                            final String avatarUrl = data.getString("avatar");
                            Log.d(TAG, "avatarUrl: " + avatarUrl);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isAdded()) {
                                        // 更新头像和用户名
                                        Glide.with(requireContext()).load(avatarUrl).into(profilePhoto);
                                        user_name.setText(username);
                                        user_content.setText("你没有新的动态哦~");
                                        fans_count.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "请求失败: " + code);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "请求失败: " + response.code());
                }
            }
        });
    }
}