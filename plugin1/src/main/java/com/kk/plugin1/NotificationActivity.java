package com.kk.plugin1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {
    NotificationHandler nHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nHandler = NotificationHandler.getInstance(this);
        initUI();
    }


    private void initUI() {
        setContentView(R.layout.activity_notification);
        findViewById(R.id.simple_notification).setOnClickListener(this);
        findViewById(R.id.big_notification).setOnClickListener(this);
        findViewById(R.id.progress_notification).setOnClickListener(this);
        findViewById(R.id.button_notifcation).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.simple_notification:
                nHandler.createSimpleNotification(this);
                break;

            case R.id.big_notification:
                nHandler.createExpandableNotification(this);
                break;

            case R.id.progress_notification:
                nHandler.createProgressNotification(this);
                break;

            case R.id.button_notifcation:
                nHandler.createButtonNotification(this);
        }

    }
}
