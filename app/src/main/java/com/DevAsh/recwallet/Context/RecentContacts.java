package com.DevAsh.recwallet.Context;

import io.realm.RealmObject;

public class RecentContacts extends RealmObject {
    public String name;
    public String number;
    public Integer freq;

    public RecentContacts(){

    }

    public RecentContacts(String name, String number, Integer freq) {
        this.name = name;
        this.number = number;
        this.freq = freq;
    }
}
