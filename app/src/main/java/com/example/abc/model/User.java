package com.example.abc.model;

public class User {
    private String myPhone;
    private String name;
    private String password;

    public User() {

    }

    public User(String myPhone, String name, String pass) {
        this.myPhone = myPhone;
        this.name = name;
        this.password = pass;
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
}
