package io.virtualapp.lite.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import io.virtualapp.bridge.ClientBean;
import io.virtualapp.bridge.IServer;
import io.virtualapp.bridge.ServerBean;

public class CoreService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends IServer.Stub {
        @Override
        public ServerBean getInfo() throws RemoteException {
            ServerBean serverBean = new ServerBean(CoreService.this.getClass().getName(), 1022);
            Log.d("server", "getInfo=" + serverBean);
            return serverBean;
        }

        @Override
        public boolean check(ClientBean bean) throws RemoteException {
            Log.d("server", "check=" + bean);
            return bean != null;
        }
    }
}
