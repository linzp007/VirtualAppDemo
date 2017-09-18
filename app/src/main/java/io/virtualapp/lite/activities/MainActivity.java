package io.virtualapp.lite.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            Log.i("server", "onServiceDisconnected");
            mClient = null;
        }
    };
    private IClient mClient;
    private TextView mVerView, mLogView;
    private Button mBtnService, mBtnOpen, mBtnOpen2, mBtnOpen3, mBtnInstall, mBtnDelete, mBtnUpdate, mBtnTestContacts;

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
        mVerView = (TextView) findViewById(R.id.tv_ver);
        mBtnService = (Button) findViewById(R.id.btn_service);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnInstall = (Button) findViewById(R.id.btn_install);
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        mLogView = (TextView) findViewById(R.id.log);
        mBtnTestContacts = (Button) findViewById(R.id.btn_test_contacts);
        mBtnOpen2 = (Button) findViewById(R.id.btn_open2);
        mBtnOpen3 = (Button) findViewById(R.id.btn_open3);
        mBtnService.setOnClickListener(this);
        mBtnInstall.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);
        mBtnTestContacts.setOnClickListener(this);
        mBtnOpen2.setOnClickListener(this);
        mBtnOpen3.setOnClickListener(this);
        AppTarget appTarget = AppTarget.get(this);
        if (VirtualCore.get().isAppInstalled(appTarget.getPackageName())) {
            updateButton(true);
        } else {
            appTarget.update();
            updateButton(false);
        }
    }

    private void log(final String text) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    log(text);
                }
            });
            return;
        }
        mLogView.append(text + "\n");
    }

    private void uninstallApp(AppTarget appTarget) {
        VirtualCore.get().uninstallPackage(App.CONTACT_PROVIDER);
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
            int flags = InstallStrategy.COMPARE_VERSION;
            if (appTarget.isInstallBySystemApp()) {
                flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
            }
            return VirtualCore.get().installPackage(file.getAbsolutePath(), flags);
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
        lunchApp(appTarget, 0);
    }

    private void lunchApp(AppTarget appTarget, int userIndex) {
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
            int userId;
            if (userIndex <= 0) {
                userId = 0;
            } else {
                List<VUserInfo> vUserInfos = VUserManager.get().getUsers();
                Collections.sort(vUserInfos, new Comparator<VUserInfo>() {
                    @Override
                    public int compare(VUserInfo o1, VUserInfo o2) {
                        return o1.id - o2.id;
                    }
                });
                if (userIndex <= vUserInfos.size()) {
                    userId = vUserInfos.get(userIndex - 1).id;
                } else {
                    Toast.makeText(this, "启动失败:用户不存在", Toast.LENGTH_SHORT).show();
                    log("找不到用户：" + vUserInfos);
                    return;
                }
            }
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
                VActivityManager.get().unbindService(MainActivity.this, mServiceConnection);
                mClient = null;
            }
        }
    };

    private void updateButton(boolean install) {
        mBtnOpen.setEnabled(install);
        mBtnInstall.setEnabled(!install);
        mBtnDelete.setEnabled(install);
        mBtnService.setEnabled(install);
        mBtnUpdate.setEnabled(install);
        mBtnTestContacts.setEnabled(install);
        mBtnOpen2.setEnabled(install);
        mBtnOpen3.setEnabled(install);
        if (install) {
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(AppTarget.get(this).getPackageName(), 0);
            if (installedAppInfo == null) {
                mVerView.setText("获取安装信息失败");
            } else {
                PackageInfo packageInfo = installedAppInfo.getPackageInfo(0);
                if (packageInfo == null) {
                    mVerView.setText("获取包信息失败");
                } else {
                    mVerView.setText(packageInfo.versionName + "(" + packageInfo.versionCode + ")");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        if (mClient != null) {
            VActivityManager.get().unbindService(this, mServiceConnection);
        }
        super.onDestroy();
    }

    private long lasttime;
    private int lastId;

    @Override
    public void onClick(View v) {
        if (lastId == v.getId()) {
            if (System.currentTimeMillis() - lasttime < 1000) {
                return;
            }
        }
        lasttime = System.currentTimeMillis();
        lastId = v.getId();
        switch (v.getId()) {
            case R.id.btn_test_contacts:
                testContacts(AppTarget.get(this));
                break;
            case R.id.btn_service:
                if (mClient == null) {
                    VActivityManager.get().bindService(this,
                            new Intent(Config.ACTION_BIND_CLIENT)
                                    .setPackage(Config.CLIENT_PKG),
                            mServiceConnection, BIND_AUTO_CREATE);
                }
                break;
            case R.id.btn_open:
                lunchApp(AppTarget.get(this));
                break;
            case R.id.btn_open2:
                lunchApp(AppTarget.get(this), 2);
                break;
            case R.id.btn_open3:
                lunchApp(AppTarget.get(this), 3);
                break;
            case R.id.btn_install:
                installApp(AppTarget.get(this));
                break;
            case R.id.btn_delete:
                uninstallApp(AppTarget.get(this));
                break;
            case R.id.btn_update:
                File file = new File(AppTarget.get(this).getUpdateFile());
                if (!file.exists()) {
                    Toast.makeText(this, "sdcard的安装包不存在\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    return;
                }
                //停止运行
                VirtualCore.get().killApp(AppTarget.get(this).getPackageName(), VUserHandle.USER_ALL);
                VUiKit.defer().when(() -> {
                    return VirtualCore.get().installPackage(file.getAbsolutePath(), InstallStrategy.UPDATE_IF_EXIST);
                }).done(rs -> {
                    if (rs == null || !rs.isSuccess) {
                        Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void testContacts(AppTarget appTarget) {
        int count = VUserManager.get().getUserCount();
        if (count < 2) {
            VUserManager.get().createUser("user2", 0);
            log("创建用户2");
        }
        if (count < 3) {
            VUserManager.get().createUser("user3", 0);
            log("创建用户3");
        }
        if (VUserManager.get().getUserCount() < 3) {
            log("创建用户失败，无法继续测试");
            return;
        }
        if (!VirtualCore.get().isAppInstalled(App.CONTACT_PROVIDER)) {
            log("安装通讯录存储");
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(App.CONTACT_PROVIDER, 0);
                InstallResult result = VirtualCore.get().installPackage(packageInfo.applicationInfo.publicSourceDir,
                        InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                if (!result.isSuccess) {
                    log("安装通讯录存储失败");
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                log("没有找到通讯录存储");
                return;
            }
        } else {
            log("通讯录存储已经安装");
        }
        List<VUserInfo> vUserInfos = VUserManager.get().getUsers();
        for (VUserInfo vUserInfo : vUserInfos) {
            if (!VirtualCore.get().isAppInstalledAsUser(vUserInfo.id, appTarget.getPackageName())) {
                VirtualCore.get().installPackageAsUser(vUserInfo.id, appTarget.getPackageName());
                log(vUserInfo.id + ":应用安装完成");
            } else {
                log(vUserInfo.id + ":应用已经安装");
            }
            if (!VirtualCore.get().isAppInstalledAsUser(vUserInfo.id, App.CONTACT_PROVIDER)) {
                VirtualCore.get().installPackageAsUser(vUserInfo.id, App.CONTACT_PROVIDER);
                log(vUserInfo.id + ":通讯录存储安装完成");
            } else {
                log(vUserInfo.id + ":通讯录存储已经安装");
            }
        }
    }
}
