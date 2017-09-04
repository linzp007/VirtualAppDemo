package io.virtualapp.lite;

import android.app.Application;
import android.content.Context;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;

import io.virtualapp.lite.delegate.MyAppRequestListener;
import io.virtualapp.lite.delegate.MyComponentDelegate;
import io.virtualapp.lite.delegate.MyPhoneInfoDelegate;
import io.virtualapp.lite.delegate.MyTaskDescriptionDelegate;


public class App extends Application {

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
                virtualCore.setPhoneInfoDelegate(new MyPhoneInfoDelegate());
                //任务历史显示,activity启动的intent处理（不显示任务）
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
            }

            @Override
            public void onServerProcess() {
                //app安装/卸载 主进程如果需要知道，请在MyAppRequestListener里面发送广播
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
