package com.example.myapplicationbeso;

import android.widget.Toast;

public class DataClass {

    private String Name;
    private String Age;
    private String Phone;
    private String id;
    private String email;
    private String date;
    private String loc ;
    private String dataImage;
    private String key;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getDataImage() {
        return dataImage;
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataClass(String name, String age, String phone, String id, String email, String date, String loc , String dataImage) {
        this.Name = name;
        this.Age = age;
        this.Phone = phone;
        this.id = id;
        this.email = email;
        this.date = date;
        this.loc= loc;
        this.dataImage = dataImage;
    }
    public DataClass(){

    }
}
