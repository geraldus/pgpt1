package com.example.pgpt1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}