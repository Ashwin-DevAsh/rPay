package com.DevAsh.recwallet.Database;

import android.os.Build;

import com.DevAsh.recwallet.BuildConfig;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Credentials extends RealmObject {

    @Ignore
    public static Credentials credentials;

    public Boolean isLogin = false;
    public String accountName;
    public String holderName;
    public String phoneNumber;
    public String email;
    public String password;
    public String token;
    public String id;
    public String status;
    @Ignore
    public Boolean isVerified =false;



    public void setLogin(Boolean login) {
        isLogin = login;
    }
    public void setName(String name) {
        this.accountName = name;
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


    public Credentials(Boolean isLogin, String accountName, String holderName, String phoneNumber, String email, String password, String token,String status) {
        this.isLogin = isLogin;
        this.accountName = accountName;
        this.holderName = holderName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.token = token;
        this.id = BuildConfig.ID+this.phoneNumber;
        this.status = status;
    }



    public Credentials(){

     }
}
