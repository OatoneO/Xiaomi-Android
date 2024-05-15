package com.xiaomi.mainapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "Home";
    private FrameLayout container;
    private BottomNavigationView bottomNavigationView;
    private Fragment recommendFragment;
    private Fragment myFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recommendFragment = new RecommendFragment();
        myFragment = new MyFragment();
        TextView textRecommend = findViewById(R.id.tv_recommend);
        TextView textMy = findViewById(R.id.tv_my);

        textRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(recommendFragment);
            }
        });

        textMy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(myFragment);
            }
        });
        switchFragment(recommendFragment);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}