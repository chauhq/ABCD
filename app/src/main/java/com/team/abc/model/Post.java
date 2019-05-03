package com.team.abc.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private long id;
    private String phoneNumber;
    private String des;
    private String url;
    private double lat;
    private double lng;
    private String state;
    private String create;
    private Double price;
    private String userId;

    public Post() {

    }

    public Post(long id, String phoneNumber, String des, String url, double lat, double lng, String state, String create, Double price, String userId) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.des = des;
        this.url = url;
        this.lat = lat;
        this.lng = lng;
        this.state = state;
        this.create = create;
        this.price = price;
        this.userId = userId;
    }


    protected Post(Parcel in) {
        id = in.readLong();
        phoneNumber = in.readString();
        des = in.readString();
        url = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        state = in.readString();
        create = in.readString();
        price = in.readDouble();
        userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(phoneNumber);
        dest.writeString(des);
        dest.writeString(url);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(state);
        dest.writeString(create);
        dest.writeDouble(price);
        dest.writeString(userId);
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
