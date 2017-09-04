package io.virtualapp.bridge;

import android.os.Parcel;
import android.os.Parcelable;

public class ClientBean implements Parcelable {
    private String name;
    private DataBean bean;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeParcelable(this.bean, flags);
    }

    public ClientBean() {
    }

    protected ClientBean(Parcel in) {
        this.name = in.readString();
        this.bean = in.readParcelable(DataBean.class.getClassLoader());
    }

    public static final Parcelable.Creator<ClientBean> CREATOR = new Parcelable.Creator<ClientBean>() {
        @Override
        public ClientBean createFromParcel(Parcel source) {
            return new ClientBean(source);
        }

        @Override
        public ClientBean[] newArray(int size) {
            return new ClientBean[size];
        }
    };

    @Override
    public String toString() {
        return "ClientBean{" +
                "name='" + name + '\'' +
                ", bean=" + bean +
                '}';
    }
}
