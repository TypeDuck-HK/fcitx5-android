# External Integrations

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## APIs & External Services

**Android Platform APIs:**
- Input Method Framework - Main runtime integration for the keyboard service.
  - SDK/Client: Android framework `InputMethodService`
  - Implementation: `app/src/main/AndroidManifest.xml`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
  - Auth: `android.permission.BIND_INPUT_METHOD`
- Android DocumentsProvider - Exposes app data files to the system file picker.
  - SDK/Client: Android framework `DocumentsProvider`
  - Implementation: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
  - Auth: `android.permission.MANAGE_DOCUMENTS` in `app/src/main/AndroidManifest.xml`
- Android content/file intents - Main activity imports `.dict`, `.scel`, and `.txt` files from `file` and `content` URIs.
  - SDK/Client: Android `Intent` filters
  - Implementation: `app/src/main/AndroidManifest.xml`
  - Auth: System-granted URI access
- Android system clipboard - Clipboard history listens to primary clip changes.
  - SDK/Client: Android `ClipboardManager`
  - Implementation: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`
  - Auth: Android clipboard access model; no external secret

**Build-Time Package Sources:**
- Google Maven, Maven Central, Gradle Plugin Portal - Gradle dependency resolution.
  - SDK/Client: Gradle repositories in `settings.gradle.kts`
  - Auth: None detected
- GitHub submodules - Native source/data dependencies for Fcitx5, libime, plugins, prebuilt binaries, Rime data, and ClearURLs rules.
  - SDK/Client: Git submodules in `.gitmodules`
  - Auth: Public GitHub access by default
- GitHub Packages - Publishes build logic and reusable Android libraries.
  - SDK/Client: Gradle `maven-publish`
  - Implementation: `build-logic/convention/build.gradle.kts`, `lib/common/build.gradle.kts`, `lib/plugin-base/build.gradle.kts`, `.github/workflows/publish.yml`
  - Auth: `GITHUB_ACTOR`, `GITHUB_TOKEN`; CI maps `GITHUB_TOKEN` from `secrets.PACKAGE_TOKEN`

**CI/CD Services:**
- GitHub Actions - Pull request builds, publishing, F-Droid build verification, and Nix builds.
  - SDK/Client: Workflow YAML
  - Implementation: `.github/workflows/pull_request.yml`, `.github/workflows/publish.yml`, `.github/workflows/fdroid.yml`, `.github/workflows/nix.yml`
  - Auth: `GITHUB_TOKEN`, `PACKAGE_TOKEN`, `CACHIX_AUTH_TOKEN`
- Jenkins - F-Droid workflow pulls build metadata for the `fcitx5-android` job.
  - SDK/Client: `curl` in `.github/workflows/fdroid.yml`
  - Auth: None detected in repository workflow
- F-Droid buildserver - Containerized metadata/build validation.
  - SDK/Client: `registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie`
  - Implementation: `.github/workflows/fdroid.yml`, `app/org.fcitx.fcitx5.android.yml`
  - Toolchain: Workflow fills CMake, NDK, platform, prebuilder revision, and Rust placeholders from the pinned prebuilt submodule's `toolchain-versions.json`, using distro-installed `yq-go`.
  - Auth: GitHub token used only for GitHub API content lookup
- Cachix - Nix binary cache used in CI.
  - SDK/Client: `cachix/cachix-action@v17`
  - Implementation: `.github/workflows/nix.yml`
  - Auth: `CACHIX_AUTH_TOKEN`

**Localization:**
- Transifex - Translation source/target mapping for Android string resources and native PO files.
  - SDK/Client: Transifex config files
  - Implementation: `transifex.yml`; resource identifiers also appear in `transifex-config.ini`
  - Auth: Not stored in reviewed files; do not commit Transifex credentials

**Native Upstream/Data Sources:**
- Fcitx and plugin upstreams - Source submodules for core and plugins.
  - SDK/Client: Git submodules in `.gitmodules`
  - Auth: Public GitHub access
- Fcitx data downloads - Upstream libime CMake references downloadable language model and dictionary archives.
  - SDK/Client: CMake download variables in `lib/libime/src/main/cpp/libime/data/CMakeLists.txt`
  - Auth: None detected
- Optional cloud pinyin source - Upstream `fcitx5-chinese-addons` supports libcurl-backed cloud pinyin, but the Android wrapper sets `ENABLE_CLOUDPINYIN` to `OFF`.
  - SDK/Client: Upstream CMake option in `lib/fcitx5-chinese-addons/src/main/cpp/fcitx5-chinese-addons/CMakeLists.txt`
  - Android status: Disabled in `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`; not copied by `app/src/main/cpp/CMakeLists.txt`
  - Auth: Not applicable

## Data Storage

**Databases:**
- Android Room SQLite database for clipboard history.
  - Connection: Local on-device database name `clbdb`
  - Client: Room `databaseBuilder`
  - Implementation: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`
  - Versioning: Room database version 4 with auto migrations in `ClipboardDatabase.kt`

**File Storage:**
- App external files directory exposed through Android DocumentsProvider.
  - Implementation: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
  - Authority: `${applicationId}.provider` in `app/src/main/AndroidManifest.xml`
- App assets and native data are packaged from Fcitx install outputs.
  - Implementation: `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`, `app/build.gradle.kts`, `app/src/main/cpp/CMakeLists.txt`
- Android backup/data extraction rules are configured.
  - Implementation: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/full_backup_content.xml`

**Caching:**
- No external cache service detected.
- Local app caches/preferences are implemented with SharedPreferences and local files.
  - Implementation: `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/RecentlyUsed.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/theme/ThemeManager.kt`

## Authentication & Identity

**Auth Provider:**
- No external user authentication provider detected.
  - Implementation: Not applicable
- Plugin trust uses Android signature-level permissions.
  - Permissions: `${applicationId}.permission.IPC` and `${applicationId}.permission.PLUGIN`
  - Implementation: `app/src/main/AndroidManifest.xml`, `lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt`, `lib/common/src/main/java/org/fcitx/fcitx5/android/common/ipc/FcitxRemoteConnection.kt`
- Release signing uses local/CI signing material passed through Gradle properties or environment variables.
  - Implementation: `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`, `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`
  - Secret names: `SIGN_KEY_BASE64`, `SIGN_KEY_FILE`, `SIGN_KEY_PWD`, `SIGN_KEY_ALIAS`

## Monitoring & Observability

**Error Tracking:**
- None detected. No Sentry, Crashlytics, Firebase Analytics, or similar SDK appears in `gradle/libs.versions.toml` or the targeted source search.

**Logs:**
- App logging uses Timber.
  - SDK/Client: `com.jakewharton.timber:timber` 5.0.1 in `gradle/libs.versions.toml`
  - Implementation examples: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/Timber.kt`
- Native Android logging links against `log` in `app/src/main/cpp/CMakeLists.txt`.
- UI exposes log viewing through `app/src/main/java/org/fcitx/fcitx5/android/ui/main/LogActivity.kt` and `app/src/main/java/org/fcitx/fcitx5/android/ui/main/log/LogView.kt`.

## CI/CD & Deployment

**Hosting:**
- Source and issue workflows are hosted on GitHub.
  - Repository references: `README.md`, `.gitmodules`, `.github/workflows/*`
- Published Maven artifacts go to GitHub Packages.
  - Implementation: `build-logic/convention/build.gradle.kts`, `lib/common/build.gradle.kts`, `lib/plugin-base/build.gradle.kts`
- Distribution metadata targets F-Droid and Google Play.
  - F-Droid: `app/org.fcitx.fcitx5.android.yml`, `.github/workflows/fdroid.yml`
  - Google Play listing metadata: `app/src/main/play`
- CI build references Jenkins downloads in `README.md` and `.github/workflows/fdroid.yml`.

**CI Pipeline:**
- Pull request pipeline builds release APK and plugin APKs across Ubuntu, macOS Intel, macOS 26, and Windows.
  - Implementation: `.github/workflows/pull_request.yml`
- Publish pipeline publishes build convention and libraries on `master` changes.
  - Implementation: `.github/workflows/publish.yml`
- F-Droid pipeline validates F-Droid builds for all four supported ABIs.
  - Implementation: `.github/workflows/fdroid.yml`
- Nix pipeline builds app and plugins inside `nix develop .#noAS`.
  - Implementation: `.github/workflows/nix.yml`

## Environment Configuration

**Required env vars:**
- Build/tool overrides: `CMAKE_VERSION`, `NDK_VERSION`, `BUILD_TOOLS_VERSION`, `BUILD_VERSION_NAME`, `BUILD_COMMIT_HASH`, `BUILD_TIMESTAMP`, `BUILD_ABI` from `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`.
- Signing: `SIGN_KEY_BASE64`, `SIGN_KEY_FILE`, `SIGN_KEY_PWD`, `SIGN_KEY_ALIAS` from `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`.
- GitHub Packages publishing: `GITHUB_ACTOR`, `GITHUB_TOKEN` from `build-logic/convention/build.gradle.kts`, `lib/common/build.gradle.kts`, and `lib/plugin-base/build.gradle.kts`.
- CI-only secrets: `PACKAGE_TOKEN` in `.github/workflows/publish.yml`, `CACHIX_AUTH_TOKEN` in `.github/workflows/nix.yml`.
- F-Droid workflow input/environment: `BUILD_NUMBER` in `.github/workflows/fdroid.yml`.

**Secrets location:**
- GitHub Actions secrets are referenced by name in workflow files but values are not present in the repository.
- Local signing material may be provided via Gradle properties or environment variables handled by `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`.
- No `.env` or `.env.*` files were detected by filename scan.

## Webhooks & Callbacks

**Incoming:**
- GitHub Actions `repository_dispatch` triggers the F-Droid workflow.
  - Implementation: `.github/workflows/fdroid.yml`
- Android app receives launcher, settings, file/content import, and input method intents.
  - Implementation: `app/src/main/AndroidManifest.xml`
- Plugin IPC service receives `${applicationId}.IPC` actions protected by signature permission.
  - Implementation: `app/src/main/AndroidManifest.xml`, `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`

**Outgoing:**
- Runtime app: no general Internet permission or Retrofit/OkHttp/Firebase-style runtime network client detected in `app/src/main/AndroidManifest.xml`, `app/build.gradle.kts`, or targeted source search.
- CI: F-Droid workflow uses `curl` to fetch Jenkins metadata, GitHub raw files, GitHub API content, and prebuilt toolchain metadata; `yq-go` is installed through apt.
  - Implementation: `.github/workflows/fdroid.yml`
- Build/submodule setup: Git submodules fetch native source and data dependencies declared in `.gitmodules`.
- Localization: Transifex sync uses mappings in `transifex.yml`; execution/auth is external to the app runtime.

---

*Integration audit: 2026-11-17*
