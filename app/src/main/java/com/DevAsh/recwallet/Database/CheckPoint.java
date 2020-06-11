package com.DevAsh.recwallet.Database;

import io.realm.RealmObject;

public class CheckPoint extends RealmObject {

    public Integer transactionCheckPoint;
    public Integer usersCheckPoint;

    public CheckPoint(){}

    public CheckPoint(Integer transactionCheckPoint, Integer usersCheckPoint){
        this.transactionCheckPoint=transactionCheckPoint;
        this.usersCheckPoint=usersCheckPoint;
    }

}
