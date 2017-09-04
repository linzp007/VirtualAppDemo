// IServer.aidl
package io.virtualapp.bridge;

import io.virtualapp.bridge.ServerBean;
import io.virtualapp.bridge.ClientBean;

interface IServer {

    ServerBean getInfo();

    boolean check(in ClientBean bean);
}
