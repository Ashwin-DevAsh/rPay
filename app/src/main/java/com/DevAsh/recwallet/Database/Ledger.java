package com.DevAsh.recwallet.Database;

class Ledger {
    public String id;
    public Integer balance;

    public Ledger(){}

    public Ledger(String id, Integer balance) {
        this.id = id;
        this.balance = balance;
    }
}
