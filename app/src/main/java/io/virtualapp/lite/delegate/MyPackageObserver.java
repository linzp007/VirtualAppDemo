package io.virtualapp.lite.delegate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;

import io.virtualapp.lite.App;

public class MyPackageObserver extends VirtualCore.PackageObserver {
    private Context mContext;

    public MyPackageObserver(Context context) {
        mContext = context;
    }

    private void sendBroadcast(String action, String pkg) {
        mContext.sendBroadcast(new Intent(action,
                Uri.parse("package:" + pkg)));
    }

    @Override
    public void onPackageInstalled(String packageName) throws RemoteException {
//        Log.d("server", "onPackageInstalled:" + packageName);
//        sendBroadcast(App.ACTION_PACKAGE_ADD, packageName);
    }

    @Override
    public void onPackageUninstalled(String packageName) throws RemoteException {
//        Log.d("server", "onPackageUninstalled:" + packageName);
//        sendBroadcast(App.ACTION_PACKAGE_REMOVE, packageName);
    }

    @Override
    public void onPackageInstalledAsUser(int userId, String packageName) throws RemoteException {
        Log.d("server", "onPackageInstalledAsUser:" + packageName);
        sendBroadcast(App.ACTION_PACKAGE_ADD, packageName);
    }

    @Override
    public void onPackageUninstalledAsUser(int userId, String packageName) throws RemoteException {
        Log.d("server", "onPackageUninstalledAsUser:" + packageName);
        sendBroadcast(App.ACTION_PACKAGE_REMOVE, packageName);
    }
}
