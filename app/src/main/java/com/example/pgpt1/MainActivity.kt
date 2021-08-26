package com.example.pgpt1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pgpt1.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.cert.Certificate

class MainActivity : AppCompatActivity() {
    // Шифрование
    // 1.  генерируем пары ключей для получателя
    // 2.  генерируем сессионный ключ
    // 3.  читаем сообщение
    // 4.  опционально сжимаем сообщение
    // 5.  шифруем сообщение с сессионным ключом
    // 6.  шифруем сессионый ключ с помощью открытого ключа получателя
    // 7.  отправляем зашифрованное сообщение с зашифрованным сессионным ключом
    // 8.  опционально -- соединяем шифрованное сообщение с шифрованным ключом в одно сообщение
    // 9.  подписываем комбинированные данные

    // Дешифровка
    // 1.  дешифруем сессионный ключ с помощью закрытого ключа получателя
    // 2.  дешифруем сообщение

    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        binding.navToKeyPairsGenerator.setOnClickListener {
            val intent = Intent(AsymmetricKeyGenerationActivity.ACTION_ASYMMETRIC_KEY_START)
            startActivity(intent)
        }

        runChecks()

        val view = binding.root
        setContentView(view)
    }

    private var mCerts: Array<out Certificate>? = null

    private fun runChecks() {
        binding.statusView.text = getString(R.string.msg_running_cert_chain_get)
        println("MAIN: running checks")
        CoroutineScope(Dispatchers.IO).launch {
            check()
        }
    }

    private suspend fun getCertsDone(certs: Array<out Certificate>?) {
        withContext(Dispatchers.Main) {
            mCerts = certs
            if (certs == null) {
                binding.statusView.text = getString(R.string.msg_keystore_certs_null)
            }else {
                TODO("Check all certs")
            }
            println("MAIN: certs done $certs")
        }
    }

    private suspend fun check() {
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
            .apply { load(null) }

        var result: Array<out Certificate>? = null

        try {
            val certs = keystore.getCertificateChain("AndroidKeyStore")
            result = certs
        } catch (e: IllegalStateException) {
            withContext(Dispatchers.Main) { illegalStateError(e) }
        } finally {
            getCertsDone(result)
        }
    }

    private fun illegalStateError(e: IllegalStateException) {
        println("MAIN: ERROR ${e.message}")
        binding.statusView.text = getString(R.string.msg_keystore_illegal_state)
        e.printStackTrace()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}