/*
 * Gustambo Client
 * Basado en Zalith Launcher 2 (GPL-3.0).
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.download.trixsafio.TrixsafioController
import com.movtery.zalithlauncher.game.download.trixsafio.TrixsafioStatus
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel

/** Tarjeta "Trixsafio": instalar, ver estado y actualizar. */
@Composable
fun TrixsafioCard(
    modpackImportViewModel: ModpackImportViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    //cada vez que cambia la lista de instancias, se vuelve a comprobar
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    LaunchedEffect(versions) { TrixsafioController.refresh() }

    val status = TrixsafioController.status
    val work = TrixsafioController.work

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = launcherCardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Trixsafio", style = MaterialTheme.typography.titleMedium)
            Text(text = statusText(status), style = MaterialTheme.typography.bodyMedium)

            if (work != null) {
                Text(text = work.label, style = MaterialTheme.typography.bodySmall)
                if (work.progress in 0f..1f) {
                    LinearProgressIndicator(progress = { work.progress }, modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                OutlinedButton(onClick = { TrixsafioController.cancel() }) { Text("Cancelar") }
            } else {
                when (status) {
                    is TrixsafioStatus.NotInstalled -> Button(
                        onClick = { TrixsafioController.install(context, status.manifest, modpackImportViewModel) }
                    ) { Text("Instalar Trixsafio") }

                    is TrixsafioStatus.UpdateAvailable -> Button(
                        onClick = { TrixsafioController.update(status.manifest) }
                    ) { Text("Actualizar a ${status.manifest.version}") }

                    is TrixsafioStatus.NeedsReinstall -> Button(
                        onClick = { TrixsafioController.install(context, status.manifest, modpackImportViewModel) }
                    ) { Text("Reinstalar (instancia nueva)") }

                    is TrixsafioStatus.Failed -> OutlinedButton(
                        onClick = { TrixsafioController.refresh() }
                    ) { Text("Reintentar") }

                    else -> {}
                }
            }
        }
    }
}

private fun statusText(status: TrixsafioStatus): String = when (status) {
    is TrixsafioStatus.Checking -> "Buscando actualizaciones…"
    is TrixsafioStatus.NotInstalled -> "No instalado · versión ${status.manifest.version}"
    is TrixsafioStatus.UpToDate -> "Al día · versión ${status.version}"
    is TrixsafioStatus.UpdateAvailable -> "Actualización disponible: ${status.from} → ${status.manifest.version}"
    is TrixsafioStatus.NeedsReinstall -> status.reason
    is TrixsafioStatus.Failed -> "No se pudo comprobar: ${status.error.localizedMessage ?: status.error::class.simpleName}"
}
