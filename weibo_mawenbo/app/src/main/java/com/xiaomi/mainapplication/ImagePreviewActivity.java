package com.xiaomi.mainapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    private static final String TAG = "ImagePreview";
    private ArrayList<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);


        setContentView(R.layout.activity_image_preview);

        View rootView = getWindow().getDecorView();

        // 设置点击事件
        rootView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                // 关闭当前活动并返回到前一个活动（返回首页）
                finish();
            }
        });

        TextView downloadTextView = findViewById(R.id.preview_header_download);
        downloadTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行下载操作
                downloadImage();
            }
        });
        // 获取传递的数据
        Intent intent = getIntent();
        int position = intent.getIntExtra("POSITION", 0);
        int location = intent.getIntExtra("LOCATION", 0);
        int imageCount = intent.getIntExtra("IMAGE_COUNT", 0);

        // 初始化 postList 或从其他地方获取数据
        postList = intent.getParcelableArrayListExtra("POST_LIST");
        if (postList == null || postList.isEmpty()) {
            // 处理 postList 为空的情况
            Log.e(TAG, "Error: Invalid postList");
            return;
        }

        if (postList != null && position >= 0 && position < postList.size()) {
            // 获取帖子数据
            Post post = postList.get(position);
            String avatarUrl = post.getAvatar();
            String username = post.getUsername();
            String imageUrl = post.getImageUrl(location);
            if (imageUrl != null) {
                ImageView imageView = findViewById(R.id.preview);
                Glide.with(this)
                        .load(imageUrl)
                        .into(imageView);
            } else {
                Log.e(TAG, "Error: Invalid image URL");
            }

            // 设置头像和昵称
            ImageView avatarImageView = findViewById(R.id.preview_header_avater);
            TextView nicknameTextView = findViewById(R.id.preview_header_user_name);
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()  // 这将使图像变为圆形
                    .into(avatarImageView);
            nicknameTextView.setText(username);

            // 设置当前页数/总图片数
            TextView pageInfoTextView = findViewById(R.id.preview_header_tv);
            String pageInfo = String.format("%d/%d", location + 1, imageCount);
            pageInfoTextView.setText(pageInfo);
            Log.d(TAG, "location: " + location);
            Log.d(TAG, "pageInfo: " + pageInfo);
            Log.d(TAG, "imageCount: " + imageCount);
        } else {
            // 处理 postList 为空或位置超出范围的情况
            Log.e(TAG, "Error: Invalid postList or location out of bounds");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = getWindow().getInsetsController();
                if (insetsController != null) {
                    insetsController.hide(WindowInsets.Type.navigationBars());
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    private void downloadImage() {
        // 获取当前显示的图像URL
        int position = getIntent().getIntExtra("POSITION", 0);
        int location = getIntent().getIntExtra("LOCATION", 0);
        Post post = postList.get(position);
        String imageUrl = post.getImageUrl(location);

        // 使用后台线程执行下载操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 执行图像下载
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // 保存图像到相册
                    String title = "Image_" + System.currentTimeMillis();
                    String description = "Image downloaded from ImagePreview";
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, title, description);

                    // 在主线程中显示下载完成的提示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("图片下载完成，请到相册查看");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void showToast(String message) {
        Toast.makeText(ImagePreviewActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}