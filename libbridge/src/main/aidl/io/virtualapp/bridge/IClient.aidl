// IClient.aidl
package io.virtualapp.bridge;

import io.virtualapp.bridge.ServerBean;
import io.virtualapp.bridge.ClientBean;

interface IClient {
    boolean login(String name,String pwd);

    ServerBean checkUpdate();
}
