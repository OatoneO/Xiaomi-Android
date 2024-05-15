package com.xiaomi.mainapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Post implements Parcelable {
    private Long id;
    private Long userid;
    private String username;
    private String phone;
    private String avatar;
    private String title;
    private String videoUrl;
    private String poster;
    private List<String> images;
    private int likecount;
    private boolean likeflag;
    private String createtime;

    public Post(Long id, Long userId, String username, String phone, String avatar, String title,
                String videoUrl, String poster, List<String> images,
                int likeCount, boolean likeFlag, String createTime) {
        this.id = id;
        this.userid = userId;
        this.username = username;
        this.phone = phone;
        this.avatar = avatar;
        this.title = title;
        this.videoUrl = videoUrl;
        this.poster = poster;
        this.images = images;
        this.likecount = likeCount;
        this.likeflag = likeFlag;
        this.createtime = createTime;
    }

    // Getters and setters

    // Parcelable 接口的方法实现
    protected Post(Parcel in) {
        id = in.readLong();
        userid = in.readLong();
        username = in.readString();
        phone = in.readString();
        avatar = in.readString();
        title = in.readString();
        videoUrl = in.readString();
        poster = in.readString();
        images = in.createStringArrayList();
        likecount = in.readInt();
        likeflag = in.readByte() != 0;
        createtime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(userid);
        dest.writeString(username);
        dest.writeString(phone);
        dest.writeString(avatar);
        dest.writeString(title);
        dest.writeString(videoUrl);
        dest.writeString(poster);
        dest.writeStringList(images);
        dest.writeInt(likecount);
        dest.writeByte((byte) (likeflag ? 1 : 0));
        dest.writeString(createtime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    // Getters and setters

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    public String getCoverUrl() {
        return poster;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
    public int getLikeCount() {return likecount;}
    public boolean isLiked() {return likeflag;}
    public String getImageUrl(int index) {
        if (images != null && index >= 0 && index < images.size()) {
            return images.get(index);
        } else {
            return null;
        }
    }


}
