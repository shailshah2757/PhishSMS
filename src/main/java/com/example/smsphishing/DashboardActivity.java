package com.example.smsphishing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public void fetch_sms(View view) {
        startActivity(new Intent(DashboardActivity.this, SMSFetchActivity.class));
    }

    public void url(View view) {
        startActivity(new Intent(DashboardActivity.this, UrlActivity.class));
    }
}