package io.virtualapp.bridge;

import android.os.Parcel;
import android.os.Parcelable;

public class ServerBean implements Parcelable {
    private String name;
    private int version;

    public ServerBean(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.version);
    }

    public ServerBean() {
    }

    protected ServerBean(Parcel in) {
        this.name = in.readString();
        this.version = in.readInt();
    }

    public static final Parcelable.Creator<ServerBean> CREATOR = new Parcelable.Creator<ServerBean>() {
        @Override
        public ServerBean createFromParcel(Parcel source) {
            return new ServerBean(source);
        }

        @Override
        public ServerBean[] newArray(int size) {
            return new ServerBean[size];
        }
    };

    @Override
    public String toString() {
        return "ServerBean{" +
                "name='" + name + '\'' +
                ", version=" + version +
                '}';
    }
}
