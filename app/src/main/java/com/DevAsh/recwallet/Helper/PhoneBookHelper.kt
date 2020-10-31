package com.DevAsh.recwallet.Helper

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract

object PhoneBookHelper {

    fun getContacts(context: Context): List<String> {
        val contacts = arrayListOf<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cr: ContentResolver = context.contentResolver
        cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")?.use { cursor ->
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val contactNumber = cursor.getString(numberIndex).replace(" ","").replace("+","")
                contacts.add(contactNumber)
                contacts.add("91$contactNumber")

            }
        }
        return contacts
    }
}