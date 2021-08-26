package com.example.pgpt1

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import com.example.pgpt1.databinding.ActivityAsymmetricKeyGenerationBinding
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*

class AsymmetricKeyGenerationActivity : AppCompatActivity() {
    private var _binding: ActivityAsymmetricKeyGenerationBinding? = null
    private val binding get() = _binding!!

    private var mUsername = ""
    private val mUsernameTouched: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    private val mKeyAlias = "${mUsername}_key"

    private var keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())//"AndroidKeyStore")

    init {
        println("PGPT: Default key store type = ${KeyStore.getDefaultType()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAsymmetricKeyGenerationBinding.inflate(layoutInflater)

        keyStore.apply { load(null) }

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
        val keyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                mKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build()
        )
        val keyPair = keyPairGenerator.generateKeyPair()
        val pubKey = Base64.getEncoder().encode(keyPair.public.encoded)
        binding.publicKey.setText(pubKey.decodeToString())
        //            keyPair.public.format + "\n" +
//                    keyPair.public.algorithm + "\n" +

        val privateKey = Base64.getEncoder().encode(keyPair.private.encoded)
        binding.privateKey.setText(privateKey.decodeToString())

        keyStore.store()

//        keyStore.setEntry(mKeyAlias)

        val privateKeyStored = keyStore.getKey(mKeyAlias, null)
        val publicKey = store.getCertificate(mKeyAlias).publicKey
                println("KEYS: private -> $privateKey")
                println("KEYS: public -> $publicKey")
                println("KEYS: public -> ${publicKey.encoded}")
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
    }

    private fun onUsernameChange(it: Editable?) {
        mUsername = it?.toString() ?: ""
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