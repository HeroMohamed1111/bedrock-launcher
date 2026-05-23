# BedrockLaunch — Minecraft Bedrock Launcher for Android

A feature-rich Android launcher for Minecraft Bedrock Edition, inspired by [LeviLaunchroid](https://github.com/LiteLDev/LeviLaunchroid). Written in Kotlin with MVVM architecture.

---

## Features

| Feature | Description |
|---|---|
| **Launcher** | Detects Minecraft Bedrock, shows version info, launches with one tap |
| **Profiles** | Multiple named profiles / config sets with per-profile notes |
| **Pack Manager** | Scans installed resource & behaviour packs; toggle on/off |
| **Mod Browser** | Browse Modrinth + CurseForge addons, search, paginate, bookmark |
| **One-tap Download** | Downloads queued to background service with progress notifications |
| **Server Browser** | Curated public Bedrock servers with ping display and favourites |
| **Custom Servers** | Add any server by host:port |
| **News Feed** | Live news from Minecraft.net (swipe-to-refresh) |
| **Dark / Light Mode** | Full Material Design 3 dynamic theming |
| **Smooth Animations** | Slide transitions between screens, animated launcher icon |

---

## Architecture

```
app/src/main/kotlin/com/bedrocklaunch/
├── BedrockLaunchApp.kt          Application + Hilt + WorkManager init
├── model/                       Pure Kotlin data models (no Android deps)
├── data/
│   ├── AppDatabase.kt           Room database (single source of truth)
│   ├── dao/                     DAO interfaces (profiles, servers, downloads, mods, packs)
│   └── entity/                  Room entities
├── api/
│   ├── modrinth/                Retrofit interface + response models (Modrinth v2 API)
│   └── curseforge/              Retrofit interface + response models (CurseForge v1 API)
├── repository/                  Single source of truth per domain
│   ├── MinecraftRepository.kt   MC detection, launch, profiles, pack scan
│   ├── ModRepository.kt         Modrinth + CurseForge search, bookmarks
│   ├── ServerRepository.kt      Public server list, favourites, ping
│   ├── DownloadRepository.kt    Download queue via Room
│   └── NewsRepository.kt        Minecraft.net news feed
├── viewmodel/                   AndroidViewModel subclasses (Hilt-injected)
├── ui/
│   ├── MainActivity.kt          Single-activity Navigation Component host
│   ├── launcher/                LauncherFragment + ProfileAdapter + PackAdapter
│   ├── mods/                    ModsFragment + ModAdapter + ModDetailFragment
│   ├── servers/                 ServersFragment + ServerAdapter
│   ├── downloads/               DownloadsFragment + DownloadAdapter
│   └── news/                    NewsFragment + NewsAdapter
├── service/
│   └── DownloadService.kt       Foreground service — processes download queue
├── di/
│   └── AppModule.kt             Hilt module (Room, Retrofit, OkHttp, Gson)
└── util/
    ├── MinecraftDetector.kt     PackageManager queries + intent launch
    └── PackScanner.kt           Scans MC data directory for packs
```

**Stack:**
- Language: Kotlin
- Architecture: MVVM + Repository pattern
- DI: Hilt
- DB: Room (SQLite)
- Networking: Retrofit 2 + OkHttp 4
- Image loading: Glide 4
- Background tasks: WorkManager + Foreground Service
- UI: Material Design 3, Navigation Component, ViewBinding
- Min SDK: 26 (Android 8.0) | Target SDK: 34

---

## Building from source

### Prerequisites

| Tool | Version |
|---|---|
| JDK | 17+ |
| Android SDK | Build Tools 34, Platform 34 |
| Gradle | 8.4 (wrapper included) |

### Steps

```bash
# 1. Clone / extract the project
cd bedrock-launcher

# 2. Set ANDROID_HOME (or edit local.properties)
export ANDROID_HOME=/path/to/android-sdk

# 3. Build debug APK
./gradlew assembleDebug

# 4. Find the APK
ls app/build/outputs/apk/debug/app-debug.apk
```

### Android Studio

1. Open the `bedrock-launcher/` folder in Android Studio Hedgehog or newer.
2. Wait for Gradle sync to complete.
3. Click **Run ▶** or **Build → Build APK**.

---

## CurseForge API Key

The bundled key in `AppModule.kt` is a placeholder. For production use, register at
[console.curseforge.com](https://console.curseforge.com) and replace the key in
`di/AppModule.kt` → `provideCurseForgeOkHttpClient()`.

---

## Permissions

| Permission | Why |
|---|---|
| INTERNET | API calls and downloads |
| QUERY_ALL_PACKAGES | Detect Minecraft installation |
| READ/WRITE_EXTERNAL_STORAGE | Access Minecraft pack directories |
| POST_NOTIFICATIONS | Download progress |
| FOREGROUND_SERVICE | Background downloads |
| REQUEST_INSTALL_PACKAGES | Sideload packs / APKs |

---

## Roadmap / TODO

- [ ] Backup & restore packs to ZIP
- [ ] Auto-update launcher components via GitHub Releases
- [ ] Asset-cache cleaner
- [ ] Mod dependency resolution
- [ ] Dedicated settings screen (mod sources, download path, theme)
- [ ] Real UDP ping for Bedrock servers (port 19132)
- [ ] World browser / WorldBackup support
- [ ] i18n / localisation
