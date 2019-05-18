package com.team.abc.model;

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
}
