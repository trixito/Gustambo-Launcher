package com.movtery.zalithlauncher.game.discord

import android.content.Context
import com.movtery.zalithlauncher.utils.logging.Logger
import com.my.kizzyrpc.KizzyRPC
import com.my.kizzyrpc.model.Assets
import com.my.kizzyrpc.model.Timestamps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.my.kizzyrpc.model.Activity as RpcActivity

private const val TAG = "DiscordRpc"

/**
 * Gustambo: muestra "Jugando Trixsafio" en el perfil de Discord mientras el juego corre.
 * Se inicia y se detiene desde GameService (proceso ":game").
 */
object DiscordRpcController {
    private const val APPLICATION_ID = "1566677126986473534"
    private const val LARGE_IMAGE = "logo"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var rpc: KizzyRPC? = null

    fun start(ctx: Context) {
        if (rpc != null) return
        if (!DiscordRpcStore.isEnabled(ctx)) return
        val token = DiscordRpcStore.readToken(ctx) ?: return

        val client = KizzyRPC(token)
        rpc = client

        scope.launch {
            try {
                client.setActivity(
                    activity = RpcActivity(
                        applicationId = APPLICATION_ID,
                        name = "Trixsafio",
                        details = "Jugando Trixsafio",
                        state = "En el servidor",
                        type = 0,
                        timestamps = Timestamps(start = System.currentTimeMillis()),
                        assets = Assets(
                            largeImage = LARGE_IMAGE,
                            largeText = "Trixsafio"
                        )
                    ),
                    status = "online",
                    since = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to set Discord presence", e)
            }
        }
    }

    fun stop() {
        try {
            rpc?.closeRPC()
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to close Discord presence", e)
        }
        rpc = null
    }
}
