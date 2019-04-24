package com.example.abc.model;

import com.google.android.gms.maps.model.LatLng;

public class Land {
    private String image;
    private String des;
    private LatLng lng;

    public Land(String image, String des, LatLng lng, String state) {
        this.image = image;
        this.des = des;
        this.lng = lng;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public LatLng getLng() {
        return lng;
    }

    public void setLng(LatLng lng) {
        this.lng = lng;
    }
}
