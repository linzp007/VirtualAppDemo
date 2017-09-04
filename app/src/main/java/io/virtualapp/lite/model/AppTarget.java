package io.virtualapp.lite.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Environment;

import java.io.File;

import io.virtualapp.lite.utils.PackageUtils;

/**
 * TODO 如果系统存在目标app
 * TODO 自定义安装卸载广播
 * TODO app来源于系统安装的，如果更新
 * TODO 如果bindservice
 */
public class AppTarget {
    private static AppTarget sAppTarget;

    public static AppTarget get(Context context) {
        if (sAppTarget == null) {
            synchronized (AppTarget.class) {
                if (sAppTarget == null) {
                    sAppTarget = new AppTarget(context);
                }
            }
        }
        return sAppTarget;
    }

    private static final String PKG = "com.kk.plugin1";
    private Context mContext;
    private final SharedPreferences mSharedPreferences;
    private String mApkFile;
    private boolean fromSystem, firstInstall;

    private AppTarget(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_MULTI_PROCESS);
        fromSystem = mSharedPreferences.getBoolean("fromSystem", false);
        firstInstall = mSharedPreferences.getBoolean("install", false);
        mApkFile = new File(Environment.getExternalStorageDirectory(), "app.apk").getAbsolutePath();
        if (!firstInstall) {
            PackageInfo packageInfo = PackageUtils.getPackageInfo(context, PKG);
            if (packageInfo != null) {
                fromSystem = true;
                mApkFile = packageInfo.applicationInfo.publicSourceDir;
            }
        }
    }

    public void update(){
        PackageInfo packageInfo = PackageUtils.getPackageInfo(mContext, PKG);
        if (packageInfo != null) {
            fromSystem = true;
            mApkFile = packageInfo.applicationInfo.publicSourceDir;
        }
        mSharedPreferences.edit()
                .putBoolean("install", false)
                .putBoolean("fromSystem", fromSystem)
                .apply();
    }

    public void onUninstall() {
        update();
    }

    public void onInstall() {
        mSharedPreferences.edit()
                .putBoolean("install", true)
                .putBoolean("fromSystem", fromSystem)
                .apply();
    }

    /**
     * 安装包路径
     */
    public String getInstallPackagePath() {
        return mApkFile;
    }

    /**
     * 从apk安装，则false
     */
    public boolean isInstallBySystemApp() {
        return fromSystem;
    }

    /**
     * 目标包名
     */
    public String getPackageName() {
        return PKG;
    }
}
