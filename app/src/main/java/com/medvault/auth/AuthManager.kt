package com.medvault.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app authentication: biometric + PIN fallback.
 * PIN is stored as a hash in EncryptedSharedPreferences.
 */
@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            "medvault_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── Lock settings ────────────────────────────────────────────

    fun isLockEnabled(): Boolean =
        securePrefs.getBoolean("lock_enabled", false)

    fun setLockEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean("lock_enabled", enabled).apply()
    }

    fun getAutoLockTimeoutMs(): Long =
        securePrefs.getLong("auto_lock_timeout", 0L) // 0 = immediately

    fun setAutoLockTimeoutMs(timeoutMs: Long) {
        securePrefs.edit().putLong("auto_lock_timeout", timeoutMs).apply()
    }

    // ── PIN ──────────────────────────────────────────────────────

    fun isPinSet(): Boolean =
        securePrefs.getString("pin_hash", null) != null

    fun setPIN(pin: String) {
        // Simple hash — in production, use bcrypt
        securePrefs.edit().putString("pin_hash", pin.hashCode().toString()).apply()
    }

    fun verifyPIN(pin: String): Boolean {
        val storedHash = securePrefs.getString("pin_hash", null) ?: return false
        return pin.hashCode().toString() == storedHash
    }

    // ── Biometric ────────────────────────────────────────────────

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isBiometricEnabled(): Boolean =
        securePrefs.getBoolean("biometric_enabled", false)

    fun setBiometricEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    /**
     * Show biometric prompt. Must be called from a FragmentActivity.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                // Don't surface individual failures — the prompt handles retries
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock MedVault")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setNegativeButtonText("Use PIN")
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
