package com.example.pgpt1

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import com.example.pgpt1.databinding.ActivityAsymmetricKeyGenerationBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
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
            try {
                load(FileInputStream("com.example.pgpt1"), "123".toCharArray())
                println("PGPT: Loaded custom key store")
            } catch (e: Throwable) {
                load(null)
                println("PGPT: Initialized new key store")
            }
        }

        binding.editUsername.doAfterTextChanged {
            onUsernameChange(it)
        }

        mUsernameTouched.observe(this, {
            binding.btnGenerate.isEnabled = it
        })
        binding.btnGenerate.setOnClickListener {
            generateKeys()
        }

        val view = binding.root
        setContentView(view)
    }

    private fun generateKeys() {
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
        val pubKey = Base64.getEncoder().encode(keyPair.public.encoded)
        binding.publicKey.setText(
            keyPair.public.format + "\n" +
                    keyPair.public.algorithm + "\n" +
                    pubKey.decodeToString()
        )


//        val privateKey = Base64.getEncoder().encode(keyPair.private.encoded)
//        binding.privateKey.setText(privateKey.decodeToString())

//        val secretKey = KeyStore.SecretKeyEntry()
//        keyStore.setEntry(mKeyAlias)

//        val protParam: KeyStore.ProtectionParameter = KeyStore.

//        var key: SecretKey = object : SecretKey {
//            override fun getAlgorithm() = "RSA"
//
//            override fun getFormat() = "X.509"
//
//            override fun getEncoded(): ByteArray {
//                return keyPair.private.encoded
//            }
//        }
//
//        val entry: KeyStore.SecretKeyEntry = KeyStore.SecretKeyEntry(key)
//        keyStore.setEntry(mKeyAlias, entry, null)

        val privateKeyStored = keyStore.getKey(mKeyAlias, null)
//        val publicKey = keyStore.getCertificate(mKeyAlias).publicKey
//        println("KEYS: private -> $privateKey")
        val publicKey = keyStore.getKey(mKeyAlias, null)
        val publicEntry =
            keyStore.getEntry(mKeyAlias, null)
        val publicCert = keyStore.getCertificate(mKeyAlias)
        println("KEYS: public key -> ${publicKey ?: "null"}")
        println("KEYS: public entry -> ${publicEntry ?: "null"}")
        println("KEYS: public cert -> $publicCert")
        mUsernameTouched.value = false
    }

//        if (mKeyStore != null) {
//            try {
//                val store = mKeyStore!!
//                val privateKey = store.getKey(mKeyAlias, null)
//                val publicKey = store.getCertificate(mKeyAlias).publicKey
//                println("KEYS: private -> $privateKey")
//                println("KEYS: public -> $publicKey")
//                println("KEYS: public -> ${publicKey.encoded}")
//                mUsernameTouched.value = false
//            } catch (e: Throwable) {
//                println("KEYS: Error: ${e.message}")
//                e.printStackTrace()
//            }
//        }


    private fun onUsernameChange(it: Editable?) {
        mUsername = it?.toString() ?: ""
        println("PGPT: Change $it <-> $mUsername")
        mUsernameTouched.value = true
//        if (mKeyStore != null) {
//            try {
//                val store = mKeyStore!!
//                val publicKey = store.getCertificate(mKeyAlias).publicKey
//                binding.publicKey.setText(publicKey.encoded.toString())
//            } catch (e: Throwable) {
//                println("KEYS: Error: ${e.message}")
//                e.printStackTrace()
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val ACTION_ASYMMETRIC_KEY_START = "ACTION_ASYMMETRIC_KEY_START"
    }
}