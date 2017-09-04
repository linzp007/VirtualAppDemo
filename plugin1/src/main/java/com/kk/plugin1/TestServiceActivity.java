package com.kk.plugin1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.virtualapp.bridge.Config;
import io.virtualapp.bridge.IServer;

public class TestServiceActivity extends AppCompatActivity {
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServer = IServer.Stub.asInterface(service);
            try {
                Log.i("client", "onServiceConnected:" + mServer.getInfo());
            } catch (RemoteException e) {
                Log.e("client", "onServiceConnected:", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServer = null;
        }
    };
    private IServer mServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_service);
        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServer == null) {
                    bindService(new Intent(Config.ACTION_BIND_SERVER).setPackage(Config.SERVER_PKG),
                            mServiceConnection, BIND_AUTO_CREATE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
