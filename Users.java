package com.example.instantchat;

public class Users {

    public String image;
    public String name;
    public String status;
    public String search;


    public Users(){

    }

    public Users(String image, String name, String status,String search) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.search = search;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() { return search; }

    public void setSearch(String search) { this.search = search; }
}
