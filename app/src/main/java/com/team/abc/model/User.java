package com.team.abc.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String myPhone;
    private String name;
    private String password;
    private boolean isAccVip = false;

    public User() {

    }

    public User(String myPhone, String name, String pass, boolean isAccVip) {
        this.myPhone = myPhone;
        this.name = name;
        this.password = pass;
        this.isAccVip = isAccVip;
    }

    protected User(Parcel in) {
        myPhone = in.readString();
        name = in.readString();
        password = in.readString();
        isAccVip = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMyPhone() {
        return myPhone;
    }

    public void setMyPhone(String myPhone) {
        this.myPhone = myPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAccVip() {
        return isAccVip;
    }

    public void setAccVip(boolean accVip) {
        isAccVip = accVip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(myPhone);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeByte((byte) (isAccVip ? 1 : 0));
    }
}
