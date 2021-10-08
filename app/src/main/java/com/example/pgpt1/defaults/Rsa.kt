package com.example.pgpt1.model.com.example.pgpt1.defaults

import java.security.spec.MGF1ParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class Rsa {
    companion object {
        const val RSA_TRANSFORMATION = "RSA/NONE/PKCS1PADDING"

        const val AES_TRANSFORMATION = "AES/CBC/PKCS5PADDING"

        val oaepParamSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA1,
            PSource.PSpecified.DEFAULT
        )

    }
}