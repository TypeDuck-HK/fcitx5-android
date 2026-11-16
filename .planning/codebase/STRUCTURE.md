# Codebase Structure

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## Directory Layout

```text
fcitx5-android/
├── app/                         # Main Android application and IME APK
│   ├── src/main/java/           # Kotlin app, core, data, input, UI, provider, utils
│   ├── src/main/cpp/            # Android JNI bridge and Android-specific Fcitx addons
│   ├── src/main/res/            # Android resources, layouts, themes, icons, XML metadata
│   ├── src/main/assets/         # App-packaged Fcitx data descriptor/assets root
│   ├── schemas/                 # Room schema snapshots for clipboard database
│   └── licenses/                # AboutLibraries license metadata
├── build-logic/convention/      # Included Gradle build with project convention plugins
├── codegen/                     # KSP processors that generate key/scancode mappings
├── gradle/                      # Gradle wrapper and version catalog
├── lib/                         # Shared Android libraries and native Prefab libraries
│   ├── common/                  # Shared AIDL and plugin IPC helpers
│   ├── plugin-base/             # Base library for plugin APKs
│   ├── fcitx5/                  # Fcitx5 native source/build module
│   ├── fcitx5-lua/              # fcitx5-lua native source/build module
│   ├── fcitx5-chinese-addons/   # Chinese addon native source/build module
│   └── libime/                  # libime native source/build module
├── plugin/                      # Separate plugin application modules
│   ├── anthy/
│   ├── chewing/
│   ├── clipboard-filter/
│   ├── hangul/
│   ├── jyutping/
│   ├── rime/
│   ├── sayura/
│   ├── thai/
│   └── unikey/
├── .github/                     # GitHub workflows and issue templates
├── .planning/codebase/          # GSD codebase maps
├── settings.gradle.kts          # Gradle module graph
├── build.gradle.kts             # Root Gradle plugin declarations
└── gradle/libs.versions.toml    # Version catalog
```

## Directory Purposes

**`app/`:**
- Purpose: Main Android app, settings UI, IME service, native bridge, Room schema, app metadata.
- Contains: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, Kotlin source, C++ source, resources, Play metadata.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/cpp/native-lib.cpp`.

**`app/src/main/java/org/fcitx/fcitx5/android/core/`:**
- Purpose: Kotlin API and models for the Fcitx engine.
- Contains: `Fcitx.kt`, `FcitxAPI.kt`, `FcitxDispatcher.kt`, `FcitxLifecycle.kt`, events, keys, config, addon and candidate models.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxEvent.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/core/data/`:**
- Purpose: Runtime app/plugin asset detection, descriptor merge, and data hierarchy sync.
- Contains: `DataManager`, descriptor models, file source/action models, plugin failure models.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/PluginDescriptor.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/daemon/`:**
- Purpose: Singleton Fcitx process manager and connection helpers.
- Contains: `FcitxDaemon`, `FcitxConnection`, coroutine helper extensions.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt`, `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxConnection.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/data/`:**
- Purpose: App data managers and persistence abstractions.
- Contains: clipboard Room DB, pinyin/table dictionaries, preferences, punctuation, quick phrase, theme managers.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/theme/ThemeManager.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/input/`:**
- Purpose: Android IME service and all keyboard/candidate UI.
- Contains: `FcitxInputMethodService`, `InputView`, `CandidatesView`, keyboard views, candidate adapters/windows, popup/picker/editorinfo/status/window-manager components.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/wm/InputWindowManager.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/ui/`:**
- Purpose: Main settings UI, setup flow, common UI helpers, settings fragments, theme editing.
- Contains: `ui/main`, `ui/setup`, `ui/common`.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainActivity.kt`, `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/provider/`:**
- Purpose: Android DocumentsProvider for app external files.
- Contains: Storage access implementation.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`.

**`app/src/main/java/org/fcitx/fcitx5/android/utils/`:**
- Purpose: Shared Kotlin utility extensions and helpers.
- Contains: Android service accessors, navigation helpers, file utilities, graph helpers, locale/setup utilities, config parsing support.
- Key files: `app/src/main/java/org/fcitx/fcitx5/android/utils`, `app/src/main/java/org/fcitx/fcitx5/android/utils/config`.

**`app/src/main/cpp/`:**
- Purpose: Native Android bridge, JNI exports, Android-specific Fcitx addons, translations.
- Contains: `native-lib.cpp`, JNI helper headers, `androidfrontend`, `androidkeyboard`, `androidnotification`, `androidaddonloader`, `po`.
- Key files: `app/src/main/cpp/CMakeLists.txt`, `app/src/main/cpp/native-lib.cpp`, `app/src/main/cpp/androidfrontend/androidfrontend.cpp`.

**`build-logic/convention/`:**
- Purpose: Project-local Gradle convention plugin implementation.
- Contains: Android app/lib/plugin conventions, native conventions, descriptor/header/component tasks, version constants.
- Key files: `build-logic/convention/build.gradle.kts`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`, `build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt`.

**`codegen/`:**
- Purpose: KSP processors for generated mappings used by core/input code.
- Contains: key and scancode mapping processors plus KSP service registration.
- Key files: `codegen/src/main/java/org/fcitx/fcitx5/android/codegen/GenKeyMapping.kt`, `codegen/src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`.

**`lib/common/`:**
- Purpose: Shared contracts for main app and plugin APKs.
- Contains: AIDL remote service interfaces, clipboard transformer interface, binding helper, plugin service base.
- Key files: `lib/common/src/main/aidl/org/fcitx/fcitx5/android/common/ipc/IFcitxRemoteService.aidl`, `lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt`.

**`lib/plugin-base/`:**
- Purpose: Shared Android library for plugin applications.
- Contains: plugin base resources, generic launcher icons/theme resources, dependency on `lib/common`.
- Key files: `lib/plugin-base/build.gradle.kts`, `lib/plugin-base/src/main/AndroidManifest.xml`, `lib/plugin-base/src/debug/AndroidManifest.xml`.

**`lib/fcitx5/`:**
- Purpose: Fcitx5 native Android library module and Prefab exports.
- Contains: upstream Fcitx5 source under `lib/fcitx5/src/main/cpp/fcitx5`, CMake modules, prebuilt dependency directory.
- Key files: `lib/fcitx5/build.gradle.kts`, `lib/fcitx5/src/main/cpp/CMakeLists.txt`.

**`lib/libime/`:**
- Purpose: libime native Android library module and Prefab exports.
- Contains: upstream libime source and CMake build.
- Key files: `lib/libime/build.gradle.kts`, `lib/libime/src/main/cpp/CMakeLists.txt`.

**`lib/fcitx5-lua/`:**
- Purpose: fcitx5-lua native Android library module.
- Contains: upstream fcitx5-lua source and CMake build.
- Key files: `lib/fcitx5-lua/build.gradle.kts`, `lib/fcitx5-lua/src/main/cpp/CMakeLists.txt`.

**`lib/fcitx5-chinese-addons/`:**
- Purpose: Chinese addon native Android library module.
- Contains: pinyin/table/native modules, data, CMake build.
- Key files: `lib/fcitx5-chinese-addons/build.gradle.kts`, `lib/fcitx5-chinese-addons/src/main/cpp/CMakeLists.txt`.

**`plugin/*/`:**
- Purpose: Independent plugin APKs providing optional input methods, native addons, data, and services.
- Contains: `build.gradle.kts`, `src/main/AndroidManifest.xml`, `src/main/res/xml/plugin.xml`, resources, optional Kotlin and C++ sources.
- Key files: `plugin/clipboard-filter/build.gradle.kts`, `plugin/clipboard-filter/src/main/java/org/fcitx/fcitx5/android/plugin/clipboard_filter/MainService.kt`, `plugin/jyutping/build.gradle.kts`.

**`.planning/codebase/`:**
- Purpose: Generated GSD codebase maps.
- Contains: `STACK.md`, `INTEGRATIONS.md`, `CONVENTIONS.md`, `TESTING.md`, `CONCERNS.md`, `ARCHITECTURE.md`, `STRUCTURE.md`.
- Key files: `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/STRUCTURE.md`.

## Key File Locations

**Entry Points:**
- `app/src/main/AndroidManifest.xml`: Declares application, launcher/settings activities, IME service, remote IPC service, documents provider, permissions, plugin queries.
- `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt`: Process-level initialization.
- `app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainActivity.kt`: Settings host and route processing.
- `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`: Android IME service.
- `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`: AIDL service used by plugins.
- `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`: Android DocumentsProvider.
- `app/src/main/cpp/native-lib.cpp`: JNI entry point and native Fcitx bootstrap.

**Configuration:**
- `settings.gradle.kts`: Includes all Gradle modules.
- `build.gradle.kts`: Root plugin version declarations.
- `gradle/libs.versions.toml`: External dependency and plugin versions.
- `gradle.properties`: Gradle/Android project properties.
- `build-logic/convention/src/main/kotlin/Versions.kt`: SDK, toolchain, app version, ABI constants.
- `app/build.gradle.kts`: Main app plugins, native targets, dependencies, descriptor/component settings.
- `plugin/*/build.gradle.kts`: Plugin application configuration.
- `plugin/pluginSchema.xsd`: Plugin descriptor schema.

**Core Logic:**
- `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`: JNI-backed Fcitx API implementation.
- `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxAPI.kt`: Public engine API contract.
- `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt`: Native-thread dispatcher.
- `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxEvent.kt`: Native-to-Kotlin event model.
- `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt`: Singleton engine lifecycle manager.
- `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt`: Data/plugin sync.
- `app/src/main/cpp/androidfrontend/androidfrontend.cpp`: Native Android frontend addon.
- `app/src/main/cpp/androidkeyboard/androidkeyboard.cpp`: Built-in Android keyboard engine.

**UI and Input:**
- `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt`: Settings graph and typed routes.
- `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings`: Settings fragments.
- `app/src/main/java/org/fcitx/fcitx5/android/ui/main/EditDeleteMenuProvider.kt`: Lifecycle-bound toolbar edit/delete actions shared by list fragments.
- `app/src/main/java/org/fcitx/fcitx5/android/ui/setup/SetupActivity.kt` and `SetupFragment.kt`: Setup state synchronization from `SetupPage`; no shared setup ViewModel.
- `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt`: Keyboard view composition.
- `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt`: Floating candidates view.
- `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard`: Keyboard definitions, rendering, actions.
- `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard/KeyboardHeightPercentBase.kt`: Display-metrics versus real-display sizing preference consumed by `InputView`.
- `app/src/main/java/org/fcitx/fcitx5/android/input/candidates`: Candidate UIs and adapters.
- `app/src/main/java/org/fcitx/fcitx5/android/input/wm`: Input window abstractions and manager.

**Data and Persistence:**
- `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt`: Root app preference categories.
- `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/ManagedPreference.kt`: Preference delegate types.
- `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db`: Room clipboard database.
- `app/schemas/org.fcitx.fcitx5.android.data.clipboard.db.ClipboardDatabase`: Room schema snapshots.
- `app/src/main/java/org/fcitx/fcitx5/android/data/theme`: Theme model, serializer, manager, preferences.
- `app/src/main/java/org/fcitx/fcitx5/android/data/pinyin`: Pinyin dictionary/custom phrase managers.
- `app/src/main/java/org/fcitx/fcitx5/android/data/table`: Table input method managers and dictionary loaders.

**Plugin Integration:**
- `app/src/main/java/org/fcitx/fcitx5/android/core/data/PluginDescriptor.kt`: Parsed plugin metadata.
- `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxPluginServices.kt`: Optional plugin service binding.
- `lib/common/src/main/aidl/org/fcitx/fcitx5/android/common/ipc`: AIDL contracts.
- `lib/common/src/main/java/org/fcitx/fcitx5/android/common/ipc/FcitxRemoteConnection.kt`: Plugin-side remote binding helper.
- `lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt`: Base class for service plugins.
- `plugin/clipboard-filter/src/main/res/xml/plugin.xml`: Example service plugin descriptor.
- `plugin/clipboard-filter/src/main/AndroidManifest.xml`: Example service plugin manifest.

**Build Logic:**
- `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`: Shared Android SDK/JVM/lint compiler setup.
- `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`: Shared native/CMake/ABI setup.
- `build-logic/convention/src/main/kotlin/AndroidPluginAppConventionPlugin.kt`: Plugin app placeholders and aggregate assemble tasks.
- `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`: CMake component install tasks into assets.
- `build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt`: `descriptor.json` generation.
- `build-logic/convention/src/main/kotlin/FcitxHeadersPlugin.kt`: Header/devel component support.

**Testing:**
- `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`: JVM unit tests.
- `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`: JVM unit tests.
- `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`: Instrumented Android/Fcitx test.
- `app/schemas/org.fcitx.fcitx5.android.data.clipboard.db.ClipboardDatabase`: Room migration schema references.

## Naming Conventions

**Files:**
- Kotlin source files use PascalCase matching the primary type, for example `FcitxDaemon.kt`, `InputView.kt`, `ManagedPreference.kt`.
- Small helper files may use generic `Functions.kt` within the package they extend, for example `app/src/main/java/org/fcitx/fcitx5/android/daemon/Functions.kt`.
- Native C++ files use lowercase or addon names, for example `native-lib.cpp`, `androidfrontend.cpp`, `androidkeyboard.cpp`.
- Gradle module build files use `build.gradle.kts`; root includes use `settings.gradle.kts`.
- Plugin descriptors use `plugin.xml` under `plugin/*/src/main/res/xml`.
- Android resources follow Android conventions: `activity_main.xml`, `fragment_setup.xml`, `ic_baseline_*_24.xml`, `values-*/strings.xml`.

**Directories:**
- Kotlin package directories mirror `org.fcitx.fcitx5.android.<area>`.
- App feature directories use area names: `core`, `daemon`, `data`, `input`, `provider`, `ui`, `utils`.
- Input UI subdirectories group by surface/component: `input/bar`, `input/candidates`, `input/keyboard`, `input/picker`, `input/popup`, `input/wm`.
- Settings fragments live under route/feature groups such as `ui/main/settings/addon`, `ui/main/settings/behavior`, `ui/main/settings/im`, `ui/main/settings/theme`.
- Native addon directories match Fcitx addon names: `app/src/main/cpp/androidfrontend`, `app/src/main/cpp/androidkeyboard`, `app/src/main/cpp/androidnotification`.
- Plugin modules use lowercase hyphenated Gradle directory names, for example `plugin/clipboard-filter`, with Kotlin package names using underscores, for example `org.fcitx.fcitx5.android.plugin.clipboard_filter`.

## Where to Add New Code

**New settings screen:**
- Route: add a new `SettingsRoute` entry in `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt`.
- Fragment: add under the closest feature directory in `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings`.
- Navigation: register the fragment in `SettingsRoute.createGraph()`.
- Toolbar actions: register a `MenuProvider` with the fragment's `viewLifecycleOwner`; reuse `EditDeleteMenuProvider` for edit/delete list actions.
- Preferences: add typed entries to `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt` or a focused manager under `app/src/main/java/org/fcitx/fcitx5/android/data`.

**New Fcitx engine operation:**
- API contract: add a suspend function to `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxAPI.kt`.
- Kotlin implementation: implement in `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` using `withFcitxContext`.
- JNI/native implementation: add external declaration in `Fcitx.kt` and JNI function in `app/src/main/cpp/native-lib.cpp`.
- Callers: invoke through `FcitxConnection.runOnReady` or `launchOnReady`, not by retaining `FcitxAPI`.

**New native Android addon:**
- Native code: add a subdirectory under `app/src/main/cpp/<addon-name>`.
- CMake: add `add_subdirectory(<addon-name>)` in `app/src/main/cpp/CMakeLists.txt`.
- App native targets: add the target in `app/build.gradle.kts` `externalNativeBuild.cmake.targets`.
- Data/config install: use CMake install components and `fcitxComponent` in `app/build.gradle.kts`.

**New input UI component/window:**
- Component code: add under `app/src/main/java/org/fcitx/fcitx5/android/input/<surface>`.
- Shared window behavior: implement or use abstractions in `app/src/main/java/org/fcitx/fcitx5/android/input/wm`.
- Composition: register the component/window in `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt`.
- Events: receive Fcitx/input updates through `InputBroadcaster` and typed event handling.

**New data manager or persistence feature:**
- Shared app data manager: add under `app/src/main/java/org/fcitx/fcitx5/android/data/<feature>`.
- Fcitx packaged data sync: add assets/config through module assets and descriptors, not manual runtime copies.
- Database-backed feature: add Room entities/DAO/database files near `app/src/main/java/org/fcitx/fcitx5/android/data/<feature>/db` and keep schemas under `app/schemas`.

**New plugin APK:**
- Gradle module: create `plugin/<name>` and include it in `settings.gradle.kts`.
- Build config: use `org.fcitx.fcitx5.android.plugin-app-convention`, `build-metadata`, `data-descriptor`, and native conventions if needed.
- Descriptor: add `plugin/<name>/src/main/res/xml/plugin.xml` with compatible `apiVersion`.
- Manifest: add `plugin/<name>/src/main/AndroidManifest.xml`; add `${mainApplicationId}.permission.IPC` only when the plugin binds the remote service.
- Shared code: depend on `lib/plugin-base` for plugin helpers and resources.
- Optional service: subclass `FcitxPluginService` from `lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt`.

**New shared plugin IPC contract:**
- AIDL: add or extend files under `lib/common/src/main/aidl/org/fcitx/fcitx5/android/common/ipc`.
- App service: implement the main-app side in `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`.
- Plugin helper: add binding/helper code under `lib/common/src/main/java/org/fcitx/fcitx5/android/common/ipc`.

**New generated mapping:**
- Processor: add to `codegen/src/main/java/org/fcitx/fcitx5/android/codegen`.
- Provider registration: update `codegen/src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`.
- Consumer module: ensure `app/build.gradle.kts` or the consuming module applies KSP dependency.

**New Gradle convention:**
- Implementation: add a plugin class under `build-logic/convention/src/main/kotlin`.
- Registration: add an entry in `build-logic/convention/build.gradle.kts`.
- Usage: apply the convention plugin by ID in the target module `build.gradle.kts`.

**New tests:**
- JVM unit tests: add under `app/src/test/java/org/fcitx/fcitx5/android`.
- Instrumented Android tests: add under `app/src/androidTest/java/org/fcitx/fcitx5/android`.
- Room schema changes: keep `app/schemas/org.fcitx.fcitx5.android.data.clipboard.db.ClipboardDatabase` updated.

## Special Directories

**`app/schemas/`:**
- Purpose: Room schema export snapshots for `ClipboardDatabase`.
- Generated: Yes.
- Committed: Yes.

**`app/licenses/` and `plugin/*/licenses/`:**
- Purpose: AboutLibraries metadata for bundled/native/plugin dependencies.
- Generated: No.
- Committed: Yes.

**`app/src/main/play/` and `plugin/*/src/main/play/`:**
- Purpose: Play/F-Droid listing metadata and release notes.
- Generated: No.
- Committed: Yes.

**`app/src/main/assets/`:**
- Purpose: Descriptor/assets root for packaged Fcitx data installed by build tasks.
- Generated: Partially, via build tasks such as `generateDataDescriptor`.
- Committed: Contains source assets such as `README.md`; generated descriptor output belongs to build/merged assets.

**`lib/fcitx5/src/main/cpp/prebuilt/`:**
- Purpose: Prebuilt native dependencies/data consumed by CMake and `fcitxComponent`.
- Generated: No in normal Gradle builds.
- Committed: Yes when present in the repository.

**`lib/*/src/main/cpp/<upstream>/`:**
- Purpose: Vendored/upstream native projects built as Android library modules.
- Generated: No.
- Committed: Yes.

**`build-logic/`:**
- Purpose: Included Gradle build for local convention plugins.
- Generated: No.
- Committed: Yes.

**`.cxx/`, `build/`, `.gradle/`:**
- Purpose: Android Gradle/CMake build outputs.
- Generated: Yes.
- Committed: No.

**`.planning/codebase/`:**
- Purpose: GSD-generated codebase intelligence documents.
- Generated: Yes.
- Committed: Project workflow dependent.

---

*Structure analysis: 2026-11-17*
