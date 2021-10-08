package com.example.pgpt1.model.com.example.pgpt1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageDecryptionViewModel: ViewModel() {
    val dataIsValid: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val decodedMessage: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}