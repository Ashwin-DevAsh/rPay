package com.DevAsh.recwallet.Models;

public class Transaction {
    public String name;
    public String number;
    public String time;
    public String amount;
    public String type;

    public Transaction(String name,String number,String time,String amount,String type){
        this.name=name;
        this.number=number;
        this.time=time;
        this.amount=amount;
        this.type=type;
    }
}
