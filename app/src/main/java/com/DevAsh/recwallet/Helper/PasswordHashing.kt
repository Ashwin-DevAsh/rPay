package com.DevAsh.recwallet.Helper

import android.util.Base64.*
import com.DevAsh.recwallet.Context.ApiContext
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


object PasswordHashing{


    private fun generateKey(): SecretKey? {
        return SecretKeySpec(ApiContext.key.toByteArray(), "AES")
    }
    fun encryptMsg(message: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, generateKey())
        val cipherText = cipher.doFinal(message.toByteArray(charset("UTF-8")))
        return encodeToString(cipherText, NO_WRAP)
    }
    fun decryptMsg(cipherString: String): String? {
        val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, generateKey())
        val decode: ByteArray = decode(cipherString,
            NO_WRAP)
        return String(cipher.doFinal(decode), charset("UTF-8"))
    }
}