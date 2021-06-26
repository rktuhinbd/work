package com.app.messagealarm.ui.terms_privacy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.messagealarm.R;
import com.app.messagealarm.ui.main.alarm_applications.AlarmApplicationActivity;
import com.google.android.material.button.MaterialButton;

public class TermsPrivacyActivity extends AppCompatActivity {

    MaterialButton btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_privacy);
        btnAgree = findViewById(R.id.btn_agree);
        btnAgree.setOnClickListener(v -> {
            startActivity(new Intent(this, AlarmApplicationActivity.class));
            finish();
        });
    }
}