package com.example.pgpt1.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {
    val username:  MutableLiveData<String> by lazy { MutableLiveData() }
}