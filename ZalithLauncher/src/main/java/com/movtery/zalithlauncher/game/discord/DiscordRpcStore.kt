package com.movtery.zalithlauncher.game.discord

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gustambo: guarda el estado de Discord Rich Presence en archivos privados de la app.
 * Se usan archivos (y no MMKV) para que el proceso ":game" pueda leerlos sin problemas.
 * El token se cifra con una clave del Android Keystore (AES-GCM).
 */
object DiscordRpcStore {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "gustambo_discord_rpc"

    private fun dir(ctx: Context): File =
        File(ctx.filesDir, "discord_rpc").also { it.mkdirs() }

    fun isEnabled(ctx: Context): Boolean = File(dir(ctx), "enabled").exists()

    fun setEnabled(ctx: Context, value: Boolean) {
        val file = File(dir(ctx), "enabled")
        if (value) file.writeText("1") else file.delete()
    }

    fun hasToken(ctx: Context): Boolean = File(dir(ctx), "token").exists()

    fun clearToken(ctx: Context) {
        File(dir(ctx), "token").delete()
    }

    fun saveToken(ctx: Context, token: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val data = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(encrypted, Base64.NO_WRAP)
        File(dir(ctx), "token").writeText(data)
    }

    fun readToken(ctx: Context): String? {
        return try {
            val file = File(dir(ctx), "token")
            if (!file.exists()) return null
            val (ivB64, dataB64) = file.readText().split(":")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                GCMParameterSpec(128, Base64.decode(ivB64, Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(dataB64, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
