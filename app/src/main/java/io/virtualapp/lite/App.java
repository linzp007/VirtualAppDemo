package io.virtualapp.lite;

import android.app.Application;
import android.content.Context;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;

import io.virtualapp.lite.delegate.MyAppRequestListener;
import io.virtualapp.lite.delegate.MyComponentDelegate;
import io.virtualapp.lite.delegate.MyPackageObserver;
import io.virtualapp.lite.delegate.MyTaskDescriptionDelegate;


public class App extends Application {
    public static final String ACTION_PACKAGE_ADD = BuildConfig.APPLICATION_ID + ".action.pacakage.add";
    public static final String ACTION_PACKAGE_UPDATE = BuildConfig.APPLICATION_ID + ".action.pacakage.update";
    public static final String ACTION_PACKAGE_REMOVE = BuildConfig.APPLICATION_ID + ".action.pacakage.remove";
    public static final String CONTACT_PROVIDER = "com.android.providers.contacts";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //io重定向
        VASettings.ENABLE_IO_REDIRECT = true;
        //允许app发送快捷方式
        VASettings.ENABLE_INNER_SHORTCUT = false;
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {
                //TODO 主进程初始化
            }

            @Override
            public void onVirtualProcess() {
                //activity生命周期监听
                virtualCore.setComponentDelegate(new MyComponentDelegate());
                //信息伪造
                //任务历史显示,activity启动的intent处理（不显示任务）
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
            }

            @Override
            public void onServerProcess() {
                //监听全部插件的安装和卸载
                virtualCore.registerObserver(new MyPackageObserver(App.this));
                //服务会通知 通过intent的安装/卸载
                virtualCore.setAppRequestListener(new MyAppRequestListener(App.this));
                //允许内部app调用外部的app名单
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqq");
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqqi");
                virtualCore.addVisibleOutsidePackage("com.tencent.minihd.qq");
                virtualCore.addVisibleOutsidePackage("com.tencent.qqlite");
                virtualCore.addVisibleOutsidePackage("com.facebook.katana");
                virtualCore.addVisibleOutsidePackage("com.whatsapp");
                virtualCore.addVisibleOutsidePackage("com.tencent.mm");
                virtualCore.addVisibleOutsidePackage("com.immomo.momo");
            }
        });
    }
}
