package com.example.pgpt1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pgpt1.databinding.ActivityMessageEncryptionBinding

class MessageEncryptionActivity : AppCompatActivity() {

    private var _binding: ActivityMessageEncryptionBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMessageEncryptionBinding.inflate(layoutInflater)

        val view = binding.root

        setContentView(view)
    }

    companion object {
        const val ACTION_MESSAGE_ENCRYPTION_ACTIVITY_START = "com.example.pgpt1.MessageEncryptionActivity"
    }
}