/*
 * Gustambo Studio
 * Basado en Zalith Launcher 2 (GPL-3.0).
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.download.trixsafio.TrixsafioController
import com.movtery.zalithlauncher.game.download.trixsafio.TrixsafioStatus
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel

/** Un evento del launcher: aparece como icono en la barra lateral. */
data class GustamboEvent(
    val id: String,
    val name: String,
    val tagline: String
)

/**
 * Lista de eventos que se muestran en la barra lateral.
 * Por ahora todos usan TrixsafioController; cuando haya varios eventos, cada uno tendrá su propio estado.
 */
object GustamboEvents {
    val all = listOf(
        GustamboEvent(id = "trixsafio", name = "Trixsafio", tagline = "Evento")
    )
}

/**
 * Inicio del launcher de eventos (estilo barra lateral):
 * - barra lateral: Inicio (logo), un icono por evento y la cuenta abajo
 * - panel principal: marca de Gustambo Studio, o la página del evento elegido con su botón JUGAR
 */
@Composable
fun EventsHome(
    isVisible: Boolean,
    modpackImportViewModel: ModpackImportViewModel,
    onPlay: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)
    //null = Inicio
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    //cada vez que cambia la lista de instancias, se vuelve a comprobar
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    LaunchedEffect(versions) { TrixsafioController.refresh() }
    //instancia instalada del evento: su icono se usa como logo del evento
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val eventVersion = currentVersion?.takeIf { it.isValid() }

    Row(
        modifier = modifier
            .fillMaxSize()
            .animated(ctx)
    ) {
        EventsRail(
            eventVersion = eventVersion,
            selectedId = selectedId,
            onSelect = { selectedId = it },
            onOpenAccount = onOpenAccount
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val event = GustamboEvents.all.firstOrNull { it.id == selectedId }
            if (event == null) {
                BrandPane(
                    title = "GUSTAMBO",
                    subtitle = "STUDIO",
                    hint = "Elige un evento en la barra de la izquierda",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                BrandPane(
                    title = event.name.uppercase(),
                    subtitle = event.tagline.uppercase(),
                    hint = null,
                    version = eventVersion,
                    modifier = Modifier.fillMaxSize()
                )
                EventControls(
                    modpackImportViewModel = modpackImportViewModel,
                    onPlay = onPlay,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun EventsRail(
    eventVersion: Version?,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(84.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Inicio: el logo de la rata
        RailTile(selected = selectedId == null, onClick = { onSelect(null) }) {
            Image(
                painter = painterResource(R.drawable.img_launcher),
                contentDescription = "Inicio",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(48.dp),
            color = MaterialTheme.colorScheme.outline
        )

        //un icono por evento
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GustamboEvents.all.forEach { event ->
                RailTile(
                    selected = selectedId == event.id,
                    onClick = { onSelect(event.id) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (eventVersion != null) {
                        VersionIconImage(
                            version = eventVersion,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        //sin instancia instalada todavía: inicial del evento
                        Text(
                            text = event.name.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        )
                    }
                }
            }
        }

        //cuenta
        RailTile(selected = false, onClick = onOpenAccount) {
            val acc = account
            if (acc != null) {
                PlayerFace(account = acc, avatarSize = 56.dp)
            } else {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RailTile(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(shape)
            .background(containerColor)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** Marca grande centrada: logo + título (como el logo de un estudio). */
@Composable
private fun BrandPane(
    title: String,
    subtitle: String,
    hint: String?,
    modifier: Modifier = Modifier,
    version: Version? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (version != null) {
                    //logo del evento = icono de su instancia
                    VersionIconImage(
                        version = version,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.img_launcher),
                        contentDescription = null,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Light,
                        fontSize = 40.sp,
                        maxLines = 1
                    )
                }
            }
            if (hint != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Estado + botón grande del evento: INSTALAR / ACTUALIZAR / JUGAR, con progreso. */
@Composable
private fun EventControls(
    modpackImportViewModel: ModpackImportViewModel,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val status = TrixsafioController.status
    val work = TrixsafioController.work

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = eventStatusText(status),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (work != null) {
            Text(text = work.label, style = MaterialTheme.typography.bodySmall)
            if (work.progress in 0f..1f) {
                LinearProgressIndicator(progress = { work.progress }, modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            OutlinedButton(onClick = { TrixsafioController.cancel() }) { Text("Cancelar") }
            return@Column
        }

        val hasValidInstance = version?.isValid() == true
        when (status) {
            is TrixsafioStatus.NotInstalled -> BigButton("INSTALAR") {
                TrixsafioController.install(context, status.manifest, modpackImportViewModel)
            }

            is TrixsafioStatus.NeedsReinstall -> BigButton("REINSTALAR") {
                TrixsafioController.install(context, status.manifest, modpackImportViewModel)
            }

            is TrixsafioStatus.UpdateAvailable -> {
                OutlinedButton(onClick = { TrixsafioController.update(status.manifest) }) {
                    Text("Actualizar a ${status.manifest.version}")
                }
                BigButton("JUGAR", onClick = onPlay)
            }

            is TrixsafioStatus.UpToDate -> BigButton("JUGAR", onClick = onPlay)

            is TrixsafioStatus.Checking -> BigButton("COMPROBANDO…", enabled = false) {}

            is TrixsafioStatus.Failed -> {
                if (hasValidInstance) {
                    OutlinedButton(onClick = { TrixsafioController.refresh() }) { Text("Reintentar") }
                    BigButton("JUGAR", onClick = onPlay)
                } else {
                    BigButton("REINTENTAR") { TrixsafioController.refresh() }
                }
            }
        }
    }
}

@Composable
private fun BigButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ScalingActionButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        onClick = onClick
    ) {
        Text(text = text, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
    }
}

private fun eventStatusText(status: TrixsafioStatus): String = when (status) {
    is TrixsafioStatus.Checking -> "Buscando actualizaciones…"
    is TrixsafioStatus.NotInstalled -> "No instalado · versión ${status.manifest.version}"
    is TrixsafioStatus.UpToDate -> "Al día · versión ${status.version}"
    is TrixsafioStatus.UpdateAvailable -> "Actualización disponible: ${status.from} → ${status.manifest.version}"
    is TrixsafioStatus.NeedsReinstall -> status.reason
    is TrixsafioStatus.Failed -> "No se pudo comprobar: ${status.error.localizedMessage ?: status.error::class.simpleName}"
}
