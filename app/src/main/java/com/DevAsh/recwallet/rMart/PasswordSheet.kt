package com.DevAsh.recwallet.rMart

import android.Manifest
import android.app.Dialog
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.*
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.DevAsh.recwallet.Context.ApiContext
import com.DevAsh.recwallet.Context.DetailsContext
import com.DevAsh.recwallet.Database.ExtraValues
import com.DevAsh.recwallet.Helper.AlertHelper
import com.DevAsh.recwallet.Helper.PasswordHashing
import com.DevAsh.recwallet.Home.Transactions.CallBack
import com.DevAsh.recwallet.Home.Transactions.FingerprintHelper
import com.DevAsh.recwallet.R
import com.DevAsh.recwallet.Registration.Register.Companion.hideKeyboardFrom
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.android.synthetic.main.sheet_rmart_password.*

import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


class PasswordSheet(val callBack: com.DevAsh.recwallet.rMart.CallBack) : BottomSheetDialogFragment() {

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator
    private val KEY_NAME = ApiContext.qrKey
    lateinit var cipher: Cipher
    lateinit var  fingerprintManager:FingerprintManager
    lateinit var keyguardManager:KeyguardManager
    var isTooManyAttempts = false
    val handler= Handler()
    var extraValues:ExtraValues? = null
    var helper:FingerprintHelper?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sheet_rmart_password, container, false)
        extraValues =  Realm.getDefaultInstance().where(ExtraValues::class.java).findFirst()
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        expand(dialog)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        amount.text = GetAccess.amount
        fingerPrint()
        onClick()
    }



    private fun onClick(){
        done.setOnClickListener {
            password(it)
        }

        forget.setOnClickListener {
            this.dismiss()
            callBack.deny()
        }


    }

    fun done(){
        Handler().postDelayed({
            this.dismiss()
            callBack.allow()
        }, 500)
    }

    fun failed(){
      callBack.deny()
    }

    fun password(view: View){
        if(PasswordHashing.decryptMsg(DetailsContext.password!!)==password.text.toString()){
            hideKeyboardFrom(activity!!, view)
            Handler().postDelayed({
                done()
            }, 500)

            if(extraValues==null || extraValues?.isEnteredPasswordOnce!!){
                Realm.getDefaultInstance().executeTransactionAsync{
                    val extraValues = ExtraValues(true)
                    it.insert(extraValues)
                }
            }
        }else{
            println(PasswordHashing.decryptMsg(DetailsContext.password!!) + " actual password")
            println(password.text.toString() + " actual password")
            invalidPassword.visibility = View.VISIBLE
            Handler().postDelayed({
                invalidPassword.visibility = View.INVISIBLE
            },2000)
        }

    }

    override fun onDestroy() {
        helper?.stopAuth()
        super.onDestroy()
    }



    private fun fingerPrint(){
        if (checkLockScreen()) {
            generateKey()
            if (initCipher()) {
                var cryptoObject: FingerprintManager.CryptoObject
                cipher.let {
                    cryptoObject = FingerprintManager.CryptoObject(it)
                }
                helper = FingerprintHelper(activity!!, object : CallBack {
                    override fun onSuccess() {
                        if (extraValues == null || !extraValues?.isEnteredPasswordOnce!!) {
                            animateBell("Enter password to enable fingerprint")
                        } else {
                            startVibrate(100)
                            fingerPrint.setColorFilter(Color.parseColor("#4BB543"))
                            errorMessage.setTextColor(Color.parseColor("#4BB543"))
                            errorMessage.text = "Authentication success !"
                            Handler().postDelayed({
                                done()
                            }, 200)
                        }
                    }

                    override fun onFailed() {
                        startVibrate(200)
                        animateBell()
                    }

                    override fun onDirtyRead() {
                        startVibrate(200)
                        animateBell("Could'nt process fingerprint")
                    }

                    override fun onTooManyAttempt() {
                        isTooManyAttempts = true
                        handler.removeCallbacksAndMessages(1)
                        fingerPrint?.setColorFilter(Color.GRAY)
                        errorMessage?.setTextColor(Color.GRAY)
                        errorMessage?.text = "Fingerprint disabled"
                    }
                })

                helper?.startAuth(fingerprintManager, cryptoObject)
            }
        }
    }
    private fun checkLockScreen(): Boolean {
         keyguardManager = activity?.getSystemService(Context.KEYGUARD_SERVICE)
                as KeyguardManager
         fingerprintManager = activity?.getSystemService(Context.FINGERPRINT_SERVICE)
                as FingerprintManager
        if (!keyguardManager.isKeyguardSecure) {
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.USE_FINGERPRINT
            ) !=
            PackageManager.PERMISSION_GRANTED) {
            AlertHelper.showError(
                "Permission not enabled (Fingerprint)",
                activity!!
            )

            return false
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            AlertHelper.showError(
                "No fingerprint registered, please register",
                activity!!
            )
            return false
        }
        return true
    }
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(
                "Failed to get KeyGenerator instance", e
            )
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            keyStore.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
    private fun initCipher(): Boolean {

        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore.load(null)
            val key = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }
    private fun startVibrate(time: Long){
        val v: Vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(time)
        }
    }
    private fun animateBell(message: String = "Authentication Failed") {

        val imgBell: ImageView = fingerPrint as ImageView
        imgBell.setColorFilter(Color.RED)
        errorMessage.text=message
        errorMessage.visibility=View.VISIBLE
        errorMessage.setTextColor(Color.RED)

        val shake: Animation = AnimationUtils.loadAnimation(activity!!, R.anim.shakeanimation)
        imgBell.animation = shake
        imgBell.startAnimation(shake)

        handler.postDelayed({
            if (!isTooManyAttempts && !message.startsWith("Enter")) {
                imgBell.setColorFilter(resources.getColor(R.color.textDark))
                errorMessage.text = "Touch the fingerprint sensor"
                errorMessage.setTextColor(resources.getColor(R.color.textDark))
            }
        }, 1500)
    }
    companion object{
        fun expand(dialog: Dialog?){
        dialog?.setOnShowListener { _ ->
            val d = dialog as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetInternal)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }
    }

}