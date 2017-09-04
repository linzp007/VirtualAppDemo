package io.virtualapp.lite.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;

import java.io.File;

import io.virtualapp.bridge.Config;
import io.virtualapp.bridge.IClient;
import io.virtualapp.lite.App;
import io.virtualapp.lite.R;
import io.virtualapp.lite.model.AppTarget;
import io.virtualapp.lite.utils.VUiKit;

public class MainActivity extends Activity implements View.OnClickListener {
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mClient = IClient.Stub.asInterface(service);
            try {
                Log.i("server", "onServiceConnected:" + mClient.login("hello", "123456"));
            } catch (RemoteException e) {
                Log.e("server", "onServiceConnected:", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mClient = null;
        }
    };
    private IClient mClient;
    private Button mBtnService, mBtnOpen, mBtnInstall, mBtnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.ACTION_PACKAGE_ADD);
        intentFilter.addAction(App.ACTION_PACKAGE_UPDATE);
        intentFilter.addAction(App.ACTION_PACKAGE_REMOVE);
        intentFilter.addDataScheme("package");
        registerReceiver(mBroadcastReceiver, intentFilter);
        mBtnService = (Button) findViewById(R.id.btn_service);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnInstall = (Button) findViewById(R.id.btn_install);
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mBtnService.setOnClickListener(this);
        mBtnInstall.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        Log.d("server", "test log");
        AppTarget appTarget = AppTarget.get(this);
        if (VirtualCore.get().isAppInstalled(appTarget.getPackageName())) {
            updateButton(true);
        } else {
            appTarget.update();
            updateButton(false);
        }
    }

    private void uninstallApp(AppTarget appTarget) {
        if (VirtualCore.get().uninstallPackage(appTarget.getPackageName())) {
            Toast.makeText(this, "卸载成功", Toast.LENGTH_SHORT).show();
            AppTarget.get(this).onUninstall();
        } else {
            Toast.makeText(this, "卸载失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void installApp(AppTarget appTarget) {
        ProgressDialog dialog = ProgressDialog.show(this, null, "下载中");
        File file = new File(appTarget.getInstallPackagePath());
        VUiKit.defer().when(() -> {
            if (!file.exists()) {
                //TODO 下载
                Log.d("kk", "下载未实现，apk不存在：" + file.getAbsolutePath());
                return null;
            }
            //对比版本InstallStrategy.COMPARE_VERSION
            return VirtualCore.get().installPackage(file.getAbsolutePath(), InstallStrategy.COMPARE_VERSION);
        }).done((rs) -> {
            dialog.dismiss();
            if (rs == null) {
                Toast.makeText(this, "下载失败\n" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } else if (rs.isSuccess) {
                appTarget.onInstall();
                Toast.makeText(this, "安装成功", Toast.LENGTH_SHORT).show();
                Log.i("kk", "安装成功，开始启动");
            } else {
                Log.i("kk", "安装失败");
                Toast.makeText(this, "安装失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lunchApp(AppTarget appTarget) {
        VUiKit.defer().when(() -> {
            if (!appTarget.isInstallBySystemApp()) {
                try {
                    VirtualCore.get().preOpt(appTarget.getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).done((open) -> {
            //va支持多开
            int userId = 0;
            Intent intent = VirtualCore.get().getLaunchIntent(appTarget.getPackageName(), userId);
            if (intent == null) {
                Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "启动应用", Toast.LENGTH_SHORT).show();
                VActivityManager.get().startActivity(intent, userId);
            }
        });
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (App.ACTION_PACKAGE_ADD.equals(intent.getAction())
                    || App.ACTION_PACKAGE_UPDATE.equals(intent.getAction())) {
                Toast.makeText(context, "应用安装/更新", Toast.LENGTH_SHORT).show();
                String packageName = intent.getData().getSchemeSpecificPart();
                updateButton(true);
            } else if (App.ACTION_PACKAGE_REMOVE.equals(intent.getAction())) {
                Toast.makeText(context, "应用卸载", Toast.LENGTH_SHORT).show();
                String packageName = intent.getData().getSchemeSpecificPart();
                updateButton(false);
                VActivityManager.get().unbindService(mServiceConnection);
                mClient = null;
            }
        }
    };

    private void updateButton(boolean install) {
        mBtnOpen.setEnabled(install);
        mBtnInstall.setEnabled(!install);
        mBtnDelete.setEnabled(install);
        mBtnService.setEnabled(install);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        if(mClient != null) {
            VActivityManager.get().unbindService(mServiceConnection);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_service:
                VActivityManager.get().bindService(this,
                        new Intent(Config.ACTION_BIND_CLIENT)
                                .setPackage(Config.CLIENT_PKG),
                        mServiceConnection, BIND_AUTO_CREATE);
                break;
            case R.id.btn_open:
                lunchApp(AppTarget.get(this));
                break;
            case R.id.btn_install:
                installApp(AppTarget.get(this));
                break;
            case R.id.btn_delete:
                uninstallApp(AppTarget.get(this));
                break;
        }
    }
}
