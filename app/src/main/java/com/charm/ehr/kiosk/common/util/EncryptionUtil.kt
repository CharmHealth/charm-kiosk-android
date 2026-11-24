package com.charm.ehr.kiosk.common.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptionUtil {

    val dbKeyAlias = "db_key_alias"
    val fileEncryptionKeyAlias = "file_encryption_key_alias"
    val commonEncryptionKeyAlias = "common_encryption_key_alias"

    // Function to generate or retrieve a 256-bit AES key from the Android Keystore
    fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return if (keyStore.containsAlias(alias)) {
            (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val parameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)  // 256 Bit encryption
                .build()
            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    // Function to encrypt plaintext using AES/GCM/NoPadding
    fun encryptData(plainText: String, keyAlias: String): Pair<ByteArray, ByteArray> {
        val secretKey = getSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv // The initialization vector (IV) generated during encryption
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Pair(iv, cipherText)
    }

    // Function to decrypt ciphertext using AES/GCM/NoPadding
    fun decryptData(iv: ByteArray, cipherText: ByteArray, keyAlias: String): String {
        val secretKey = getSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // GCMParameterSpec uses a 128-bit authentication tag length (which is standard) along with the IV
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }

}