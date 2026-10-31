> **Gustambo Client** is an unofficial, modified version of [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2), made for Trixsafio events. It is not affiliated with or endorsed by the original authors. Licensed under GPL-3.0.
>
> **Gustambo Client** es una versión no oficial y modificada de [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2), hecha para los eventos de Trixsafio. No está afiliada ni respaldada por los autores originales. Licencia GPL-3.0.

---

# Gustambo Client

Launcher de [Minecraft: Java Edition](https://www.minecraft.net/) para **Android**, creado por **Gustambo Studio** para los eventos y servidores de **Trixsafio**.

Es un fork de [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2): usa [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) como núcleo de arranque y está construido con **Jetpack Compose** y **Material Design 3**.

> [!IMPORTANT]
> Este proyecto **no es** el Zalith Launcher oficial. Para el launcher original visita [ZalithLauncher/ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2).

## ✨ Qué lo diferencia

* **Instancia de Trixsafio:** crea la instancia del proyecto con un solo botón (Minecraft, loader y mods ya configurados).
* **Sistema de actualización:** descarga mods nuevos o corregidos sin reinstalar nada.

> Estas funciones están en desarrollo.

## 📦 Cómo compilar (desarrolladores)

### Requisitos

* Android Studio Bumblebee o superior
* Android SDK:
    * **API mínima**: 26
    * **API objetivo**: 35
* JDK 11

### Pasos

```bash
git clone git@github.com:trixito/Gustambo-Launcher.git
# Abre el proyecto con Android Studio y compílalo
```

## 🌐 Traducciones

Las traducciones base vienen del proyecto original en [Weblate](https://hosted.weblate.org/projects/zalithlauncher2). Gracias a todos los colaboradores de idiomas del proyecto original.

## 📜 Licencia

Este proyecto se distribuye bajo la licencia **[GPL-3.0](LICENSE)**.

### Términos adicionales (según la sección 7 de la GPLv3)

1. Al distribuir una versión modificada debes cambiar de forma razonable el nombre o la versión del programa para distinguirla de la original ([GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374)).
   * Las versiones modificadas **no pueden incluir en su nombre "ZalithLauncher" ni su abreviatura "ZL"**, ni usar nombres parecidos al oficial que puedan causar confusión.
   * Toda versión modificada **debe indicar de forma visible, en la pantalla de inicio o en la interfaz principal, que es una "versión no oficial modificada"**.
   * El nombre de la app se cambia en [gradle.properties](./ZalithLauncher/gradle.properties).

2. No se pueden eliminar los avisos de copyright que muestra el programa ([GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370)).

## 🙏 Créditos

Basado en [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2) de sus autores originales.

## Proyectos de código abierto utilizados

Este software usa las siguientes librerías:

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                    |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|----------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [link↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [link↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [link↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [link↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [link↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [link↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [link↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [link↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Apache 2.0           | [link↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [link↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [link↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [link↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [link↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [link↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [link↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [link↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [link↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [link↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [link↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [link↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Mesa                                  | Copyright © The Mesa Authors                                                                                  | MIT License          | [link↗](https://mesa3d.org/)                                                       |
| MobileGlues                           | Copyright © 2025-2026 MobileGL-Dev                                                                            | LGPL-2.1 License     | [link↗](https://github.com/MobileGL-Dev/MobileGlues)                               |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NG-GL4ES                              | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License          | [link↗](https://github.com/BZLZHH/NG-GL4ES)                                        |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [link↗](https://square.github.io/okio/)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [link↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [link↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [link↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [link↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [link↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [link↗](https://github.com/libsdl-org/SDL)                                         |
| SimpleFPEWrapper                      | Copyright © 2026 MobileGL-Dev                                                                                 | LGPL-3.0 License     | [link↗](https://github.com/MobileGL-Dev/SimpleFPEWrapper)                          |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [link↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [link↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [link↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [link↗](https://tukaani.org/xz/java.html)                                          |
