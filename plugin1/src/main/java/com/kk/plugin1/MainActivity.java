package com.kk.plugin1;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.github.tamir7.contacts.ContactData;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.QueryPlus;
import com.github.tamir7.contacts.Update;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    TextView mContactText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Contacts.initialize(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mContactText = (TextView) findViewById(R.id.tv_contacts);
        ((WebView) findViewById(R.id.webview)).loadUrl("http://ip.cn");
        findViewById(R.id.test_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestServiceActivity.class));
            }
        });
        findViewById(R.id.test_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
            }
        });
        findViewById(R.id.test_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imgFile = new File(Environment.getExternalStorageDirectory(),
                        "Download/IMG_" + System.currentTimeMillis() + ".jpg");
                if (!imgFile.getParentFile().exists()) {
                    imgFile.getParentFile().mkdirs();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));
                startActivityForResult(intent, 1111);
            }
        });
        try {
            QueryPlus query = new QueryPlus(this);
            final int count = query.count();
            printf("contacts count:" + count);
            if (count == 0) {
                Update update = new Update(this);
                update.insert(new ContactData("hello", "10086"));
            }
        } catch (Exception e) {
            printf("contacts:\n" + Log.getStackTraceString(e));
        }
    }

    private void printf(final String text) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printf(text);
                }
            });
            return;
        }
        mContactText.append(text + "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                //拍照功能
                case 1111:
                    startPhotoZoom(data.getData());
                    break;
            }
        }
    }

    private void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        //设置是否裁剪
        intent.putExtra("crop", "true");

        //设置宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        //设置裁剪图片的宽高：影响效果
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        intent.putExtra("output", uri);
        intent.putExtra("outputFormat", "JPEG");
        startActivityForResult(intent, 1112);
    }
}
