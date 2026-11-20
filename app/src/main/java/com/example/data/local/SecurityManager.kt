package com.example.data.local

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class HashedPin(
    val saltBase64: String,
    val hashBase64: String
)

object SecurityManager {
    private const val PBKDF2_ALGO = "PBKDF2WithHmacSHA256"
    private const val PIN_ITERATIONS = 10_000
    private const val BACKUP_ITERATIONS = 12_000
    private const val KEY_BITS = 256
    private const val GCM_TAG_BITS = 128

    private val random = SecureRandom()

    /**
     * Never stores plain-text PIN. Generates a random 16-byte salt and derives a 256-bit PBKDF2 hash.
     */
    fun hashPin(pin: String): HashedPin {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val hash = pbkdf2(pin.toCharArray(), salt, PIN_ITERATIONS, KEY_BITS)
        return HashedPin(
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        )
    }

    fun verifyPin(pin: String, saltBase64: String, expectedHashBase64: String): Boolean {
        if (saltBase64.isBlank() || expectedHashBase64.isBlank()) return false
        return try {
            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            val expected = Base64.decode(expectedHashBase64, Base64.NO_WRAP)
            val candidate = pbkdf2(pin.toCharArray(), salt, PIN_ITERATIONS, KEY_BITS)
            MessageDigest.isEqual(candidate, expected)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Encrypts backup JSON payload using AES-256-GCM with a PBKDF2-derived key from user passphrase.
     */
    fun encryptBackupJson(plainJson: String, passphrase: String): String {
        val salt = ByteArray(16)
        val iv = ByteArray(12)
        random.nextBytes(salt)
        random.nextBytes(iv)

        val keyBytes = pbkdf2(passphrase.toCharArray(), salt, BACKUP_ITERATIONS, KEY_BITS)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

        return JSONObject().apply {
            put("app", "AmarNote")
            put("encrypted", true)
            put("cipher", "AES-256-GCM")
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        }.toString(2)
    }

    fun isEncryptedBackup(rawText: String): Boolean {
        return try {
            val obj = JSONObject(rawText)
            obj.optBoolean("encrypted", false) && obj.has("ciphertext")
        } catch (_: Exception) {
            false
        }
    }

    fun decryptBackupJson(encryptedEnvelope: String, passphrase: String): String {
        val obj = JSONObject(encryptedEnvelope)
        val salt = Base64.decode(obj.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(obj.getString("iv"), Base64.NO_WRAP)
        val ciphertext = Base64.decode(obj.getString("ciphertext"), Base64.NO_WRAP)

        val keyBytes = pbkdf2(passphrase.toCharArray(), salt, BACKUP_ITERATIONS, KEY_BITS)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_BITS, iv))
        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun pbkdf2(chars: CharArray, salt: ByteArray, iterations: Int, bits: Int): ByteArray {
        val spec = PBEKeySpec(chars, salt, iterations, bits)
        return try {
            SecretKeyFactory.getInstance(PBKDF2_ALGO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        val res = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return res == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
        prompt.authenticate(info)
    }
}
