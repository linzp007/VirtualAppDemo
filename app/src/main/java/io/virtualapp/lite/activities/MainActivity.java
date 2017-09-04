package io.virtualapp.lite.activities;

import android.app.Activity;
import android.app.IServiceConnection;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;

import java.io.File;

import io.virtualapp.bridge.Config;
import io.virtualapp.bridge.IClient;
import io.virtualapp.bridge.IServer;
import io.virtualapp.lite.model.AppTarget;
import io.virtualapp.lite.utils.VUiKit;


public class MainActivity extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.BLUE);
        setContentView(linearLayout);
        AppTarget appTarget = AppTarget.get(this);
        if (VirtualCore.get().isAppInstalled(appTarget.getPackageName())) {
            Log.d("kk", "va已经安装该app，直接启动应用");
            lunchApp(appTarget);
        } else {
            Log.d("kk", "va未安装该app，开始下载app");
            downloadApp(appTarget);
        }
    }

    private void downloadApp(AppTarget appTarget) {
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
                lunchApp(appTarget);
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

                VActivityManager.get().bindService(this,
                        new Intent(Config.ACTION_BIND_CLIENT)
                                .setPackage(Config.CLIENT_PKG),
                        mServiceConnection, BIND_AUTO_CREATE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        VActivityManager.get().unbindService(mServiceConnection);
        super.onDestroy();
    }
}
