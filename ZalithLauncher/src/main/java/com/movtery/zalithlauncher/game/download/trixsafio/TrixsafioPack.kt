/*
 * Gustambo Client
 * Basado en Zalith Launcher 2 (GPL-3.0).
 */

package com.movtery.zalithlauncher.game.download.trixsafio

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.getVersionIconFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private const val TAG = "TrixsafioUpdater"

/** Dónde vive trixsafio.json (raíz del repo, rama main). Cámbialo si lo alojas en otro sitio. */
const val TRIXSAFIO_MANIFEST_URL =
    "https://raw.githubusercontent.com/trixito/Gustambo-Launcher/main/trixsafio.json"

/** Lo que publicas tú en el repo: versión actual del pack y de dónde descargarlo. */
@Keep
data class TrixsafioManifest(
    @SerializedName("id") val id: String = "trixsafio",
    @SerializedName("name") val name: String = "Trixsafio",
    /** Cualquier texto; si es distinto al instalado, hay actualización. */
    @SerializedName("version") val version: String = "",
    @SerializedName("minecraft") val minecraft: String = "",
    @SerializedName("fabricLoader") val fabricLoader: String = "",
    @SerializedName("mrpackUrls") val mrpackUrls: List<String> = emptyList(),
    @SerializedName("sha1") val sha1: String? = null,
    @SerializedName("size") val size: Long = 0L,
    @SerializedName("changelog") val changelog: String? = null
)

/** Se guarda dentro de la instancia: .gustambo/pack.json */
@Keep
data class TrixsafioState(
    @SerializedName("id") val id: String,
    @SerializedName("version") val version: String,
    @SerializedName("minecraft") val minecraft: String,
    @SerializedName("fabricLoader") val fabricLoader: String,
    /** ruta relativa -> sha1 de cada archivo que el pack instaló */
    @SerializedName("files") val files: Map<String, String>
)

sealed interface TrixsafioStatus {
    data object Checking : TrixsafioStatus
    data class NotInstalled(val manifest: TrixsafioManifest) : TrixsafioStatus
    data class UpToDate(val version: String) : TrixsafioStatus
    data class UpdateAvailable(val from: String, val manifest: TrixsafioManifest) : TrixsafioStatus
    /** Cambió Minecraft o Fabric: no se puede actualizar en sitio. */
    data class NeedsReinstall(val manifest: TrixsafioManifest, val reason: String) : TrixsafioStatus
    data class Failed(val error: Throwable) : TrixsafioStatus
}

private sealed interface Source {
    val sha1: String
    class Zip(val entry: ZipEntry, override val sha1: String) : Source
    class Remote(val urls: List<String>, override val sha1: String) : Source
}

object TrixsafioUpdater {
    private const val STATE_DIR = ".gustambo"
    private const val STATE_FILE = "pack.json"

    /** Archivos del jugador que nunca se pisan si ya existen. */
    private val PRESERVE = setOf("options.txt", "optionsof.txt", "optionsshaders.txt", "servers.dat")

    // ---------- estado en la instancia ----------

    private fun stateFile(gameDir: File) = File(File(gameDir, STATE_DIR), STATE_FILE)

    fun readState(gameDir: File): TrixsafioState? = runCatching {
        stateFile(gameDir).takeIf { it.isFile }?.readText()
            ?.let { GSON.fromJson(it, TrixsafioState::class.java) }
    }.getOrNull()

    private fun writeState(gameDir: File, state: TrixsafioState) {
        val file = stateFile(gameDir)
        file.parentFile?.mkdirs()
        file.writeText(GSON.toJson(state))
    }

    /** Busca la instancia de Trixsafio por su archivo de estado (no por el nombre, que el jugador puede cambiar). */
    fun findInstance(id: String): Pair<Version, TrixsafioState>? =
        VersionsManager.versions.value.firstNotNullOfOrNull { v ->
            readState(v.getGameDir())?.takeIf { it.id == id }?.let { v to it }
        }

    // ---------- comprobar ----------

    suspend fun fetchManifest(): TrixsafioManifest = withContext(Dispatchers.IO) {
        val m = GSON.fromJson(fetchStringFromUrl(TRIXSAFIO_MANIFEST_URL), TrixsafioManifest::class.java)
        require(m.version.isNotBlank() && m.mrpackUrls.isNotEmpty()) { "trixsafio.json incompleto" }
        m
    }

    suspend fun check(): TrixsafioStatus {
        val manifest = fetchManifest()
        val (_, state) = findInstance(manifest.id) ?: return TrixsafioStatus.NotInstalled(manifest)
        if (state.version == manifest.version) return TrixsafioStatus.UpToDate(state.version)
        if (state.minecraft != manifest.minecraft || state.fabricLoader != manifest.fabricLoader) {
            return TrixsafioStatus.NeedsReinstall(
                manifest,
                "La versión ${manifest.version} cambia Minecraft o Fabric (${manifest.minecraft} / ${manifest.fabricLoader}). Hay que reinstalar."
            )
        }
        return TrixsafioStatus.UpdateAvailable(state.version, manifest)
    }

    // ---------- descargar ----------

    suspend fun downloadPack(manifest: TrixsafioManifest, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            val out = File(File(PathManager.DIR_CACHE, "trixsafio"), "${manifest.id}-${manifest.version}.mrpack")
            out.parentFile?.mkdirs()
            var done = 0L
            downloadFileFromSources(
                urls = manifest.mrpackUrls,
                outputFile = out,
                sha1 = manifest.sha1
            ) { n ->
                done += n
                onProgress(if (manifest.size > 0) (done.toDouble() / manifest.size).toFloat().coerceIn(0f, 1f) else -1f)
            }
            out
        }

    /** Tras la primera instalación (hecha por el importador de Zalith) anota qué archivos puso el pack. */
    suspend fun writeInitialState(context: Context, versionName: String, pack: File, manifest: TrixsafioManifest) =
        withContext(Dispatchers.IO) {
            val gameDir = File(getVersionsHome(), versionName)
            installIcon(context, gameDir)
            val files = ZipFile(pack).use { zip -> readDesired(zip).mapValues { it.value.sha1 } }
            writeState(gameDir, TrixsafioState(manifest.id, manifest.version, manifest.minecraft, manifest.fabricLoader, files))
        }

    /** Copia el logo de Trixsafio (assets/trixsafio/icon.png) como icono de la instancia. */
    fun installIcon(context: Context, versionFolder: File) {
        runCatching {
            val icon = getVersionIconFile(versionFolder)
            icon.parentFile?.mkdirs()
            context.assets.open("trixsafio/icon.png").use { input ->
                icon.outputStream().use { output -> input.copyTo(output) }
            }
        }.onFailure { Logger.warning(TAG, "No se pudo poner el icono", it) }
    }

    // ---------- actualizar en sitio ----------

    suspend fun update(
        version: Version,
        manifest: TrixsafioManifest,
        onStatus: (label: String, progress: Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val gameDir = version.getGameDir().canonicalFile
        val oldState = readState(gameDir) ?: throw IllegalStateException("La instancia no tiene datos de Trixsafio")

        onStatus("Descargando actualización", 0f)
        val pack = downloadPack(manifest) { onStatus("Descargando actualización", it) }
        try {
            ZipFile(pack).use { zip ->
                onStatus("Comparando archivos", -1f)
                val desired = readDesired(zip)
                val total = desired.size.coerceAtLeast(1)
                var index = 0

                for ((rel, src) in desired) {
                    ensureActive()
                    index++
                    onStatus("Actualizando archivos ($index/$total)", index.toFloat() / total)

                    val target = resolveInside(gameDir, rel)
                    if (rel in PRESERVE && target.exists()) continue

                    val needsCopy = if (rel.startsWith("mods/")) {
                        // mods: siempre deben coincidir con el pack (también repara jars dañados)
                        !isUpToDate(target, src)
                    } else {
                        // configs, resourcepacks, etc.: solo si el pack cambió ese archivo o no existe
                        !target.isFile || oldState.files[rel] != src.sha1
                    }
                    if (!needsCopy) continue

                    target.parentFile?.mkdirs()
                    when (src) {
                        is Source.Zip -> {
                            val tmp = File(target.path + ".gustambo_tmp")
                            zip.getInputStream(src.entry).use { input ->
                                tmp.outputStream().use { output -> input.copyTo(output) }
                            }
                            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                        is Source.Remote -> downloadFileFromSources(src.urls, target, src.sha1)
                    }
                }

                // borrar lo que el pack anterior instaló y el nuevo ya no incluye
                onStatus("Limpiando mods viejos", -1f)
                for (rel in oldState.files.keys) {
                    if (rel in desired || rel in PRESERVE) continue
                    runCatching { resolveInside(gameDir, rel).takeIf { it.isFile }?.delete() }
                        .onFailure { Logger.warning(TAG, "No se pudo borrar $rel", it) }
                }

                writeState(
                    gameDir,
                    TrixsafioState(
                        manifest.id, manifest.version, manifest.minecraft, manifest.fabricLoader,
                        desired.mapValues { it.value.sha1 }
                    )
                )
            }
            Logger.info(TAG, "Instancia actualizada a ${manifest.version}")
        } finally {
            pack.delete()
        }
    }

    // ---------- utilidades ----------

    /** Lee qué debe haber en la instancia: `files` del index (descargas) + overrides del zip. */
    private fun readDesired(zip: ZipFile): Map<String, Source> {
        val map = LinkedHashMap<String, Source>()

        zip.getEntry("modrinth.index.json")?.let { e ->
            val text = zip.getInputStream(e).bufferedReader().use { it.readText() }
            JsonParser.parseString(text).asJsonObject.getAsJsonArray("files")?.forEach { el ->
                val o = el.asJsonObject
                if (o.getAsJsonObject("env")?.get("client")?.asString == "unsupported") return@forEach
                val path = o.get("path").asString
                val sha1 = o.getAsJsonObject("hashes")?.get("sha1")?.asString ?: return@forEach
                val urls = o.getAsJsonArray("downloads").map { it.asString }
                map[path] = Source.Remote(urls, sha1)
            }
        }

        // client-overrides va después para que gane sobre overrides
        for (prefix in listOf("overrides/", "client-overrides/")) {
            for (entry in zip.entries().asSequence()) {
                if (entry.isDirectory || !entry.name.startsWith(prefix)) continue
                map[entry.name.removePrefix(prefix)] = Source.Zip(entry, sha1Of(zip, entry))
            }
        }
        return map
    }

    private fun isUpToDate(target: File, src: Source): Boolean {
        if (!target.isFile) return false
        if (src is Source.Zip && src.entry.size >= 0 && target.length() != src.entry.size) return false
        return sha1Of(target).equals(src.sha1, ignoreCase = true)
    }

    /** Evita rutas tipo ../../ dentro del zip o del estado. */
    private fun resolveInside(root: File, rel: String): File {
        val f = File(root, rel).canonicalFile
        require(f.path.startsWith(root.path + File.separator)) { "Ruta inválida: $rel" }
        return f
    }

    private fun sha1Of(zip: ZipFile, entry: ZipEntry): String =
        zip.getInputStream(entry).use { digest(it) }

    private fun sha1Of(file: File): String = file.inputStream().use { digest(it) }

    private fun digest(input: java.io.InputStream): String {
        val md = MessageDigest.getInstance("SHA-1")
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = input.read(buf)
            if (n <= 0) break
            md.update(buf, 0, n)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
