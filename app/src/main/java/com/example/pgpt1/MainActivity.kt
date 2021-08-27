package com.example.pgpt1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pgpt1.databinding.ActivityMainBinding
import java.security.KeyStore

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

    private val keyStore = KeyStore.getInstance("AndroidKeyStore")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        binding.navToKeyPairsGenerator.setOnClickListener {
            val intent = Intent(AsymmetricKeyGenerationActivity.ACTION_ASYMMETRIC_KEY_START)
            startActivity(intent)
        }

        binding.navToMessageEncryption.setOnClickListener {
            val intent = Intent(MessageEncryptionActivity.ACTION_MESSAGE_ENCRYPTION_ACTIVITY_START)
            startActivity(intent)
        }

        val view = binding.root
        setContentView(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}