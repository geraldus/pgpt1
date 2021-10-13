package com.example.pgpt1

import android.os.Bundle
import android.security.keystore.KeyProperties
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.pgpt1.databinding.ActivityMessageEncryptionBinding
import com.example.pgpt1.model.com.example.pgpt1.defaults.Rsa
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.Key
import java.security.KeyStore
import java.security.PublicKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.spec.MGF1ParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

class MessageEncryptionActivity : AppCompatActivity() {

    private var _binding: ActivityMessageEncryptionBinding? = null

    private val binding get() = _binding!!

    private var recipient: String = ""

    private var message: String = ""

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMessageEncryptionBinding.inflate(layoutInflater)

        binding.recipient.doAfterTextChanged { recipient = it.toString() }
        binding.message.doAfterTextChanged { message = it.toString() }

        binding.encrypt.setOnClickListener { encrypt() }

//        var x = Workbook

        val view = binding.root

        setContentView(view)
    }

    private fun encrypt() {
        println("PGPT: Encrypting")
        CoroutineScope(Dispatchers.IO).launch {
            encryptAsync()
        }
    }

    private suspend fun encryptAsync() {
        val maybeKey = getRecipientKey()
        if (maybeKey != null) {
            val recipientKey = maybeKey.publicKey
            val recipientKeyTxt = Base64.getEncoder().encodeToString(recipientKey.encoded)

            println("PGPT: CERT -> $recipientKeyTxt")

            val sessionKey = generateSessionKey()
            val encoded = encodeMessage(sessionKey)
            val (msg, iv) = encoded
            withContext(Dispatchers.Main) {
                onEncryptionDone(recipientKey, sessionKey, iv, msg)
            }
        }
    }

    private fun generateSessionKey(): SecretKey {
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val rnd = SecureRandom()
        kg.init(256, rnd)
        return kg.generateKey()
    }

    private fun rsaEncode(
        keyToEncode: SecretKey,
        iv: ByteArray,
        recipientKey: PublicKey
    ): ByteArray {
        val cipher = getRSACipher(recipientKey, Cipher.ENCRYPT_MODE)
        val message = keyToEncode.encoded + iv
        return cipher.doFinal(message)
    }

    private fun encodeMessage(sessionKey: SecretKey): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(Rsa.AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
        val cipherText = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        return cipherText to iv
    }

    private fun onEncryptionDone(
        recipientKey: PublicKey,
        sessionKey: SecretKey,
        iv: ByteArray,
        result: ByteArray
    ) {
        fun base64(msg: ByteArray) = Base64.getEncoder().encodeToString(msg)
        val encodedSK = rsaEncode(sessionKey, iv, recipientKey)
        val sessionKeyText = base64(sessionKey.encoded)
        val ivText = base64(iv)
        val encodedMessage = base64(result)
        binding.sessionKey.setText(sessionKeyText)
        binding.iv.setText(ivText)
        binding.encrypted.setText(encodedMessage)
        binding.encodedSecret.setText(Base64.getEncoder().encodeToString(encodedSK))

        println("PGPT: RESULTS")
        println("PGPT: Session key: $sessionKeyText")
        println("PGPT: IV: $ivText")

        val recipientPrivateKey = keyStore.getKey("${recipient}_key", null)
        println("PGPT: Private key for decode $recipientPrivateKey / ${recipient}_key")
        val cipher = getRSACipher(recipientPrivateKey, Cipher.DECRYPT_MODE)
//        val unBase64 = Base64.getDecoder().decode(result)
        val decodedSecret = cipher.doFinal(encodedSK)
        val ivSize = iv.size
        val keySize = sessionKey.encoded.size
        println("PGPT: IV SIZE = $ivSize, KEY SIZE = $keySize")
        val decodedKey = decodedSecret.copyOfRange(0, keySize)
        val decodedIV = decodedSecret.copyOfRange(keySize, keySize + iv.size)
        println("PGPT: TESTING KEY EQ -> ${decodedKey.contentEquals(sessionKey.encoded)}")
        println("PGPT: TESTING IV EQ -> ${iv.contentEquals(decodedIV)}")
        println("PGPT: DECODED SESSION KEY -> ${base64(decodedKey)}")
        println("PGPT: DECODED IV -> ${base64(decodedIV)}")

        val aesCipher = Cipher.getInstance(Rsa.AES_TRANSFORMATION)
        val k = SecretKeySpec(decodedKey, "AES")
        aesCipher.init(Cipher.DECRYPT_MODE, k, IvParameterSpec(decodedIV))
        val decodedResult = aesCipher.doFinal(result)
        println("PGPT: DECODED -> ${decodedResult.toString(Charsets.UTF_8)}")
    }

    private suspend fun getRecipientKey(): Certificate? {
        val cert = keyStore.getCertificate("${recipient}_key")
        return if (cert == null) {
            withContext(Dispatchers.Main) {
                onNoRecipientKey()
            }
            null
        } else {
            cert
        }
    }

    private fun onNoRecipientKey() {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setMessage(getString(R.string.msg_no_recipient_key))
            setPositiveButton(
                getString(R.string.btn_got_it),
                null
            )
            create()
        }
        dialog.show()
    }

    private fun getRSACipher(key: Key, mode: Int): Cipher {
        return Cipher.getInstance(Rsa.RSA_TRANSFORMATION).apply {
            init(mode, key, Rsa.oaepParamSpec)
            println("PGPT: CIPHER DECRYPT MODE")
        }
    }

    private fun getRSACipher(key: PublicKey, mode: Int): Cipher {
        return Cipher.getInstance(Rsa.RSA_TRANSFORMATION).apply {
            init(mode, key, Rsa.oaepParamSpec)
            println("PGPT: CIPHER ENCRYPT MODE")
        }
    }

    companion object {
        const val ACTION_MESSAGE_ENCRYPTION_ACTIVITY_START =
            "com.example.pgpt1.MessageEncryptionActivity"
    }
}