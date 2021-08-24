package com.example.pgpt1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AsymmetricKeyGenerationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asymmetric_key_generation)
    }

    companion object {
        const val ACTION_ASYMMETRIC_KEY_START = "ACTION_ASYMMETRIC_KEY_START"
    }
}