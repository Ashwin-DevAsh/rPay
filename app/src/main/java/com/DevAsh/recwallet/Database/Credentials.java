package com.DevAsh.recwallet.Database;

import io.realm.RealmObject;

public class Credentials extends RealmObject {
    public Boolean isLogin = false;
    public String name;
    public String phoneNumber;
    public String email;
    public String password;
    public String token;


    public void setLogin(Boolean login) {
        isLogin = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Credentials(String name, String phoneNumber, String email, String password, String token, Boolean isLogin ){
         this.isLogin = isLogin;
         this.name = name;
         this.phoneNumber = phoneNumber;
         this.email = email;
         this.password = password;
         this.token=token;
     }

     public Credentials(){

     }
}
