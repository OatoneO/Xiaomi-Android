package com.xiaomi.mainapplication;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.startActivity;

import static java.security.AccessController.getContext;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    private Context context;
    public interface OnPostItemClickListener {
        void onPostDelete(Post post);
    }

    private static final String TAG = "PostAdapter";

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);

        ImageView ivWideImage = view.findViewById(R.id.ivWideImage);
        ImageView ivTallImage = view.findViewById(R.id.ivTallImage);
        LinearLayout gridViewImages = view.findViewById(R.id.gridViewImages);

        return new ViewHolder(view, ivWideImage, ivTallImage, gridViewImages);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = postList.get(position);


        Glide.with(holder.avatarImageView.getContext())
                .load(post.getAvatar())
                .circleCrop()  // 这将使图像变为圆形
                .into(holder.avatarImageView);
        holder.usernameTextView.setText(post.getUsername());
        holder.titleTextView.setText(post.getTitle());


        holder.deleteButton.setOnClickListener(v -> {

            deletePost(position);
        });

        holder.commentButton.setOnClickListener(v -> {
            showToast("点击第" + (position + 1) + "条数据评论按钮");
        });

        String videoUrl = null;
        videoUrl = post.getVideoUrl();
        Log.d(TAG, "videoUrl: " + videoUrl);
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        if (videoUrl.startsWith("https")) {
            holder.videoView.setVisibility(View.GONE);
            holder.coverView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.textView.setVisibility(View.GONE);
            Log.d(TAG, "getVideoUrl");

            String coverUrl = post.getCoverUrl();
// 使用Glide库加载封面图片
            Glide.with(context).load(coverUrl).into(holder.coverView);
            mediaPlayer[0] = new MediaPlayer();

            VideoPlayer videoPlayer = new VideoPlayer(holder.videoView);

// 检查 MediaPlayer 是否已经被释放
            if (videoPlayer.isPlaying()) {
                videoPlayer.stopVideo();
                Log.d(TAG, "VideoPlayer已停止");
            }

// 设置视频源
//            videoPlayer.playVideo(videoUrl);
            Log.d(TAG, "设置视频源");
            // 在视频开始播放时设置videoView为可见，coverView为不可见
            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    holder.videoView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "setVisibility");
                    holder.coverView.setVisibility(View.GONE);
                }
            });

// 设置点击监听
            holder.videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (videoPlayer.isPlaying()) {
                        videoPlayer.pauseVideo();
                        Log.d(TAG, "VideoPlayer已暂停");
                    } else {
                        videoPlayer.resumeVideo();
                        Log.d(TAG, "VideoPlayer已开始播放");
                    }
                }
            });

            // 设置循环播放
            videoPlayer.setLooping(true);

        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.coverView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.GONE);
            Log.d(TAG, "getImages");

            List<String> images = post.getImages(); // 获取图片列表
            int imageCount = images.size();
            Log.d(TAG, "imageCount: " + imageCount);

            if (imageCount == 1) {
                // 单张图片，显示大图样式
                holder.ivWideImage.setVisibility(View.VISIBLE);
                holder.ivTallImage.setVisibility(View.GONE);
                holder.gridViewImages.setVisibility(View.GONE);

                String imageUrl = images.get(0); // 获取第一张图片的URL
                Log.d(TAG, "imageUrl: " + imageUrl);

                Glide.with(holder.itemView.getContext())
                        .asBitmap()
                        .load(imageUrl)
                        .apply(new RequestOptions().centerCrop())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // 获取图片的宽高
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                Log.d(TAG, "image width: " + width);
                                Log.d(TAG, "image height: " + height);

                                // 根据宽高比例设置ImageView的宽高
                                if (width > height) {
                                    // 横图样式
                                    // 设置图片到ImageView中
                                    holder.ivWideImage.setImageBitmap(resource);
                                } else {
                                    // 竖图样式
                                    holder.ivWideImage.setVisibility(View.GONE);
                                    holder.ivTallImage.setVisibility(View.VISIBLE);
                                    holder.gridViewImages.setVisibility(View.GONE);
                                    // 设置图片到ImageView中
                                    holder.ivTallImage.setImageBitmap(resource);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // 图片加载被清除时的回调方法
                            }
                        });
            } else if (imageCount > 1) {
                // 多张图片，显示九宫格样式
                holder.ivWideImage.setVisibility(View.GONE);
                holder.ivTallImage.setVisibility(View.GONE);
                holder.gridViewImages.setVisibility(View.VISIBLE);

                if (imageCount > 0) {
                    LinearLayout gridViewImages = holder.gridViewImages;
                    gridViewImages.removeAllViews(); // 清除可能已存在的 ImageView

                    int columnCount = 3; // 每行显示的列数
                    int rowCount = (int) Math.ceil((double) imageCount / columnCount); // 计算行数

                    int index = 0;
                    for (int i = 0; i < rowCount; i++) {
                        LinearLayout rowLayout = new LinearLayout(context);
                        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        for (int j = 0; j < columnCount; j++) {
                            if (index >= imageCount) {
                                // 添加空白占位符
                                ImageView placeholderImageView = new ImageView(context);
                                placeholderImageView.setLayoutParams(new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                ));
                                rowLayout.addView(placeholderImageView);
                            } else {
                                ImageView imageView = new ImageView(context);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                ));
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imageView.setAdjustViewBounds(true);
                                // 使用 Glide 加载图片
                                String imageUrl = images.get(index);
                                Log.d(TAG, "imageUrl at index " + index + ": " + imageUrl);
                                Glide.with(context)
                                        .load(imageUrl)
                                        .into(imageView);

                                imageView.setTag(index); // 将当前图片的位置保存为 imageView 对象的一个属性
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int location = (Integer) v.getTag(); // 在点击事件发生时获取图片的位置
                                        showToast("我是第" + (location + 1) + "张图片，当前帖子总图片数量为" + imageCount + "张");
                                        // 创建 Intent 对象，指定启动大图浏览页面（ImagePreviewActivity）
                                        Intent intent = new Intent(context, ImagePreviewActivity.class);
                                        intent.putExtra("POST_LIST", new ArrayList<>(postList)); // 将 postList 作为 Extra 数据传递给 Intent 对象
                                        intent.putExtra("POSITION", position);//图片在帖子中的位置
                                        intent.putExtra("LOCATION", location);//帖子在帖子列表中的位置
                                        intent.putExtra("IMAGE_COUNT", imageCount); // 将图片总数量作为 Extra 数据传递给 Intent 对象

                                        // 启动大图浏览页面
                                        context.startActivity(intent);
                                        Log.d(TAG, "Start ImagePreviewActivity");
                                        Log.d(TAG, "Selected image location: " + location);
                                        Log.d(TAG, "Image count: " + imageCount);
                                    }
                                });
                                rowLayout.addView(imageView);
                            }
                            index++;
                        }

                        gridViewImages.addView(rowLayout);
                    }
                }
            }
        }
        int likeCount = post.getLikeCount();
        boolean liked = post.isLiked();

        if (liked) {
            holder.postLike.setBackgroundResource(R.drawable.post_like_up); // 设置点赞按钮的图片为点赞状态的图片资源
            holder.postLike.setText(likeCount); // 设置点赞按钮的文本为likeCount
        } else {
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


    public void deletePost(int position) {
        // Delete the post at the given position
        postList.remove(position);
        notifyItemRemoved(position);
    }

    private void showToast(String message) {
        // Show a toast message
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView usernameTextView;
        ImageButton deleteButton;
        TextView titleTextView;
        VideoView videoView;
        ImageView coverView;
        LinearLayout imageView;
        ImageView ivWideImage;
        ImageView ivTallImage;
        LinearLayout gridViewImages;
        TextView commentsTextView;
        TextView likesTextView;
        Button commentButton;
        TextView textView;
        Button postLike;



        public ViewHolder(@NonNull View itemView, ImageView ivWideImage, ImageView ivTallImage, LinearLayout gridViewImages) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avater);
            usernameTextView = itemView.findViewById(R.id.user_name);
            titleTextView = itemView.findViewById(R.id.post_title);
            videoView = itemView.findViewById(R.id.post_video);
            coverView = itemView.findViewById(R.id.post_video_cover);
            imageView = itemView.findViewById(R.id.post_image);
            textView = itemView.findViewById(R.id.post_text);
            deleteButton = itemView.findViewById(R.id.post_delete);
            commentButton = itemView.findViewById(R.id.post_discuss);
            postLike = itemView.findViewById(R.id.post_like);
            this.ivWideImage = ivWideImage;
            this.ivTallImage = ivTallImage;
            this.gridViewImages = gridViewImages;
            boolean liked = false;
            // 点赞按钮点击事件处理
            postLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLoggedIn(v.getContext())) {
                        // 用户已登录，执行操作
                        performAction(getAdapterPosition());
                    } else {
                        // 用户未登录，跳转到登录页面
                        redirectToLoginPage(v.getContext());
                    }
                }

                private boolean isLoggedIn(Context context) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null);
                    return token != null; // 如果令牌不为空，则表示用户已登录；否则表示用户未登录
                }
                private void performAction(int position) {

                    if (liked) {
                        // 已经点赞，执行取消点赞操作
                        performUnlikeAction(getAdapterPosition());
                    } else {
                        // 未点赞，执行点赞操作
                        performLikeAction(getAdapterPosition());
                    }
                }
                private void performLikeAction(int position) {
                    // 执行点赞操作，并根据请求结果更新本地数据状态和UI

                    // 创建 OkHttpClient 实例
                    OkHttpClient client = new OkHttpClient();

                    SharedPreferences sharedPreferences = avatarImageView.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null);

                    // 构建请求体
                    MediaType mediaType = MediaType.parse("application/json");
                    JSONObject requestBody = new JSONObject();
                    try {
                        requestBody.put("position", position);
                    } catch (JSONException e) {
                        // 处理JSONException异常
                        e.printStackTrace();
                    }
                    RequestBody body = RequestBody.create(mediaType, requestBody.toString());

                    // 构建请求
                    Request request = new Request.Builder()
                            .url("https://hotfix-service-prod.g.mi.com/weibo/like/up")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Bearer " + token) // 添加Bearer Token
                            .post(body)
                            .build();

                    // 发送异步请求并处理响应
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // 网络请求失败，处理错误情况
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                // 网络请求成功的处理逻辑
                                boolean isSuccess = true; // 根据实际响应结果进行判断
                                if (isSuccess) {
                                    // 更新本地数据状态为已点赞
                                    updateLikeStatus(position, true);
                                    // 执行点赞动画
                                    performLikeAnimation();
                                    // 更新UI
                                    updateLikeUI(position);
                                } else {
                                    // 处理点赞操作失败的情况
                                }
                            } else {
                                // 网络请求失败，处理错误情况
                            }
                        }
                    });
                }

                private void performUnlikeAction(int position) {
                    // 执行取消点赞操作，并根据请求结果更新本地数据状态和UI

                    // 创建 OkHttpClient 实例
                    OkHttpClient client = new OkHttpClient();

                    SharedPreferences sharedPreferences = avatarImageView.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null);


                    // 构建请求体
                    MediaType mediaType = MediaType.parse("application/json");
                    JSONObject requestBody = new JSONObject();
                    try {
                        requestBody.put("position", position);
                    } catch (JSONException e) {
                        // 处理JSONException异常
                        e.printStackTrace();
                    }
                    RequestBody body = RequestBody.create(mediaType, requestBody.toString());

                    // 构建请求
                    Request request = new Request.Builder()
                            .url("https://hotfix-service-prod.g.mi.com/weibo/like/down")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Bearer " + token) // 添加Bearer Token
                            .post(body)
                            .build();

                    // 发送异步请求并处理响应
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // 网络请求失败，处理错误情况
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                // 网络请求成功的处理逻辑
                                boolean isSuccess = true; // 根据实际响应结果进行判断
                                if (isSuccess) {
                                    // 更新本地数据状态为未点赞
                                    updateLikeStatus(position, false);
                                    // 执行取消点赞动画
                                    performUnlikeAnimation();
                                    // 更新UI
                                    updateUnLikeUI(position);
                                } else {
                                    // 处理取消点赞操作失败的情况
                                }
                            } else {
                                // 网络请求失败，处理错误情况
                            }
                        }
                    });
                }

                private void redirectToLoginPage(Context context) {
                    // 跳转到登录页面

                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                }
                private void updateLikeStatus(int position, boolean isLiked) {
                    SharedPreferences sharedPreferences = avatarImageView.getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("liked_" + position, isLiked);
                    editor.apply();
                }

                private void performLikeAnimation(){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
// 定义缩放动画：从1.0倍放大到1.2倍，然后回到1.0倍
                            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                                    postLike,
                                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.2f, 1.0f),
                                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.2f, 1.0f)
                            );
                            scaleAnimator.setDuration(1000); // 动画持续时间为1000毫秒

                            // 定义旋转动画：沿Y轴旋转360度
                            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(postLike, View.ROTATION_Y, 0f, 360f);
                            rotateAnimator.setDuration(1000); // 动画持续时间为1000毫秒

                            // 创建动画集合，同时执行缩放动画和旋转动画
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.playTogether(scaleAnimator, rotateAnimator);
                            animatorSet.start();
                        }
                    });

                }
                private void updateLikeUI(int position) {
                    // 更新与特定位置相关的UI元素
                    List<Post> postList = new ArrayList<>();
                    if (postList != null && !postList.isEmpty() && position < postList.size()) {
                        // 更新与特定位置相关的UI元素
                        Post post = postList.get(position);
                        int likeCount = post.getLikeCount();
                        postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.post_like_up, 0, 0, 0);
                        postLike.setText(String.valueOf(likeCount));
                    }
                }
                private void performUnlikeAnimation() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 定义缩放动画：从1.0倍缩小到0.8倍，然后回到1.0倍
                            ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                                    postLike,
                                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.8f, 1.0f),
                                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.8f, 1.0f)
                            );
                            scaleAnimator.setDuration(1000); // 动画持续时间为1000毫秒

                            // 执行动画
                            scaleAnimator.start();
                        }
                    });

                }
                private void updateUnLikeUI(int position) {
                    // 更新与特定位置相关的UI元素
                    List<Post> postList = new ArrayList<>();
                    if (postList != null && !postList.isEmpty() && position < postList.size()) {
                        // 更新与特定位置相关的UI元素
                        Post post = postList.get(position);
                        int likeCount = post.getLikeCount();
                        postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.post_like, 0, 0, 0);
                        postLike.setText("点赞");
                    }

                }
            });
        }
    }
}