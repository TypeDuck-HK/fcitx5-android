<!-- refreshed: 2026-11-17 -->
# Architecture

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## System Overview

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Android app and plugin APKs                         │
├──────────────────────────┬──────────────────────────┬───────────────────────┤
│ Settings/activity UI     │ Input method service     │ Plugin applications   │
│ `app/src/main/java/.../ui` │ `app/src/main/java/.../input` │ `plugin/*`      │
└──────────────┬───────────┴──────────────┬───────────┴───────────┬───────────┘
               │                          │                       │
               ▼                          ▼                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Kotlin core and service boundary                         │
│ `app/src/main/java/org/fcitx/fcitx5/android/core`                            │
│ `app/src/main/java/org/fcitx/fcitx5/android/daemon`                          │
│ `lib/common/src/main/aidl`                                                   │
└──────────────────────────────────────────┬──────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Native Fcitx/libime runtime                          │
│ `app/src/main/cpp/native-lib.cpp`                                             │
│ `app/src/main/cpp/androidfrontend`                                            │
│ `app/src/main/cpp/androidkeyboard`                                            │
│ `lib/fcitx5/src/main/cpp`, `lib/libime/src/main/cpp`                          │
└──────────────────────────────────────────┬──────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Assets, user data, descriptors                          │
│ `app/src/main/assets`, `plugin/*/src/main/assets`, app data dir               │
│ `build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt`              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| Application bootstrap | Initializes direct-boot mode, logging, preferences, clipboard, theme, locale, shutdown/unlock/restart receivers | `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt` |
| Settings shell | Hosts the app settings UI and navigation graph; handles launcher, app preferences, and dictionary import intents | `app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainActivity.kt` |
| Route model | Defines all settings destinations as parcelable/serializable route objects and builds the Navigation graph | `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt` |
| IME service | Owns Android `InputMethodService` lifecycle, input/candidate views, editor events, cursor tracking, and event handling | `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt` |
| Input view composition | Builds the virtual keyboard surface from dependency-injected components and window panes | `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt` |
| Floating candidates | Renders preedit and paged candidate UI, anchored to cursor or fallback positions | `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt` |
| Input window manager | Switches keyboard, symbol, emoji, and other input windows inside the IME surface | `app/src/main/java/org/fcitx/fcitx5/android/input/wm/InputWindowManager.kt` |
| Daemon facade | Owns the singleton Fcitx engine instance and hands clients scoped `FcitxConnection` handles | `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt` |
| Connection API | Defines safe access modes for immediate, ready-gated, and fire-if-ready Fcitx operations | `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxConnection.kt` |
| Kotlin Fcitx facade | Implements `FcitxAPI`, exposes cached state and JNI methods, starts/stops native runtime | `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` |
| Fcitx dispatcher | Runs native startup, libuv loop iterations, and queued Kotlin calls on a single `fcitx-main` thread | `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt` |
| Lifecycle registry | Models Fcitx state transitions and provides lifecycle coroutine scope | `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxLifecycle.kt` |
| Native JNI bridge | Creates native Fcitx instance, sets standard paths/env vars, registers Android callbacks, exposes JNI functions | `app/src/main/cpp/native-lib.cpp` |
| Android frontend addon | Implements the Fcitx frontend/input context bridge for Android commit, candidate, preedit, and key events | `app/src/main/cpp/androidfrontend/androidfrontend.cpp` |
| Android keyboard engine | Implements the built-in English/word-hint input method engine | `app/src/main/cpp/androidkeyboard/androidkeyboard.cpp` |
| Data manager | Detects plugin APKs, merges descriptor hierarchies, copies assets, and maintains the installed Fcitx data tree | `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt` |
| Data hierarchy | Merges app/plugin descriptors and computes file/symlink migration actions | `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt` |
| Plugin service bridge | Binds optional plugin services and broadcasts messages to them | `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxPluginServices.kt` |
| Remote IPC service | Exposes AIDL functions for plugins and clipboard transformers | `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt` |
| Shared plugin base | Supplies common AIDL contracts and plugin service base class | `lib/common/src/main/aidl`, `lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt` |
| Documents provider | Exposes external app files through Android storage access framework | `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt` |
| Build conventions | Centralizes Android, native, plugin, descriptor, header, and metadata build behavior | `build-logic/convention/src/main/kotlin` |

## Pattern Overview

**Overall:** Layered Android app with a daemon-owned native engine and APK-based plugin extension.

**Key Characteristics:**
- Use `FcitxDaemon.connect(name)` from Android clients; do not construct or retain `Fcitx` directly (`app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt:29`).
- Use `FcitxConnection.runOnReady` or `launchOnReady` for most engine operations; use `runImmediately` only for non-blocking cached state or flow access (`app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxConnection.kt:15`).
- Keep native Fcitx operations on the `FcitxDispatcher` single-thread loop (`app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt:38`).
- Route engine-to-Android updates through `FcitxEvent` and `eventFlow`, then let UI/service components consume typed events (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:397`).
- Package built-in/native data with descriptor files and let `DataManager.sync()` merge the app and plugin asset trees (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:181`).
- Build plugins as separate Android application modules under `plugin/*`, with `res/xml/plugin.xml` metadata and optional plugin services.

## Layers

**Android Entry Layer:**
- Purpose: Receives Android lifecycle, launcher, IME, IPC, and document-provider entry points.
- Location: `app/src/main/AndroidManifest.xml`
- Contains: `FcitxApplication`, activities, `FcitxInputMethodService`, `FcitxRemoteService`, `FcitxDataProvider`.
- Depends on: Android framework, app resources, `app/src/main/java/org/fcitx/fcitx5/android`.
- Used by: Android OS and external/plugin processes.

**Settings UI Layer:**
- Purpose: Presents app configuration, Fcitx addon/input-method configuration, plugin status, logs, setup, and import flows.
- Location: `app/src/main/java/org/fcitx/fcitx5/android/ui`
- Contains: `MainActivity`, fragments, route model, preference screens, theme editors, setup screens.
- Depends on: `FcitxDaemon`, `AppPrefs`, `SettingsRoute`, `utils/config`.
- Used by: Launcher activity and explicit settings intents.

**Input UI Layer:**
- Purpose: Implements IME surface, keyboard windows, candidate windows, popup/emoji/symbol panels, editor interaction, hardware key handling.
- Location: `app/src/main/java/org/fcitx/fcitx5/android/input`
- Contains: `FcitxInputMethodService`, `InputView`, `CandidatesView`, `input/keyboard`, `input/candidates`, `input/wm`, `input/broadcast`.
- Depends on: `FcitxConnection`, `FcitxEvent`, `AppPrefs`, `ThemeManager`, Android IME APIs.
- Used by: Android input method framework.

**Daemon/Core Layer:**
- Purpose: Provides lifecycle-safe access to the singleton native Fcitx engine and typed Kotlin API.
- Location: `app/src/main/java/org/fcitx/fcitx5/android/core`, `app/src/main/java/org/fcitx/fcitx5/android/daemon`
- Contains: `Fcitx`, `FcitxAPI`, `FcitxDaemon`, `FcitxConnection`, `FcitxDispatcher`, `FcitxLifecycle`, event/data types.
- Depends on: JNI `native-lib`, coroutines, `DataManager`, `ClipboardManager`, `FcitxPluginServices`.
- Used by: UI, IME service, settings fragments, remote service.

**Data/Preferences Layer:**
- Purpose: Stores app preferences, theme files, clipboard database, Fcitx assets, custom dictionaries, quick phrases, punctuation, and table IM data.
- Location: `app/src/main/java/org/fcitx/fcitx5/android/data`, `app/src/main/java/org/fcitx/fcitx5/android/core/data`
- Contains: `AppPrefs`, `ManagedPreference`, Room clipboard database, managers for pinyin/table/theme/quickphrase/punctuation, descriptor merge logic.
- Depends on: SharedPreferences, Room, Android assets/file APIs.
- Used by: Application bootstrap, IME UI, settings UI, native startup.

**Native Engine Layer:**
- Purpose: Builds and runs Fcitx5, libime, Chinese addons, Android frontend/keyboard/notification addons, JNI surface.
- Location: `app/src/main/cpp`, `lib/fcitx5/src/main/cpp`, `lib/libime/src/main/cpp`, `lib/fcitx5-chinese-addons/src/main/cpp`, `lib/fcitx5-lua/src/main/cpp`
- Contains: CMake projects, upstream sources, Android-specific addons, JNI conversion helpers.
- Depends on: Gradle native conventions, CMake, NDK, Prefab libraries, prebuilt assets.
- Used by: `Fcitx.kt` through JNI and Gradle packaging.

**Plugin Layer:**
- Purpose: Ships optional input methods, native addons, data files, and services as independent APK modules.
- Location: `plugin/*`
- Contains: Android application modules, `res/xml/plugin.xml`, manifests, optional Kotlin services, optional native CMake code.
- Depends on: `lib/plugin-base`, `lib/common`, native library modules as needed.
- Used by: `DataManager.detectPlugins()` and `FcitxPluginServices.connectAll()`.

**Build Logic Layer:**
- Purpose: Encodes module conventions, native build settings, component installation, descriptors, headers, app/plugin packaging.
- Location: `build-logic/convention/src/main/kotlin`
- Contains: Gradle convention plugins registered in `build-logic/convention/build.gradle.kts`.
- Depends on: Android Gradle Plugin, Kotlin Gradle Plugin, version catalog.
- Used by: all Gradle modules via plugin IDs in `*.gradle.kts`.

## Data Flow

### Primary IME Key/Event Path

1. Android binds the IME service declared in `app/src/main/AndroidManifest.xml:124`.
2. `FcitxInputMethodService.onCreate()` connects to the daemon and subscribes to `eventFlow` (`app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt:205`).
3. `FcitxDaemon.connect()` starts the singleton engine when the first client connects (`app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt:86`).
4. `Fcitx.start()` registers event handlers, sync callback, log stream, and starts the dispatcher (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:532`).
5. `FcitxDispatcher.start()` executes native startup and then repeatedly runs native loop iterations plus queued Kotlin operations on `fcitx-main` (`app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt:64`).
6. Native startup configures paths, addon domains, callbacks, and Android frontend bindings (`app/src/main/cpp/native-lib.cpp:510`).
7. Native callbacks call `Fcitx.handleFcitxEvent()`, which updates event handlers and emits `eventFlow` (`app/src/main/cpp/native-lib.cpp:594`, `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:397`).
8. `FcitxInputMethodService.handleFcitxEvent()` commits text, sends/forwards key events, updates composing text, updates subtype/status icon state, or shows the keyboard as needed (`app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt:232`).
9. `InputView`, `CandidatesView`, and subcomponents consume cached state/events through `FcitxConnection` and update UI (`app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt:352`, `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt:108`).

When the native input panel is empty, `androidfrontend.cpp` forwards its overlay message as `auxUp` through the existing input-panel event path.

### Settings UI Path

1. `MainActivity` is launched through `MAIN`, `APPLICATION_PREFERENCES`, or `VIEW` intents (`app/src/main/AndroidManifest.xml:69`).
2. `MainActivity.onCreate()` inflates `ActivityMainBinding`, installs the toolbar as the support action bar, creates the Navigation graph, observes toolbar state, and processes the incoming intent; `onSupportNavigateUp()` handles back callbacks and navigation (`app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainActivity.kt:42`).
3. `SettingsRoute.createGraph()` maps sealed route objects to fragments (`app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt:184`).
4. `MainViewModel` holds toolbar state and owns a daemon connection for settings fragments (`app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainViewModel.kt:14`).
5. Fragments read/write managed app preferences under `app/src/main/java/org/fcitx/fcitx5/android/data/prefs` or call `FcitxConnection.runOnReady` for native Fcitx configuration.
6. Fragments own toolbar actions through lifecycle-bound `MenuProvider`s. `EditDeleteMenuProvider` observes `MainViewModel.toolbarButton` (`NONE`, `EDIT`, `DELETE`); save actions and the main screen's about menu are registered by their owning fragments.

### Native Startup and Data Sync

1. `Fcitx.start()` schedules `DataManager.sync()` as the first native startup step (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:423`).
2. `DataManager.sync()` reads the previous installed descriptor, reads the app asset descriptor, detects plugin packages, and creates a merged `DataHierarchy` (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:181`).
3. `DataHierarchy.install()` rejects conflicting file/symlink paths while preserving each source (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt:29`).
4. `DataHierarchy.diff()` produces create/update/delete/symlink actions for the data directory (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt:86`).
5. `DataManager.sync()` copies files from app or plugin assets into the device-protected app data tree and writes the new descriptor (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:236`).
6. Native `startupFcitx()` receives app data, native library path, external data/cache paths, and plugin gettext domains (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:449`, `app/src/main/cpp/native-lib.cpp:510`).

### Plugin Detection and IPC

1. The app queries packages with action `${applicationId}.plugin.MANIFEST` (`app/src/main/AndroidManifest.xml:20`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:37`).
2. Plugin APKs publish metadata in `res/xml/plugin.xml`, for example `plugin/clipboard-filter/src/main/res/xml/plugin.xml:1`.
3. `DataManager.detectPlugins()` parses `apiVersion`, `domain`, `description`, and `hasService`, then rejects incompatible plugin API versions (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:87`).
4. `FcitxPluginServices.connectAll()` binds plugins whose descriptors declare `hasService=true` (`app/src/main/java/org/fcitx/fcitx5/android/core/FcitxPluginServices.kt:79`).
5. Plugin services subclass `FcitxPluginService`; binding calls `start()` and unbinding calls `stop()` (`lib/common/src/main/java/org/fcitx/fcitx5/android/common/FcitxPluginService.kt:14`).
6. Plugins bind the app remote IPC service through `bindFcitxRemoteService()` (`lib/common/src/main/java/org/fcitx/fcitx5/android/common/ipc/FcitxRemoteConnection.kt:13`).
7. `FcitxRemoteService` implements `IFcitxRemoteService` for restart, loaded plugin metadata, clipboard transformers, and reload requests (`app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt:59`).

**State Management:**
- Native engine state is singleton and lifecycle-gated by `FcitxDaemon` and `FcitxLifecycle`.
- Engine UI state is cached in `Fcitx` as `inputMethodEntryCached`, `statusAreaActionsCached`, `clientPreeditCached`, and `inputPanelCached` (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:40`).
- App preferences use a singleton `AppPrefs` wrapping `SharedPreferences` (`app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt:430`).
- Clipboard history uses Room in `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt`.
- Fcitx assets are materialized into `DataManager.dataDir`, using device-protected storage on Android N+ (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:55`).
- Input UI state is distributed across service fields, dependency-scoped input components, and `InputBroadcaster` components under `app/src/main/java/org/fcitx/fcitx5/android/input`.
- `InputView` computes keyboard height from orientation-specific percentages and the advanced `KeyboardHeightPercentBase` preference (`DisplayMetrics` by default, or `RealSize`), listening to both keyboard and advanced preference changes.
- Setup state is read directly from `SetupPage`: `SetupActivity.updateButtons()` and `SetupFragment.sync()` refresh on focus/page changes without a shared `SetupViewModel`.

## Key Abstractions

**FcitxConnection:**
- Purpose: Prevents direct leakage of `FcitxAPI` and enforces lifecycle-aware access to the native engine.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxConnection.kt`, `app/src/main/java/org/fcitx/fcitx5/android/daemon/Functions.kt`.
- Pattern: Capability/facade object returned by singleton daemon.

**FcitxAPI:**
- Purpose: Typed Kotlin surface for key events, candidates, input methods, configuration, addons, status actions, and cached event state.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxAPI.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`.
- Pattern: Interface implemented by JNI-backed facade.

**FcitxEvent:**
- Purpose: Typed event channel from native callbacks to Android service/UI.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxEvent.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:397`.
- Pattern: Sealed event model emitted through `SharedFlow`.

**ManagedPreference:**
- Purpose: Type-safe wrapper over `SharedPreferences`, with weak listener support and delegate syntax.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/ManagedPreference.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/AppPrefs.kt`.
- Pattern: Property delegate plus preference category registry.

**SettingsRoute:**
- Purpose: Type-safe route contract for settings navigation.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/ui/main/settings/SettingsRoute.kt`.
- Pattern: `@Serializable`/`@Parcelize` sealed class with Navigation Kotlin DSL.

**DataDescriptor/DataHierarchy:**
- Purpose: Checksummed representation of app/plugin asset trees and deterministic merge/diff operations.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataDescriptor.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt`, `build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt`.
- Pattern: Build-time descriptor generation plus runtime reconciliation.

**PluginDescriptor:**
- Purpose: Runtime metadata parsed from plugin APK XML.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/core/data/PluginDescriptor.kt`, `plugin/clipboard-filter/src/main/res/xml/plugin.xml`.
- Pattern: Manifest/resource-discovered extension point.

**InputWindow and Input Components:**
- Purpose: Compose keyboard, symbols, emoji, popup, preedit, and candidate UI into a dynamic IME surface.
- Examples: `app/src/main/java/org/fcitx/fcitx5/android/input/wm/InputWindowManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt`.
- Pattern: Dynamic dependency scope with unique components and attachable windows.

**Build Convention Plugins:**
- Purpose: Keep module build behavior consistent and encode native/component descriptor tasks.
- Examples: `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`, `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`.
- Pattern: Included build with project-local Gradle plugins.

## Entry Points

**Application:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt`
- Triggers: Android process creation through `android:name=".FcitxApplication"` in `app/src/main/AndroidManifest.xml:39`.
- Responsibilities: Direct boot context, logging, preferences, clipboard, theme, locale, crash handling, receiver registration.

**Launcher/settings activity:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/ui/main/MainActivity.kt`
- Triggers: `MAIN`, `APPLICATION_PREFERENCES`, `VIEW`, and internal `ACTION_RUN` intents in `app/src/main/AndroidManifest.xml:69`.
- Responsibilities: Settings navigation host, support action bar/navigation, toolbar state observation, notification permission prompt, dictionary import routing; fragments own menu commands.

**Setup activity:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/ui/setup/SetupActivity.kt`
- Triggers: Main activity startup when setup is incomplete; manifest declaration at `app/src/main/AndroidManifest.xml:50`.
- Responsibilities: Initial IME setup flow.

**Input method service:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
- Triggers: Android input method binding through `android.view.InputMethod` intent in `app/src/main/AndroidManifest.xml:124`.
- Responsibilities: Connects to daemon, creates input/candidate views, sends key and editor updates to Fcitx, applies Fcitx events to editor/UI.

**Remote IPC service:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`
- Triggers: Plugin binding through action `${applicationId}.IPC` in `app/src/main/AndroidManifest.xml:139`.
- Responsibilities: Exposes AIDL remote control and clipboard transformer registry.

**Documents provider:**
- Location: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
- Triggers: Android documents provider action in `app/src/main/AndroidManifest.xml:149`.
- Responsibilities: Exposes external app files for create/read/write/copy/move/delete/search.

**Native library:**
- Location: `app/src/main/cpp/native-lib.cpp`
- Triggers: `System.loadLibrary("native-lib")` in `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:231`.
- Responsibilities: JNI binding, Fcitx instance creation, native callback registration, Fcitx/libime operations.

**Plugin app:**
- Location: `plugin/*/src/main/AndroidManifest.xml`, `plugin/*/src/main/res/xml/plugin.xml`
- Triggers: Package manager discovery by plugin manifest action; optional service binding by `${mainApplicationId}.plugin.SERVICE`.
- Responsibilities: Provide Fcitx assets/native libraries and optional IPC-backed services.

**Gradle module graph:**
- Location: `settings.gradle.kts`
- Triggers: Gradle builds.
- Responsibilities: Includes `:app`, `:codegen`, shared libraries, native libraries, `:lib:plugin-base`, and all plugin modules.

## Architectural Constraints

- **Threading:** All native Fcitx API calls must run through `FcitxDispatcher`, which uses one `fcitx-main` thread and a queued `CoroutineDispatcher` (`app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt:38`).
- **Lifecycle:** Fcitx starts when the first `FcitxDaemon` client connects and stops when the last client disconnects (`app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt:86`, `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt:101`).
- **Global state:** Singleton/global state exists in `FcitxDaemon`, `FcitxApplication`, `AppPrefs`, `DataManager`, `FcitxPluginServices`, `ClipboardManager`, and `ThemeManager`.
- **Direct boot:** Preferences and data required before unlock use direct-boot-aware contexts/device-protected storage (`app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt:73`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:55`).
- **Plugin security:** Main app exposes signature permissions `${applicationId}.permission.IPC` and `${applicationId}.permission.PLUGIN` (`app/src/main/AndroidManifest.xml:29`); plugin service manifests use the main app placeholders (`plugin/clipboard-filter/src/main/AndroidManifest.xml:5`).
- **Native assets:** Native startup depends on descriptor-synced data and native library paths assembled before `startupFcitx()` (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:423`).
- **ABI packaging:** Native modules build for supported ABIs and disable split APKs for bundle tasks via `NativeBaseConventionPlugin` (`build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt:41`).
- **Circular imports:** Not detected by file inspection.

## Anti-Patterns

### Direct Fcitx Instance Access

**What happens:** Client code constructs or stores `Fcitx`/`FcitxAPI` directly instead of obtaining a `FcitxConnection`.
**Why it's wrong:** It bypasses daemon lifecycle gating, can leak the native engine facade, and can call native code outside the dispatcher/lifecycle contract.
**Do this instead:** Use `FcitxDaemon.connect(name)` and run operations through `FcitxConnection` (`app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt:29`, `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxConnection.kt:13`).

### Native Calls Outside Dispatcher

**What happens:** Kotlin code calls JNI-backed operations without `withFcitxContext`, `runOnReady`, or `launchOnReady`.
**Why it's wrong:** Native Fcitx assumes single-thread event-loop access and `FcitxDispatcher.dispatch()` wakes the native loop for queued work.
**Do this instead:** Add API methods to `FcitxAPI` and implement them in `Fcitx` using `withFcitxContext` (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:480`).

### Manual Asset Copying Outside Descriptors

**What happens:** App or plugin code writes packaged Fcitx data directly into the app data hierarchy.
**Why it's wrong:** `DataManager.sync()` computes actions from descriptors and can delete, update, or conflict with files it does not know about.
**Do this instead:** Put packaged files under module assets/resources and let `DataDescriptorPlugin` generate `descriptor.json`; use `fcitxComponent` for CMake-installed components (`build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt:59`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt:35`).

### Unregistered Input Windows

**What happens:** A keyboard/symbol/picker window is attached without being added to the dependency scope or known essential window map.
**Why it's wrong:** Broadcast receivers and dependency-managed components miss lifecycle/scope setup, and essential windows cannot preserve views across attaches.
**Do this instead:** Add essential windows through `InputWindowManager.addEssentialWindow()` and attach by key when applicable (`app/src/main/java/org/fcitx/fcitx5/android/input/wm/InputWindowManager.kt:64`).

## Error Handling

**Strategy:** Prefer guarded operations with logging, descriptor conflict classification, lifecycle state checks, and explicit fallback/default values.

**Patterns:**
- Application-level crash-loop handling stores crash time and opens `LogActivity` for release builds (`app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt:91`).
- Daemon lifecycle methods skip invalid start/stop states with Timber warnings (`app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt:532`).
- Plugin detection records parse/API/path failures in `failedPlugins` (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:171`).
- AIDL callbacks are wrapped in try/catch and death recipients unregister plugin clipboard transformers (`app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt:40`, `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt:83`).
- Preference reads reset corrupted values to defaults (`app/src/main/java/org/fcitx/fcitx5/android/data/prefs/ManagedPreference.kt:72`).
- Documents provider throws `FileNotFoundException` for failed file operations (`app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt:139`).

## Cross-Cutting Concerns

**Logging:** Use Timber in Kotlin app code (`app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt:124`) and route native Fcitx logs through `native_streambuf` (`app/src/main/cpp/native-lib.cpp:502`).

**Validation:** Plugin API compatibility and descriptor parse validation live in `DataManager.detectPlugins()` (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt:148`); descriptor symlink/file conflicts live in `DataHierarchy.install()` (`app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt:29`).

**Authentication:** No user auth provider. Inter-process access relies on Android signature permissions in `app/src/main/AndroidManifest.xml` and plugin manifests such as `plugin/clipboard-filter/src/main/AndroidManifest.xml`.

**Localization:** Android strings live under `app/src/main/res/values*`; Fcitx gettext files live under native `po` directories such as `app/src/main/cpp/po` and `lib/fcitx5/src/main/cpp/fcitx5/po`. `app/src/main/java/org/fcitx/fcitx5/android/utils/Locales.kt` adds `zh_CN`/`zh_TW` fallbacks for nonmatching-region `Hans`/`Hant` Chinese locales on Android N+.

**Build metadata:** Version/SDK/ABI constants live in `build-logic/convention/src/main/kotlin/Versions.kt`; plugin build placeholders for main app IDs are injected by `AndroidPluginAppConventionPlugin` (`build-logic/convention/src/main/kotlin/AndroidPluginAppConventionPlugin.kt:23`).

---

*Architecture analysis: 2026-11-17*
