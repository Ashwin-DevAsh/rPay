package com.DevAsh.recwallet.Database;

import io.realm.RealmObject;

public class ExtraValues extends RealmObject {
    public Boolean isEnteredPasswordOnce = false;
    public Integer timeIndex = 0;


    public ExtraValues(){}

    public ExtraValues(Boolean isEnteredPasswordOnce){
        this.isEnteredPasswordOnce = isEnteredPasswordOnce;
    }
}
