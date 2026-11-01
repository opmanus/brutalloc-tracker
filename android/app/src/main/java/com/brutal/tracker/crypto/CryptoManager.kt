package com.brutal.tracker.crypto

import android.util.Base64
import com.brutal.tracker.data.EncryptedEnvelope
import com.brutal.tracker.data.LocationPacket
import com.google.gson.Gson
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12

    private val secureRandom = SecureRandom()
    private val gson = Gson()

    /**
     * Encrypts a LocationPacket using AES-256-GCM with a passphrase-derived key
     */
    fun encrypt(packet: LocationPacket, passphrase: String): EncryptedEnvelope {
        val plaintextJson = gson.toJson(packet)
        val plaintextBytes = plaintextJson.toByteArray(Charsets.UTF_8)

        // Generate 16-byte random salt and 12-byte random IV
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        // Derive 256-bit AES key via PBKDF2WithHmacSHA256
        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // Encrypt with AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherBytes = cipher.doFinal(plaintextBytes)

        return EncryptedEnvelope(
            s = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            c = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts an EncryptedEnvelope back to LocationPacket (useful for local verification)
     */
    fun decrypt(envelope: EncryptedEnvelope, passphrase: String): LocationPacket {
        val salt = Base64.decode(envelope.s, Base64.NO_WRAP)
        val iv = Base64.decode(envelope.iv, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(envelope.c, Base64.NO_WRAP)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decryptedBytes = cipher.doFinal(cipherBytes)
        val json = String(decryptedBytes, Charsets.UTF_8)
        return gson.fromJson(json, LocationPacket::class.java)
    }
}
