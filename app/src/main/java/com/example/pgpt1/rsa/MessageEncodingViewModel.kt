package com.example.pgpt1.model.com.example.pgpt1.rsa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageEncodingViewModel: ViewModel() {
    val publicKey: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val message: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val encoded: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}