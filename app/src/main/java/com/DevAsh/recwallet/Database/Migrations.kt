package com.DevAsh.recwallet.Database

import io.realm.DynamicRealm
import io.realm.RealmMigration

open class Migrations : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema

        if(oldVersion==3L){
            var credentials = schema.get("Credentials")
            credentials!!.addField("test", String::class.java).transform{
                obj ->
                obj.set("test","hello")

            }
        }

        else if(oldVersion==4L){
            val credentials = schema.get("Credentials")
            credentials!!.removeField("test")

            val stateLedger = schema.create("StateLedger")
            stateLedger.addField("id", String::class.java).transform { obj->obj.set("id","null") }
            stateLedger.addField("amount", Integer::class.java).transform { obj->obj.set("amount","null") }

        }

        else if(oldVersion==5L){
            val checkPoint = schema.create("CheckPoint")
            checkPoint.addField("transactionCheckPoint",Integer::class.java)
            checkPoint.addField("usersCheckPoint",Integer::class.java)

        }

        else if(oldVersion==6L){
            val checkPoint = schema.get(("CheckPoint"))
            checkPoint!!.removeField("transactionCheckPoint")
            checkPoint.removeField("usersCheckPoint")
            checkPoint.addField("checkPoint",Integer::class.java)
        }

        else if(oldVersion==7L){
            val checkPoint = schema.create("RecentContacts")
            checkPoint.addField("name",String::class.java)
            checkPoint.addField("number",String::class.java)
            checkPoint.addField("freq",Integer::class.java)

        }

    }

    override fun hashCode(): Int {
        return Migrations::class.java.hashCode()
    }

    override fun equals(`object`: Any?): Boolean {
        return if (`object` == null) {
            false
        } else `object` is Migrations
    }
}