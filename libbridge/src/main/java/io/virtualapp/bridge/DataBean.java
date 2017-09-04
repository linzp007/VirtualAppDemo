package io.virtualapp.bridge;

import android.os.Parcel;
import android.os.Parcelable;

public class DataBean implements Parcelable {
    private long date;

    public DataBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.date);
    }

    protected DataBean(Parcel in) {
        this.date = in.readLong();
    }

    public static final Parcelable.Creator<DataBean> CREATOR = new Parcelable.Creator<DataBean>() {
        @Override
        public DataBean createFromParcel(Parcel source) {
            return new DataBean(source);
        }

        @Override
        public DataBean[] newArray(int size) {
            return new DataBean[size];
        }
    };
}
