package com.example.pgpt1

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.pgpt1.databinding.ActivityMessageDecryptionBinding
import com.example.pgpt1.model.com.example.pgpt1.MessageDecryptionViewModel
import com.example.pgpt1.model.com.example.pgpt1.defaults.Rsa.Companion.RSA_TRANSFORMATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

class MessageDecryptionActivity : AppCompatActivity() {

    private var _binding: ActivityMessageDecryptionBinding? = null

    private val model: MessageDecryptionViewModel by viewModels()

    private val binding get() = _binding!!

    private var recipient: String = ""

    private var message: String = ""

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMessageDecryptionBinding.inflate(layoutInflater)

        binding.recipient.doAfterTextChanged {
            recipient = it.toString()
            model.dataIsValid.value = canDecrypt()
        }
        binding.message.doAfterTextChanged {
            message = it.toString()
            model.dataIsValid.value = canDecrypt()
        }


        binding.decrypt.setOnClickListener {
            decrypt()
        }

        model.dataIsValid.observe(this) {
            binding.decrypt.isEnabled = it
        }

        model.decodedMessage.observe(this) {
            binding.decodedMessage.setText(it)
        }

        val view = binding.root

        setContentView(view)
    }

    private fun canDecrypt(): Boolean {
        return message.isNotEmpty() && recipient.isNotEmpty()
    }

    private fun decrypt() {
        println("PGPT: Decrypting")
        if (canDecrypt()) {
            CoroutineScope(Dispatchers.IO).launch {
                decryptAsync()
            }
        } else {
            println("Wrong input")
        }
    }

    private suspend fun decryptAsync() {
        val key = getRecipientPrivateKey()
        if (key == null) {
            withContext(Dispatchers.Main) {
                onNoRecipientKey()
            }
        } else {
            var cipher: Cipher? = null
            try {
                cipher = getRSACipher(key, Cipher.DECRYPT_MODE)
            } catch (e: KeyStoreException) {
                withContext(Dispatchers.Main) {
                    wrongKeyAlert()
                }
            }

            val messageBytes = Base64.getDecoder().decode(message)
            try {
                val decodedSecret = cipher?.doFinal(messageBytes)
                withContext(Dispatchers.Main) {
                    if (decodedSecret != null) {
                        model.decodedMessage.value = decodedSecret.decodeToString()

                    } else {
                        decryptionError()
                    }
                }
            } catch (e: KeyStoreException) {
                println("Decryption error")
                println(e.localizedMessage)
            } catch (e: IllegalBlockSizeException) {
                withContext(Dispatchers.Main) {
                    println("Decryption error, invalid block size")
                    println(e.localizedMessage)
                    decryptionError()
                }
            } catch (e: InvalidKeyException) {
                withContext(Dispatchers.Main) {
                    model.decodedMessage.value = ""
                    wrongKeyAlert()
                }
            }
        }
    }

    private fun wrongKeyAlert() {
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

//    private fun generateSessionKey(): SecretKey {
//        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
//        val rnd = SecureRandom()
//        kg.init(256, rnd)
//        return kg.generateKey()
//    }
//
//    private fun rsaEncode(keyToEncode: SecretKey, iv: ByteArray, recipientKey: PublicKey): ByteArray {
//        val cipher = getRSACipher(recipientKey, Cipher.ENCRYPT_MODE)
//        val message = keyToEncode.encoded + iv
//        return cipher.doFinal(message)
//    }
//
//    private fun encodeMessage(sessionKey: SecretKey): Pair<ByteArray, ByteArray> {
//        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
//        cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
//        val cipherText = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
//        val iv = cipher.iv
//        return cipherText to iv
//    }
//
//    private fun onEncryptionDone(
//        recipientKey: PublicKey,
//        sessionKey: SecretKey,
//        iv: ByteArray,
//        result: ByteArray
//    ) {
//        fun base64(msg: ByteArray) = Base64.getEncoder().encodeToString(msg)
//        val encodedSK = rsaEncode(sessionKey, iv, recipientKey)
//        val sessionKeyText = base64(sessionKey.encoded)
//        val ivText = base64(iv)
//        val encodedMessage = base64(result)
//        binding.sessionKey.setText(sessionKeyText)
//        binding.iv.setText(ivText)
//        binding.encrypted.setText(encodedMessage)
//        binding.encodedSecret.setText(Base64.getEncoder().encodeToString(encodedSK))
//
//        println("PGPT: RESULTS")
//        println("PGPT: Session key: $sessionKeyText")
//        println("PGPT: IV: $ivText")
//
//        val recipientPrivateKey = keyStore.getKey("${recipient}_key", null)
//        println("PGPT: Private key for decode $recipientPrivateKey / ${recipient}_key")
//        val cipher = getRSACipher(recipientPrivateKey, Cipher.DECRYPT_MODE)
////        val unBase64 = Base64.getDecoder().decode(result)
//        val decodedSecret = cipher.doFinal(encodedSK)
//        val ivSize = iv.size
//        val keySize = sessionKey.encoded.size
//        println("PGPT: IV SIZE = $ivSize, KEY SIZE = $keySize")
//        val decodedKey = decodedSecret.copyOfRange(0, keySize)
//        val decodedIV = decodedSecret.copyOfRange(keySize, keySize + iv.size)
//        println("PGPT: TESTING KEY EQ -> ${decodedKey.contentEquals(sessionKey.encoded)}")
//        println("PGPT: TESTING IV EQ -> ${iv.contentEquals(decodedIV)}")
//        println("PGPT: DECODED SESSION KEY -> ${base64(decodedKey)}")
//        println("PGPT: DECODED IV -> ${base64(decodedIV)}")
//
//        val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
//        val k = SecretKeySpec(decodedKey, "AES")
//        aesCipher.init(Cipher.DECRYPT_MODE, k, IvParameterSpec(decodedIV))
//        val decodedResult = aesCipher.doFinal(result)
//        println("PGPT: DECODED -> ${decodedResult.toString(Charsets.UTF_8)}")
//    }


    private fun getRecipientPrivateKey(): Key? {
        try {
            return keyStore.getKey("${recipient}_key", null)
        } catch (e: Throwable) {
            println("Error reading key:")
            println(e.localizedMessage)
        }
        return null
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

    private fun decryptionError() {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setMessage(getString(R.string.msg_decode_error))
            setPositiveButton(
                getString(R.string.btn_got_it),
                null
            )
            create()
        }
        dialog.show()
    }

    private fun getRSACipher(key: Key, mode: Int): Cipher {
        return Cipher.getInstance(RSA_TRANSFORMATION).apply {
            println("Key info:")
            println(key)
            println(key.algorithm)
            println(key.format)
//            init(mode, key,  oaepParamSpec)
//            init(mode, key,  PKCS1Algorit)
            init(mode, key)
        }
    }

    companion object {
        const val ACTION_MESSAGE_DECRYPTION_ACTIVITY_START =
            "com.example.pgpt1.MessageDecryptionActivity"
    }
}