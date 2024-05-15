package com.xiaomi.mainapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class RecommendFragment extends Fragment {
    private static final String TAG = "RecommendFragment";
    private View view;
    private RecyclerView recyclerView;
    private List<Post> postArrayList = new ArrayList<>();
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommend, container, false);

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView); // 将'recyclerView'替换为你的视图ID
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 实例化postAdapter并传递postArrayList
        postAdapter = new PostAdapter(requireContext(), postArrayList);
        recyclerView.setAdapter(postAdapter);

        // 初始化SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading) {
                    currentPage = 1;
                    hasMoreData = true;
                    loadPostData();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // 加载初始的帖子数据
        loadPostData();

        // 其他逻辑...

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        recyclerView = null;
        postAdapter = null;
    }

    private void loadPostData() {
        if (!hasMoreData) {
            Toast.makeText(requireContext(), "没有更多内容", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoading = true;

        Thread thread = new Thread(new NetworkRequestRunnable());
        thread.start();
    }

    private class NetworkRequestRunnable implements Runnable {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
// 创建一个List用于存储所有的response
            List<Response> responses = new ArrayList<>();

            for (int i = 1; i <= 2; i++) {
                HttpUrl.Builder urlBuilder = HttpUrl.parse("https://hotfix-service-prod.g.mi.com/weibo/homePage").newBuilder();
                urlBuilder.addQueryParameter("size", "10");
                // 设置current的值
                urlBuilder.addQueryParameter("current", String.valueOf(i));
                String url = urlBuilder.build().toString();

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .build();

                try {
                    // 执行请求并获取response
                    Response response = client.newCall(request).execute();
                    // 将response添加到List中
                    responses.add(response);
                    // 处理响应...
                } catch (IOException e) {
                    e.printStackTrace();
                    // 处理异常...
                }
            }

            for (Response response : responses){
                try {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        List<Post> fetchedPosts = parsePosts(responseData);
                        Log.d(TAG, "responseData: " + responseData);
                        Log.d(TAG, "fetchedPosts:" + fetchedPosts);

                        // Update RecyclerView on the main thread
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (currentPage == 1) {
                                    postArrayList.clear();
                                }

                                postArrayList.addAll(fetchedPosts);
                                Log.d(TAG, "ArrList" + fetchedPosts);
                                postAdapter.notifyDataSetChanged();

                                if (currentPage == 1) {
                                    recyclerView.scrollToPosition(0);
                                }

                                currentPage++;

                                // 模拟随机打乱数据顺序
                                Collections.shuffle(postArrayList);

                                isLoading = false;
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isLoading = false;
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    private List<Post> parsePosts(String responseData) {
        List<Post> parsedPosts = new ArrayList<>();

        try {
            JSONObject responseJson = new JSONObject(responseData);
            int code = responseJson.getInt("code");
            if (code == 200) {
                JSONObject data = responseJson.getJSONObject("data");
                JSONArray records = data.getJSONArray("records");

                for (int i = 0; i < records.length(); i++) {
                    JSONObject postJson = records.getJSONObject(i);
                    Long id = postJson.getLong("id");
                    Long userId = postJson.getLong("userId");
                    String username = postJson.getString("username");
                    String phone = postJson.getString("phone");
                    String avatar = postJson.getString("avatar");
                    String title = postJson.getString("title");
                    String videoUrl = postJson.optString("videoUrl");
                    String poster = postJson.optString("poster");
                    List<String> images;
                    if (!postJson.isNull("images")) {
                        JSONArray imagesJson = postJson.getJSONArray("images");
                        images = new ArrayList<>();
                        for (int j = 0; j < imagesJson.length(); j++) {
                            images.add(imagesJson.getString(j));
                        }
                    } else {
                        images = new ArrayList<>();
                    }
                    int likeCount = postJson.getInt("likeCount");
                    boolean likeFlag = postJson.getBoolean("likeFlag");
                    String createTime = postJson.getString("createTime");

                    Post post = new Post(id, userId, username, phone, avatar, title, videoUrl, poster, images, likeCount, likeFlag, createTime);
                    parsedPosts.add(post);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return parsedPosts;
    }
}