# Technology Stack

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## Languages

**Primary:**
- Kotlin 2.4.10 - Android application, Android plugin APKs, build logic, KSP code generation, and tests; versions live in `gradle/libs.versions.toml`, app code lives under `app/src/main/java`, plugin code under `plugin/*/src/main/java`, and build logic under `build-logic/convention/src/main/kotlin`.
- C++ - Native Fcitx5 runtime, Android JNI bridge, input method modules, and native plugins; entry points include `app/src/main/cpp/CMakeLists.txt`, `lib/fcitx5/src/main/cpp/CMakeLists.txt`, `lib/libime/src/main/cpp/CMakeLists.txt`, and `plugin/*/src/main/cpp/CMakeLists.txt`.

**Secondary:**
- Java 11 bytecode target - Gradle convention code sets `JavaVersion.VERSION_11` in `build-logic/convention/src/main/kotlin/Versions.kt` and Kotlin `jvmTarget` in `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`.
- Gradle Kotlin DSL - Root build, module builds, and included build logic use `*.gradle.kts` files such as `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, and `build-logic/convention/build.gradle.kts`.
- Python 3 - Development shell dependency for native tooling in `flake.nix`; no primary Python application package manager is detected.
- Shell - Build/support scripts exist in native submodules and workflow snippets, for example `plugin/anthy/src/main/cpp/anthy-cmake/build-android.sh`.
- XML - Android manifests and resources live under `app/src/main`, `lib/*/src/main`, and `plugin/*/src/main`.
- Nix - Reproducible development shell and Android SDK composition are defined in `flake.nix` and exposed through `shell.nix`.

## Runtime

**Environment:**
- Android app/runtime package: `org.fcitx.fcitx5.android`, declared in `app/build.gradle.kts`.
- Base app version: 0.1.3, base version code 11, declared in `build-logic/convention/src/main/kotlin/Versions.kt`.
- Android SDK: compile SDK 36, target SDK 36, min SDK 23, declared centrally in `build-logic/convention/src/main/kotlin/Versions.kt`.
- Android NDK: 28.0.13004108 by default, declared in `build-logic/convention/src/main/kotlin/Versions.kt` and consumed through `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`.
- CMake: 3.31.6 by default, declared in `build-logic/convention/src/main/kotlin/Versions.kt`, `flake.nix`, and CI workflows such as `.github/workflows/pull_request.yml`.
- JDK: CI and Nix development shell use Java 17 (`.github/workflows/pull_request.yml`, `.github/workflows/publish.yml`, `flake.nix`) while compiled Android/Kotlin bytecode targets Java 11 (`build-logic/convention/src/main/kotlin/Versions.kt`).

**Package Manager:**
- Gradle Wrapper 9.6.1 - `gradle/wrapper/gradle-wrapper.properties`.
- Gradle version catalog - `gradle/libs.versions.toml`.
- Nix flakes - `flake.nix` and `flake.lock`.
- Lockfile: `flake.lock` present; no Gradle dependency lockfiles detected.

## Frameworks

**Core:**
- Android Gradle Plugin 9.4.1 - Android app/library/plugin APK builds via `gradle/libs.versions.toml`.
- Kotlin Android/JVM 2.4.10 - App, plugin, codegen, and build logic via `gradle/libs.versions.toml`.
- Fcitx5 native framework - Vendored submodule at `lib/fcitx5/src/main/cpp/fcitx5` with Android wrapper in `lib/fcitx5/src/main/cpp/CMakeLists.txt`.
- libime - Vendored input method engine library at `lib/libime/src/main/cpp/libime` with Android wrapper in `lib/libime/src/main/cpp/CMakeLists.txt`.
- AndroidX AppCompat, Activity, Lifecycle, Navigation, Preference, Paging, RecyclerView, Startup, ViewPager2 - Dependencies declared in `gradle/libs.versions.toml` and consumed in `app/build.gradle.kts`.
- Room 2.8.4 - Clipboard history database declared in `gradle/libs.versions.toml`, wired with KSP in `app/build.gradle.kts`, implemented in `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt`.
- Material Components 1.14.0 - UI dependency declared in `gradle/libs.versions.toml` and consumed in `app/build.gradle.kts`.

**Testing:**
- JUnit 4.13.2 - Unit tests via `app/build.gradle.kts`.
- AndroidX Test Runner/Rules 1.7.0 - Instrumentation tests via `app/build.gradle.kts`.
- AndroidX Lifecycle runtime testing 2.11.0 - Instrumentation support via `app/build.gradle.kts`.
- Native CMake tests exist inside vendored submodules, but Android Gradle targets disable many upstream test options in wrapper CMake files such as `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`.

**Build/Dev:**
- Included Gradle build logic - Custom convention plugins live in `build-logic/convention/src/main/kotlin` and are included by `settings.gradle.kts`.
- KSP 2.3.10 - Used by `app/build.gradle.kts` for Room and project code generation from `codegen/build.gradle.kts`.
- Kotlin Serialization 1.11.0 - JSON parsing and plugin data, declared in `gradle/libs.versions.toml` and used by `app/build.gradle.kts` and `plugin/clipboard-filter/build.gradle.kts`.
- AboutLibraries 14.2.1 - Offline license metadata collection/export in `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`.
- ProGuard/R8 resource shrinking - Enabled for release builds in `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`.
- Prefab - Native Android library publication for Fcitx5, libime, Lua, and Chinese addon modules in `lib/fcitx5/build.gradle.kts`, `lib/libime/build.gradle.kts`, `lib/fcitx5-lua/build.gradle.kts`, and `lib/fcitx5-chinese-addons/build.gradle.kts`.
- GitHub Actions - CI/CD workflows in `.github/workflows/pull_request.yml`, `.github/workflows/publish.yml`, `.github/workflows/fdroid.yml`, and `.github/workflows/nix.yml`.

## Key Dependencies

**Critical:**
- `com.android.tools.build:gradle` 9.4.1 - Android build system in `gradle/libs.versions.toml`.
- `org.jetbrains.kotlin:kotlin-gradle-plugin` 2.4.10 - Kotlin compilation in `gradle/libs.versions.toml`.
- `androidx.room:*` 2.8.4 - Clipboard storage and paging in `app/build.gradle.kts`.
- `org.jetbrains.kotlinx:kotlinx-coroutines-android` 1.11.0 - App async work in `app/build.gradle.kts`.
- `org.jetbrains.kotlinx:kotlinx-serialization-json` 1.11.0 - JSON parsing in app/plugin modules, including `plugin/clipboard-filter/build.gradle.kts`.
- `io.arrow-kt:arrow-core` and `io.arrow-kt:arrow-functions` 2.2.3 - Functional helpers in `app/build.gradle.kts`.
- `com.jakewharton.timber:timber` 5.0.1 - App logging in `app/build.gradle.kts`.
- `com.vanniktech:android-image-cropper` 4.7.0 - Image crop support in `app/build.gradle.kts`; its default provider/activity are explicitly removed in `app/src/main/AndroidManifest.xml`.
- Fcitx5/libime/fcitx5-lua/fcitx5-chinese-addons - Core native engine components included as Gradle modules in `settings.gradle.kts` and linked by `app/src/main/cpp/CMakeLists.txt`.

**Infrastructure:**
- Boost - Prebuilt native dependency from `lib/fcitx5/src/main/cpp/prebuilt`; CMake requirements appear in `app/src/main/cpp/CMakeLists.txt`, `lib/libime/src/main/cpp/CMakeLists.txt`, and `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`.
- libuv - Prebuilt event loop dependency linked by `app/src/main/cpp/CMakeLists.txt` and found by `lib/fcitx5/src/main/cpp/CMakeLists.txt`.
- OpenCC, Marisa, Lua, libintl-lite, zstd - Native libraries recorded in `app/licenses/libraries` and consumed through `lib/fcitx5/src/main/cpp/prebuilt`.
- nlohmann_json - Prebuilt CMake dependency resolved by `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`.
- librime, leveldb, yaml-cpp, glog, librime-lua, librime-octagram, librime-predict - Rime plugin native dependencies recorded in `plugin/rime/licenses/libraries`.
- libchewing, libhangul, libthai, libiconv, anthy-unicode - Plugin native dependencies recorded in `plugin/chewing/licenses/libraries`, `plugin/hangul/licenses/libraries`, `plugin/thai/licenses/libraries`, and `plugin/anthy/licenses/libraries`.
- ClearURLs Rules - Clipboard filter data submodule declared in `.gitmodules` and packaged by `plugin/clipboard-filter/build.gradle.kts`.

## Configuration

**Environment:**
- Build override environment/Gradle properties are read through `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`: `CMAKE_VERSION`, `NDK_VERSION`, `BUILD_TOOLS_VERSION`, `BUILD_VERSION_NAME`, `BUILD_COMMIT_HASH`, `BUILD_TIMESTAMP`, and `BUILD_ABI`.
- Release signing is configured from `SIGN_KEY_BASE64`, `SIGN_KEY_FILE`, `SIGN_KEY_PWD`, and `SIGN_KEY_ALIAS` in `build-logic/convention/src/main/kotlin/ProjectExtensions.kt`; do not store signing values in committed files.
- Publishing to GitHub Packages uses `GITHUB_ACTOR` and `GITHUB_TOKEN` in `build-logic/convention/build.gradle.kts`, `lib/common/build.gradle.kts`, and `lib/plugin-base/build.gradle.kts`.
- CI uses secret names `PACKAGE_TOKEN` and `CACHIX_AUTH_TOKEN` in `.github/workflows/publish.yml` and `.github/workflows/nix.yml`; secret values are not stored in the repository.
- No `.env` or `.env.*` files were detected by filename scan.

**Build:**
- Root Gradle settings and module list: `settings.gradle.kts`.
- Root plugin aliases: `build.gradle.kts`.
- Version catalog: `gradle/libs.versions.toml`.
- Android/NDK version constants: `build-logic/convention/src/main/kotlin/Versions.kt`.
- Android convention plugins: `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`, `AndroidAppConventionPlugin.kt`, `AndroidLibConventionPlugin.kt`, and `AndroidPluginAppConventionPlugin.kt`.
- Native convention plugins and tasks: `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`, `NativeAppConventionPlugin.kt`, `NativeLibConventionPlugin.kt`, and `NativeBuildTasks.kt`.
- Nix shell: `flake.nix`, `flake.lock`, `shell.nix`.
- Localization mapping: `transifex.yml`; `transifex-config.ini` exists but is not required for build execution and should be treated cautiously as tool config.

## Platform Requirements

**Development:**
- Android SDK platform 36, build tools 36.1.0, platform tools 36.0.2, NDK 28.0.13004108, and CMake 3.31.6 are composed by `flake.nix`.
- Java 17 is used by CI and the Nix shell in `.github/workflows/pull_request.yml`, `.github/workflows/publish.yml`, and `flake.nix`.
- Extra native build tools: Extra CMake Modules, gettext, Python 3, and ICU are included in `flake.nix`; CI installs Extra CMake Modules and gettext in `.github/workflows/pull_request.yml`.
- Supported ABIs are `armeabi-v7a`, `arm64-v8a`, `x86`, and `x86_64`, declared in `build-logic/convention/src/main/kotlin/Versions.kt` and applied in `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`.

**Production:**
- Android APKs are built from `app/build.gradle.kts` and plugin APKs under `plugin/*/build.gradle.kts`.
- Release builds enable minification/resource shrinking and ProGuard defaults in `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`.
- Distribution targets include GitHub Releases/Jenkins CI references in `README.md`, F-Droid metadata/workflow in `app/org.fcitx.fcitx5.android.yml` and `.github/workflows/fdroid.yml`, and Google Play listing metadata under `app/src/main/play`.
- Maven artifacts for build logic and libraries publish to GitHub Packages from `.github/workflows/publish.yml`.

---

*Stack analysis: 2026-11-17*
