package com.kk.plugin1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import io.virtualapp.bridge.IClient;
import io.virtualapp.bridge.ServerBean;

public class MyService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends IClient.Stub {
        @Override
        public boolean login(String name, String pwd) throws RemoteException {
            Log.d("client", "login:" + name + "/" + pwd);
            return true;
        }

        @Override
        public ServerBean checkUpdate() throws RemoteException {
            ServerBean serverBean = new ServerBean(MyService.this.getClass().getName(), 102);
            Log.d("client", "checkUpdate:" + serverBean);
            return serverBean;
        }
    }
}
