/*
 * Gustambo Client
 * Basado en Zalith Launcher 2 (GPL-3.0).
 */

package com.movtery.zalithlauncher.game.download.trixsafio

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TrixsafioController"

data class TrixsafioWork(val label: String, /** 0..1, o -1 si es indeterminado */ val progress: Float)

/** Estado global de Trixsafio (singleton, igual que VersionsManager). */
object TrixsafioController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var status by mutableStateOf<TrixsafioStatus>(TrixsafioStatus.Checking)
        private set
    var work by mutableStateOf<TrixsafioWork?>(null)
        private set

    private var checkJob: Job? = null
    private var workJob: Job? = null

    fun refresh() {
        if (work != null) return
        checkJob?.cancel()
        checkJob = scope.launch {
            status = try {
                withContext(Dispatchers.IO) { TrixsafioUpdater.check() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.error(TAG, "No se pudo comprobar Trixsafio", e)
                TrixsafioStatus.Failed(e)
            }
        }
    }

    /** Primera instalación: descarga el .mrpack y se lo pasa al importador de Zalith. */
    fun install(context: Context, manifest: TrixsafioManifest, importVm: ModpackImportViewModel) {
        if (workJob?.isActive == true) return
        workJob = scope.launch {
            try {
                work = TrixsafioWork("Descargando ${manifest.name}", -1f)
                val pack = TrixsafioUpdater.downloadPack(manifest) { p ->
                    work = TrixsafioWork("Descargando ${manifest.name}", p)
                }
                work = null
                importVm.import(
                    context = context,
                    uri = Uri.fromFile(pack),
                    onImported = { versionName ->
                        scope.launch {
                            runCatching { TrixsafioUpdater.writeInitialState(context, versionName, pack, manifest) }
                                .onFailure { Logger.error(TAG, "No se pudo guardar el estado inicial", it) }
                            withContext(Dispatchers.IO) { pack.delete() }
                            refresh()
                        }
                    }
                )
            } catch (e: CancellationException) {
                work = null
                throw e
            } catch (e: Throwable) {
                Logger.error(TAG, "Falló la instalación", e)
                work = null
                status = TrixsafioStatus.Failed(e)
            }
        }
    }

    /** Actualiza la instancia existente sin reinstalar. */
    fun update(manifest: TrixsafioManifest) {
        if (workJob?.isActive == true) return
        workJob = scope.launch {
            try {
                val (version, _) = TrixsafioUpdater.findInstance(manifest.id)
                    ?: throw IllegalStateException("No se encontró la instancia de ${manifest.name}")
                work = TrixsafioWork("Preparando…", -1f)
                TrixsafioUpdater.update(version, manifest) { label, p -> work = TrixsafioWork(label, p) }
                work = null
                refresh()
            } catch (e: CancellationException) {
                work = null
                throw e
            } catch (e: Throwable) {
                Logger.error(TAG, "Falló la actualización", e)
                work = null
                status = TrixsafioStatus.Failed(e)
            }
        }
    }

    fun cancel() {
        workJob?.cancel()
        work = null
    }
}
