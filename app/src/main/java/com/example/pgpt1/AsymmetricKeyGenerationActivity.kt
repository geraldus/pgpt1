package com.example.pgpt1

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import com.example.pgpt1.databinding.ActivityAsymmetricKeyGenerationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.Certificate
import java.util.*
import javax.security.auth.x500.X500Principal

class AsymmetricKeyGenerationActivity : AppCompatActivity() {
    private var _binding: ActivityAsymmetricKeyGenerationBinding? = null
    private val binding get() = _binding!!

    private var mUsername = ""
    private val mUsernameTouched: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    private val mKeyAlias get() = "${mUsername}_key"

    private var keyStore: KeyStore =
        KeyStore.getInstance("AndroidKeyStore")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAsymmetricKeyGenerationBinding.inflate(layoutInflater)

        keyStore.apply {
            load(null)
        }

        binding.editUsername.doAfterTextChanged {
            onUsernameChange(it)
        }

        binding.btnGenerate.isEnabled = true

        binding.btnGenerate.setOnClickListener {
            checkKey()
        }

        val view = binding.root
        setContentView(view)
    }

    private fun generateKeys() {
        setBusy()
        CoroutineScope(Dispatchers.IO).launch {
            generateKeysAsync()
        }
    }

    private fun onKeyGenerationSuccess(keyPair: KeyPair) {
        setReady()
        val pubKey = Base64.getEncoder().encode(keyPair.public.encoded)
        mUsernameTouched.value = false
        binding.publicKey.setText(pubKey.decodeToString())

    }

    private fun onKeyGenerationFailure() {
        setReady()
    }

    private fun setBusy() {
        showProgress()
        disableButton()
    }

    private fun setReady() {
        hideProgress()
        enableButton()
    }

    private fun disableButton() {
        binding.btnGenerate.isEnabled = false
    }

    private fun enableButton() {
        binding.btnGenerate.isEnabled = true
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    private suspend fun generateKeysAsync() {
        println("PGPT: Generating key for <$mKeyAlias>")

        val keyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")

        val parameterSpec = KeyGenParameterSpec.Builder(
            mKeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setCertificateSerialNumber(BigInteger.valueOf(777))
            setCertificateSubject(X500Principal("CN=$mKeyAlias"))
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            build()
        }
        keyPairGenerator.initialize(parameterSpec)
        val keyPair = keyPairGenerator.generateKeyPair()

        withContext(Dispatchers.Main) {
            onKeyGenerationSuccess(keyPair)
        }

        val publicKey = keyStore.getKey(mKeyAlias, null)
        val publicEntry =
            keyStore.getEntry(mKeyAlias, null)

        println("KEYS: public key -> ${publicKey ?: "null"}")
        println("KEYS: public entry -> ${publicEntry ?: "null"}")
    }

    private fun checkKey() {
        CoroutineScope(Dispatchers.IO).launch {
            checkKeysAsync(mUsername)
        }
    }

    private suspend fun checkKeysAsync(username: String) {
        val cert = keyStore.getCertificate(mKeyAlias)
        withContext(Dispatchers.Main) {
            if (cert != null) {
                println("KEYS: public cert -> $cert")
                onKeyAlreadyExist(cert)
            } else {
                onKeyNotFound()
            }
        }
    }

    private fun onKeyAlreadyExist(cert: Certificate) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_key_already_exists, mUsername))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.btn_use_existing)) { dialog, id ->
                val printableVal = Base64.getEncoder().encode(cert.publicKey.encoded)
                binding.publicKey.setText(printableVal.decodeToString())
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_generate_new)) { dialog, id ->
                generateKeys()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun onKeyNotFound() {
        generateKeys()
    }

    private fun onUsernameChange(it: Editable?) {
        mUsername = it?.toString() ?: ""
        mUsernameTouched.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val ACTION_ASYMMETRIC_KEY_START = "ACTION_ASYMMETRIC_KEY_START"
    }
}