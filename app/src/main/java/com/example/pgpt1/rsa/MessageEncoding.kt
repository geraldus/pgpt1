package com.example.pgpt1.rsa

import android.os.Bundle
import android.util.Base64
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.pgpt1.databinding.ActivityMessageEncodingBinding
import com.example.pgpt1.model.com.example.pgpt1.defaults.Rsa
import com.example.pgpt1.model.com.example.pgpt1.rsa.MessageEncodingViewModel
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class MessageEncoding : AppCompatActivity() {

    private lateinit var binding: ActivityMessageEncodingBinding

    private val model: MessageEncodingViewModel by viewModels()

    private var isValidInput: Boolean = false

    private var key: PublicKey? = null

    private var haveKey: Boolean = false
//
//    private val keyIsSupported = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessageEncodingBinding.inflate(layoutInflater)

        binding.key.doAfterTextChanged {
            model.publicKey.value = it.toString()
        }

        binding.message.doAfterTextChanged {
            model.message.value = it.toString()
        }

        binding.encrypt.apply {
            isEnabled = key != null
            setOnClickListener {
                println("CLICK")
                encrypt()
            }
        }

        model.message.observe(this) {
            checkValidity()
        }

        model.publicKey.observe(this) {
            checkValidity()
        }

        model.encoded.observe(this) {
            binding.encodedMessage.setText(it)
        }

        setContentView(binding.root)
    }

    private fun encrypt() {
        println("ENCRYPTION")
        if (key == null /* || !haveKey || !isValidInput */) {
            print("Encryption error: wrong config")
            return
        }
        println("Creating Cipher")
        val cipher = Cipher.getInstance(Rsa.RSA_TRANSFORMATION)
        println("Initializing Cipher")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        println("Cipher initialized")
        val cipherData: ByteArray = cipher.doFinal(model.message.value?.toByteArray())
        val encryptedData =  Base64.encodeToString(cipherData, Base64.DEFAULT)
        println("Encoding result: $encryptedData")
        model.encoded.value = encryptedData
    }

    private fun checkValidity() {
        val (valid, imported) = hasValidInput()
        if (!valid) {
            println("INVALID INPUT")
            reset()
            return
        }
        println("ENCRYPTION ALLOWED")
        key = imported
        binding.encrypt.isEnabled = true
    }

    private fun hasValidInput(): Pair<Boolean, PublicKey?> {
        val key = model.publicKey.value
        val msg = model.message.value
        return if (key == null || key.isEmpty()) {
            false to null
        } else if (msg == null || msg.trim().isEmpty()) {
            false to null
        } else {
            var convertedKey = marshalStringKey(key)
            (convertedKey != null) to convertedKey
        }
    }

    private fun marshalStringKey(str: String): PublicKey? {
        val publicBytes: ByteArray = Base64.decode(str, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        try {
            return keyFactory.generatePublic(keySpec)

        } catch (e: Throwable) {
            println("Key import error")
            e.printStackTrace()
        }
        return null
    }

    private fun reset() {
        haveKey = false
        isValidInput = false
        binding.encrypt.isEnabled = false
    }

    companion object {
        const val ACTION_MESSAGE_ENCRYPTION_ACTIVITY_START =
            "com.example.pgpt1.rsa.MessageEncryptionActivity"
    }
}