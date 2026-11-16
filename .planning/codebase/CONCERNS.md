# Codebase Concerns

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## Tech Debt

**Gradle native build integration uses private Android Gradle Plugin internals:**
- Issue: Native install tasks reflect into `ExternalNativeBuildJsonTask` and import internal AGP classes such as `com.android.build.gradle.internal.cxx.*`.
- Files: `build-logic/convention/src/main/kotlin/NativeBuildTasks.kt`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`, `build-logic/convention/src/main/kotlin/NativeBaseConventionPlugin.kt`
- Impact: AGP upgrades can break builds at runtime with renamed fields, moved classes, or changed CXX model behavior. The non-null assertions on `abi`, `cmake`, and `cmakeExe` make these failures hard crashes.
- Fix approach: Encapsulate the AGP-internal access behind one adapter with version checks and clear failure messages. Prefer public AGP Variant/Artifacts APIs where possible, and add a build-logic test or smoke task that exercises `installFcitxComponent` for every AGP upgrade.

**Large JNI bridge centralizes unrelated native responsibilities:**
- Issue: `app/src/main/cpp/native-lib.cpp` contains 1,292 lines covering daemon startup, event delivery, key sending, config conversion, dictionary conversion, custom phrase IO, and INI read/write.
- Files: `app/src/main/cpp/native-lib.cpp`, `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`
- Impact: JNI signatures, native lifecycle, and data conversion are tightly coupled. Changes to one area can regress unrelated IME behavior, and crash/debug boundaries are difficult to isolate.
- Fix approach: Split JNI entry points by domain, for example `fcitx-runtime-jni.cpp`, `config-jni.cpp`, `dictionary-jni.cpp`, and `customphrase-jni.cpp`. Keep `Fcitx.kt` external declarations grouped to match the native files.

**Singleton lifecycle and global mutable state are pervasive:**
- Issue: Runtime state is managed through process-wide singletons and companion-object flows.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/daemon/FcitxDaemon.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxLifecycle.kt`, `app/src/main/cpp/native-lib.cpp`
- Impact: Restart, test isolation, and multi-client behavior depend on strict state ordering. `FcitxLifecycleRegistry.ensureAt()` throws on unexpected transitions, and `Fcitx.stop()` only stops from READY state.
- Fix approach: Keep all Fcitx access through `FcitxDaemon.connect()` and `FcitxConnection`. Add lifecycle transition tests around STARTING, READY, STOPPING, and STOPPED, including restart during pending jobs.

**Vendored native source and prebuilt artifacts dominate the repository:**
- Issue: The repo contains many nested submodules and a shallow prebuilt dependency submodule.
- Files: `.gitmodules`, `lib/fcitx5/src/main/cpp/prebuilt`, `lib/fcitx5/src/main/cpp/fcitx5`, `lib/libime/src/main/cpp/libime`, `plugin/rime/src/main/cpp/rime-prelude`
- Impact: Dependency provenance, patch management, and upgrade review are complex. `lib/` is about 240 MB and includes architecture-specific prebuilts for Boost, libuv, OpenCC, librime, libchewing, libhangul, libthai, zstd, leveldb, Lua, and related data.
- Fix approach: Treat `lib/fcitx5/src/main/cpp/prebuilt` as a pinned binary supply-chain dependency. Document how each prebuilt is produced, store checksums, and keep submodule updates in separate reviewable changes.

**Clipboard transformer chain is plugin-driven and mutable at runtime:**
- Issue: Plugins can register `IClipboardEntryTransformer` instances, and every clipboard entry is synchronously transformed through all registered binders.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`, `plugin/clipboard-filter/src/main/java/org/fcitx/fcitx5/android/plugin/clipboard_filter/MainService.kt`, `plugin/clipboard-filter/src/main/java/org/fcitx/fcitx5/android/plugin/clipboard_filter/ClearURLs.kt`
- Impact: A slow, crashing, or repeatedly dying plugin can delay clipboard capture or produce inconsistent transformations. Remote failures are caught, but transformer latency and output validation are not bounded.
- Fix approach: Add timeout/budget enforcement around transformer calls, treat nullable transformer output as unchanged input, and expose transformer failures in plugin diagnostics.

## Known Bugs

**Android notification addon is a stub:**
- Symptoms: Fcitx notification requests return id `0`; only `showTip()` is surfaced as a toast.
- Files: `app/src/main/cpp/androidnotification/androidnotification.cpp`
- Trigger: Native addons that call `Notifications::sendNotification()` or expect close/action callbacks.
- Workaround: Tips still show through `androidfrontend()->call<IAndroidFrontend::showToast>()`, but action notifications are unavailable.

**DocumentsProvider path handling can resolve outside the intended root:**
- Symptoms: `fileFromDocId()` builds `File(docIdPrefix, docId)` directly, and `isChildDocument()` checks only `documentId.startsWith(parentDocumentId)`.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
- Trigger: Malformed or hostile document IDs passed to provider methods such as `openDocument()`, `deleteDocument()`, `renameDocument()`, or `moveDocument()`.
- Workaround: The provider is protected by `android.permission.MANAGE_DOCUMENTS`, but the code should still canonicalize and reject paths outside `baseDir`.

**Image crop result depends on temp-file transfer and force unwraps:**
- Symptoms: Crop completion can fail if `getCroppedImage()` returns null, `cropRect` is unavailable, or temp file decoding/deletion fails.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/ui/main/CropImageActivity.kt`
- Trigger: Large images, unusual source URIs, edit mode with initial rect plus rotation, or crop library edge cases.
- Workaround: Exceptions are caught in `onCropImage()` and reported with a toast; `CropResult.Success.bitmap` lazily decodes from the temp file and deletes it.

**Android keyboard surrounding-text behavior is incomplete:**
- Symptoms: Space insertion and spell delimiter handling can be wrong after cursor movement or for delimiters beyond `-`.
- Files: `app/src/main/cpp/androidkeyboard/androidkeyboard.cpp`
- Trigger: Cursor movement during Android keyboard composition or spelling input containing delimiters like spaces, punctuation, or underscores.
- Workaround: Current implementation handles the common hyphen case and resets state on input method changes.

**Potential Kotlin null-assertion crashes remain in UI/service utilities:**
- Symptoms: Several UI and system-service paths use `!!` where Android framework objects can be absent on vendor builds, lifecycle races, or unusual contexts.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/Intent.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/InputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/SystemService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListEntryUi.kt`
- Trigger: Missing `Window`, absent system services, missing parcel extras, missing drawables, or platform-specific behavior.
- Workaround: Many call sites are reached only after normal Android lifecycle setup. Replace `!!` with explicit errors for invariants and graceful fallbacks for framework data.

## Security Considerations

**Backups may include sensitive clipboard/history/preferences data:**
- Risk: `android:allowBackup="true"` is enabled and backup rules exclude only `usr`, descriptor, license, and README files. Room databases and shared preferences are not excluded here.
- Files: `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/full_backup_content.xml`, `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDatabase.kt`
- Current mitigation: Data hierarchy assets under `usr` are excluded from cloud backup and device transfer.
- Recommendations: Exclude clipboard databases and sensitive preferences explicitly, or document why they are safe to back up. Consider a setting that disables clipboard history backup independently from app backup.

**Exported surfaces rely on signature/system permissions:**
- Risk: Main activity file import, IME service, remote IPC service, documents provider, and plugin services are exported.
- Files: `app/src/main/AndroidManifest.xml`, `plugin/clipboard-filter/src/main/AndroidManifest.xml`, `lib/plugin-base/src/main/AndroidManifest.xml`, `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
- Current mitigation: `FcitxRemoteService` uses `${applicationId}.permission.IPC` with signature protection, plugin services use `${applicationId}.permission.PLUGIN`, the IME requires `android.permission.BIND_INPUT_METHOD`, and the documents provider requires `android.permission.MANAGE_DOCUMENTS`.
- Recommendations: Keep permission checks close to the service/provider code as defense in depth. Add tests or lint checks verifying exported components retain their intended permissions.

**Data descriptor paths from plugins drive file creation, deletion, and symlink creation:**
- Risk: Plugin descriptors can cause `DataManager.sync()` to create/update/delete files and create symlinks under the Fcitx data directory.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/FileAction.kt`
- Current mitigation: Plugin APK loading is signature-protected by manifest permissions, and `DataHierarchy` detects path conflicts.
- Recommendations: Validate descriptor paths and symlink targets for canonical containment under `DataManager.dataDir`. Reject absolute paths, `..` segments, and symlink targets that escape the app data tree.

**Private Android APIs are used for behavior and UI polish:**
- Risk: Reflection against hidden platform APIs can break on OEM builds or new Android releases.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/utils/SystemProperty.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/ScrollbarDrawable.kt`
- Current mitigation: `SystemProperty.kt` catches exceptions and returns an empty string; `ScrollbarDrawable.kt` catches reflection failures and logs warnings.
- Recommendations: Keep private API use isolated in these utility files. Ensure every call site tolerates empty or unchanged results.

## Performance Bottlenecks

**Fcitx event flow can drop events under load:**
- Problem: Native events are emitted into a `MutableSharedFlow` with `extraBufferCapacity = 15` and `BufferOverflow.DROP_OLDEST`.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
- Cause: Rapid candidate, preedit, key, and status events can exceed the buffer while collectors are busy.
- Improvement path: Separate high-frequency display events from state-critical events. Use conflated state holders for latest candidate/preedit state and reliable channels for lifecycle/config events.

**FcitxDispatcher queue has no backpressure:**
- Problem: `FcitxDispatcher` queues `WrappedRunnable` work in a `ConcurrentLinkedQueue` and only logs when jobs wait more than 2 seconds.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxDispatcher.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
- Cause: The native `uv_run(..., UV_RUN_ONCE)` loop and queued coroutine work share one thread, and `postFcitxJob()` accepts an unlimited channel.
- Improvement path: Add queue size metrics, coalesce redundant UI/config jobs, and bound input-service job channels where dropping stale work is acceptable.

**Clipboard history trim loads all unpinned rows:**
- Problem: `removeOutdated()` reads all unpinned entries, sorts in memory, and marks older entries deleted.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt`
- Cause: Trimming is implemented in Kotlin instead of as a bounded SQL delete/update.
- Improvement path: Move trimming into DAO queries using limit/offset or timestamp subqueries, and index deleted/pinned/timestamp columns if not already indexed.

**DocumentsProvider search walks the full external file tree:**
- Problem: `querySearchDocuments()` uses `fileFromDocId(rootId).walk()` and filters in Kotlin.
- Files: `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
- Cause: Search has no index and performs synchronous filesystem traversal before returning up to 50 rows.
- Improvement path: Bound traversal by directory depth or maintain an index for user-facing data. Check `CancellationSignal` during traversal.

**URL cleanup applies many regex rules on every clipboard URL:**
- Problem: `ClearURLs.transformWith()` iterates all providers and applies provider regexes, redirections, raw rules, query rules, and fragment rules.
- Files: `plugin/clipboard-filter/src/main/java/org/fcitx/fcitx5/android/plugin/clipboard_filter/ClearURLs.kt`
- Cause: The full ClearURLs catalog is evaluated per matching clipboard text.
- Improvement path: Pre-index providers by host/domain patterns where possible and cap transformation time in `FcitxRemoteService`.

## Fragile Areas

**IME service is a large lifecycle-heavy class:**
- Files: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
- Why fragile: The class owns lifecycle connection, event collection, input view replacement, hardware/virtual keyboard routing, cursor prediction, inline suggestions, and Android configuration handling in 1,123 lines.
- Safe modification: Keep changes small and route native work through `postFcitxJob()` to preserve ordering. Test focus changes, keyboard visibility, hardware keyboard input, subtype switching, and configuration changes.
- Test coverage: Only `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt` exercises Fcitx core behavior; IME UI/lifecycle behavior has no direct instrumentation coverage.

**Native Android frontend bridges Fcitx and Java UI state:**
- Files: `app/src/main/cpp/androidfrontend/androidfrontend.cpp`, `app/src/main/cpp/androidfrontend/inputcontextcache.h`, `app/src/main/cpp/native-lib.cpp`
- Why fragile: Input contexts, candidate events, preedit, cursor movement, and Java callbacks cross C++/JNI/Kotlin boundaries.
- Safe modification: Keep JNI object conversions centralized in `app/src/main/cpp/object-conversion.h` and `app/src/main/cpp/jni-utils.h`. Add instrumentation tests for every new event type.
- Test coverage: Core Fcitx candidate and commit behavior is covered by `FcitxTest.kt`, but frontend callback sequencing and Android `InputConnection` behavior are not covered.
- The empty-panel overlay-message path in `androidfrontend.cpp` also has no dedicated test.

**Data sync performs destructive filesystem migration:**
- Files: `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataHierarchy.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/FileUtil.kt`
- Why fragile: A descriptor diff can delete files/directories, update assets, and recreate symlinks. Failures occur during startup before the native daemon is ready.
- Safe modification: Validate descriptors before applying actions, write migrations to a temp tree where possible, and preserve user-modifiable files separately from managed assets.
- Test coverage: No app JVM tests exercise descriptor merge, diff ordering, path validation, plugin conflicts, or partial migration failure.

**Build workflows assemble release artifacts but do not run tests:**
- Files: `.github/workflows/pull_request.yml`, `.github/workflows/nix.yml`, `app/build.gradle.kts`
- Why fragile: PR CI runs `./gradlew :app:assembleRelease` and `./gradlew :assembleReleasePlugins`; it does not run `test`, `connectedAndroidTest`, or native test targets.
- Safe modification: Add at least `./gradlew :app:testDebugUnitTest` to PR CI and a scheduled/native job for C++ tests that are practical on CI.
- Test coverage: Android app has `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`, `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`, and `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`; native upstream trees contain many tests but are not visibly invoked by the PR workflow.

## Scaling Limits

**Plugin and submodule count raises release coordination cost:**
- Current capacity: The Gradle settings include the app, four native library modules, plugin-base, codegen, and eight plugin APK modules.
- Limit: Adding more plugin modules increases native build time, asset sync complexity, Play/F-Droid packaging work, and IPC compatibility surface.
- Scaling path: Keep plugin APIs versioned through `PluginDescriptor.pluginAPI`, add compatibility tests for `lib/common/src/main/aidl`, and automate plugin package verification in CI.

**Clipboard history is local Room storage with app-process cleanup:**
- Current capacity: Governed by `AppPrefs.getInstance().clipboard.clipboardHistoryLimit`.
- Limit: Large histories make trimming and UI paging depend on local database query shape and cleanup frequency.
- Scaling path: Keep history limit bounded, push trim/delete operations into SQL, and add database-size telemetry or diagnostics.

**DocumentsProvider search is capped but not indexed:**
- Current capacity: Search returns up to 50 results.
- Limit: Traversal cost grows with the number of files under external app data even though result count is capped.
- Scaling path: Add cancellation checks and index known managed/user data paths.

## Dependencies at Risk

**Android Gradle Plugin 9.4.1 internal APIs:**
- Risk: Internal CXX APIs and reflected fields are not compatibility contracts.
- Impact: Native install tasks can fail during Gradle configuration or execution.
- Migration plan: Move toward public AGP APIs and keep a version-gated adapter for unavoidable internal access.

**Splitties 3.0.0 UI DSL:**
- Risk: Several UI screens are built with Splitties view DSL while unwanted Splitties modules are excluded globally.
- Impact: Dependency upgrades or transitive changes can break UI construction patterns across `app/src/main/java/org/fcitx/fcitx5/android/ui` and `app/src/main/java/org/fcitx/fcitx5/android/input`.
- Migration plan: Keep new UI consistent with existing DSL for now; isolate any future migration to AndroidX/Compose by feature area.

**Image cropper dependency:**
- Risk: `com.vanniktech:android-image-cropper` drives custom theme image workflows and requires manifest component removal.
- Impact: Crop behavior and manifest merging affect `CropImageActivity` and theme editing.
- Migration plan: Wrap cropper-specific APIs behind local helpers in `app/src/main/java/org/fcitx/fcitx5/android/ui/main/CropImageActivity.kt` before replacing or upgrading the library.

**Prebuilt native dependency submodule:**
- Risk: `lib/fcitx5/src/main/cpp/prebuilt` is a binary/source supply-chain input outside Gradle dependency metadata.
- Impact: ABI mismatches, stale security fixes, or missing architectures can break native builds or runtime loading.
- Migration plan: Track checksums, generation scripts, and supported ABIs for every prebuilt package. Keep prebuilt refreshes separate from application behavior changes.

## Missing Critical Features

**Native notification actions are not implemented:**
- Problem: Addons cannot deliver Android notifications with action callbacks through `androidnotification`.
- Blocks: Any feature relying on interactive native notification behavior.

**Descriptor path containment validation is missing:**
- Problem: Data sync and documents provider code build filesystem paths from descriptor/document IDs without canonical containment checks in the same layer.
- Blocks: Confident expansion of plugin-managed data and user-facing file management.

**Automated regression coverage for IME UI/lifecycle is minimal:**
- Problem: Input window replacement, cursor tracking, hardware keyboard mode, inline suggestions, configuration changes, and plugin IPC lack dedicated automated tests.
- Blocks: Safe refactors of `FcitxInputMethodService`, `InputView`, candidates UI, and daemon lifecycle.

## Test Coverage Gaps

**Android app unit coverage:**
- What's not tested: Preference migration, data descriptor diffing, clipboard history trimming, provider path safety, ClearURLs transformations, crop flow, and lifecycle edge cases.
- Files: `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`, `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`, `app/src/main/java/org/fcitx/fcitx5/android/core/data/DataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`, `plugin/clipboard-filter/src/main/java/org/fcitx/fcitx5/android/plugin/clipboard_filter/ClearURLs.kt`
- Risk: Core app behavior can regress while release builds still pass.
- Priority: High

**Instrumentation coverage:**
- What's not tested: Actual IME `InputConnection` interactions, window insets, candidate layouts, hardware keyboard switching, Android 14 subtype sync, remote plugin service behavior, and DocumentsProvider operations.
- Recent changes also lack dedicated coverage for keyboard height base selection, custom multi-pointer touch dispatch/popup cancellation, fragment menu lifecycle, and setup focus synchronization.
- Files: `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/provider/FcitxDataProvider.kt`
- Risk: Device/OEM/API-level regressions are likely to be caught manually rather than in CI.
- Priority: High

**Build-logic coverage:**
- What's not tested: Convention plugins, AGP internal adapter behavior, component install tasks, metadata generation, and native install ordering.
- Files: `build-logic/convention/src/main/kotlin/NativeBuildTasks.kt`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`, `build-logic/convention/src/main/kotlin/DataDescriptorPlugin.kt`
- Risk: Build failures appear late in release workflows and can be hard to diagnose.
- Priority: Medium

**Native Android glue coverage:**
- What's not tested: JNI conversion error paths, lifecycle restart while events are pending, native dictionary conversion exceptions, and Android frontend event sequencing.
- Files: `app/src/main/cpp/native-lib.cpp`, `app/src/main/cpp/androidfrontend/androidfrontend.cpp`, `app/src/main/cpp/androidkeyboard/androidkeyboard.cpp`, `app/src/main/cpp/androidnotification/androidnotification.cpp`
- Risk: Native crashes or event-order bugs can bypass Kotlin tests.
- Priority: High

---

*Concerns audit: 2026-11-17*
