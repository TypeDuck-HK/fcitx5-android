# Coding Conventions

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## Naming Patterns

**Files:**
- Use PascalCase for Kotlin type-bearing files: `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`.
- Use short domain names for Kotlin utility files that expose top-level extension functions: `app/src/main/java/org/fcitx/fcitx5/android/utils/Intent.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/Toast.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/FileUtil.kt`.
- Use `Functions.kt` only for package-local clusters that do not have a clearer domain name: `app/src/main/java/org/fcitx/fcitx5/android/daemon/Functions.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/dependency/Functions.kt`.
- Use `*Plugin.kt` for Gradle convention plugins: `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`, `build-logic/convention/src/main/kotlin/FcitxComponentPlugin.kt`.
- Use lowercase `test*.cpp` names for native C++ tests: `lib/libime/src/main/cpp/libime/test/testinputbuffer.cpp`, `plugin/chewing/src/main/cpp/fcitx5-chewing/test/testchewing.cpp`.

**Functions:**
- Use lower camelCase for Kotlin functions and methods: `export()` in `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `setupForest()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Timber.kt`, `enterMultiSelect()` in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`.
- Prefer receiver-style extension functions for Android framework conveniences: `Intent.parcelable()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Intent.kt`, `Context.toast()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Toast.kt`, `Timber.Forest.setupForest()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Timber.kt`.
- Use capitalized factory-like functions only when suppressing `FunctionName` for DSL/component constructors: `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListPreset.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/ColorFilter.kt`.
- Use C++ `test_*` helper names and a single `main()` for native unit executables: `lib/libime/src/main/cpp/libime/test/testinputbuffer.cpp`.

**Variables:**
- Use lower camelCase for regular locals and properties: `fakeCustomTheme`, `commitString`, `enabledIme` in `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt` and `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.
- Use a leading underscore for private mutable backing collections with read-only public views: `_entries` and `entries` in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`.
- Use `SCREAMING_SNAKE_CASE` for Kotlin constants only when they represent external or fallback constants: `FALLBACK_DESC` in `app/src/main/java/org/fcitx/fcitx5/android/utils/ClipboardEntryTransformer.kt`.
- Use concise lambda names only in local, obvious collection transforms; use descriptive names when values cross a boundary: `entry`, `index`, `timestamp` in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt` and `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt`.

**Types:**
- Use PascalCase for classes, objects, sealed classes, interfaces, and enum classes: `Theme`, `Theme.Custom`, `UserDataManager`, `ManagedPreference`, `KawaiiBarStateMachine` in `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/bar/KawaiiBarStateMachine.kt`.
- Use nested types when the type is only meaningful inside its owner: `Theme.Custom.CustomBackground` in `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt`, `ManagedPreference.PBool` in `app/src/main/java/org/fcitx/fcitx5/android/data/prefs/ManagedPreference.kt`.
- Use `object` for process-wide managers and state-machine definitions: `UserDataManager` in `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `ExpandButtonStateMachine` in `app/src/main/java/org/fcitx/fcitx5/android/input/bar/ExpandButtonStateMachine.kt`.
- Use explicit Gradle plugin class names ending in `ConventionPlugin` for build logic: `AndroidBaseConventionPlugin` in `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`.

## Code Style

**Formatting:**
- Use Kotlin official style. The project sets `kotlin.code.style=official` in `gradle.properties` and commits IntelliJ Kotlin official settings in `.idea/codeStyles/Project.xml`.
- Use 4-space indentation in Kotlin, Gradle Kotlin DSL, XML continuation indentation, and native C++ files: `.idea/codeStyles/Project.xml`, `app/build.gradle.kts`, `lib/libime/src/main/cpp/libime/test/testinputbuffer.cpp`.
- Keep SPDX copyright/license headers at the top of Kotlin and C++ source files: `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `build-logic/convention/src/main/kotlin/Versions.kt`, `plugin/chewing/src/main/cpp/fcitx5-chewing/test/testchewing.cpp`.
- Prefer expression bodies for short wrappers and accessors: `appContext` in `app/src/main/java/org/fcitx/fcitx5/android/utils/AppContext.kt`, `errorState()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Exception.kt`, `getItemCount()` in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`.
- Break chained builders and long constructor calls vertically. Follow the style in `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt` and `codegen/src/main/java/org/fcitx/fcitx5/android/codegen/GenKeyMapping.kt`.

**Linting:**
- Android lint is configured through Gradle convention plugins. `AndroidBaseConventionPlugin` sets SDK levels, Java compatibility, Kotlin compiler options, and disables only `UseKtx` in `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`.
- No standalone ktlint, detekt, Spotless, ESLint, Prettier, or Biome configuration is detected in the repository root.
- Use targeted suppressions near the API that needs them, with the narrowest suppression string: `@Suppress("DEPRECATION")` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Intent.kt`, `@SuppressLint("NotifyDataSetChanged")` in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`, `@Suppress("UnstableApiUsage")` in `build-logic/convention/src/main/kotlin/AndroidBaseConventionPlugin.kt`.
- Keep Gradle configuration in convention plugins instead of repeating settings in module build files: `app/build.gradle.kts` applies `org.fcitx.fcitx5.android.app-convention`, while shared behavior lives under `build-logic/convention/src/main/kotlin/`.

## Import Organization

**Order:**
1. File header, then `package`, then imports. This order is used in `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt` and `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`.
2. Kotlin imports follow the IntelliJ project import table from `.idea/codeStyles/Project.xml`: Android packages, AndroidX packages, third-party packages, project `org.*` packages, then `java.*`/`javax.*`.
3. Do not use wildcard imports in Kotlin source. Keep explicit imports as in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`.
4. For C++, include local generated/test headers first when required, then fcitx/library headers, then standard library headers, matching `plugin/chewing/src/main/cpp/fcitx5-chewing/test/testchewing.cpp`.

**Path Aliases:**
- Kotlin uses package imports rooted at `org.fcitx.fcitx5.android`; no TypeScript-style path aliases are present.
- Gradle modules are referenced through project paths such as `project(":lib:common")`, `project(":lib:fcitx5")`, and `project(":codegen")` in `app/build.gradle.kts`.
- Dependencies use the Gradle version catalog aliases from `gradle/libs.versions.toml`, for example `libs.kotlinx.coroutines`, `libs.androidx.room.runtime`, and `libs.junit`.

## Error Handling

**Patterns:**
- Use Kotlin `Result` via `runCatching` for import/export, filesystem, and user-data operations that should report failure without throwing through callers: `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/theme/ThemeFilesManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/FileUtil.kt`.
- Use localized exception helpers for user-visible validation failures: `errorState()`, `errorArg()`, and `errorRuntime()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Exception.kt`.
- Use nullable returns for recoverable absence: `Theme.Custom.CustomBackground.toDrawable()` returns `Drawable?` in `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt`; Room DAO lookups return nullable entities in `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt`.
- Catch broad framework/IPC exceptions only at Android or binder boundaries, then log and continue with a fallback: `app/src/main/java/org/fcitx/fcitx5/android/FcitxRemoteService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/ClipboardEntryTransformer.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`.
- Use `styledColorOrDefault()` from `app/src/main/java/org/fcitx/fcitx5/android/utils/Splitties.kt` when a framework theme color may be absent or unresolvable on a device.
- In native tests and native code, use `FCITX_ASSERT` for invariants and explicit exception checks for expected failures: `lib/libime/src/main/cpp/libime/test/testinputbuffer.cpp`.

## Logging

**Framework:** Timber for Kotlin app code; Android `Log` is wrapped by custom Timber trees.

**Patterns:**
- Initialize logging through `Timber.setupForest()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Timber.kt`.
- Use `Timber.d` for verbose lifecycle and state transitions: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/EventStateMachine.kt`.
- Use `Timber.i` for notable runtime events and instrumentation diagnostics: `app/src/main/java/org/fcitx/fcitx5/android/FcitxApplication.kt`, `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.
- Use `Timber.w` when swallowing or recovering from exceptions: `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/ClipboardManager.kt`, `app/src/main/java/org/fcitx/fcitx5/android/utils/ScrollbarDrawable.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/theme/ThemeFilesManager.kt`.
- Do not log secrets or raw credential values. Configuration files such as `.env` are not part of this repository scan.

## Comments

**When to Comment:**
- Use comments for non-obvious platform workarounds, build-system hacks, and compatibility constraints: `build-logic/convention/src/main/kotlin/AndroidAppConventionPlugin.kt`, `build-logic/convention/src/main/kotlin/NativeBuildTasks.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`.
- Use TODO/FIXME comments for known follow-up work with enough context to act on it: `codegen/src/main/java/org/fcitx/fcitx5/android/codegen/GenKeyMapping.kt`, `app/src/main/java/org/fcitx/fcitx5/android/data/theme/ThemeMonet.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/keyboard/KeyView.kt`.
- Avoid narrating obvious statements; most utilities in `app/src/main/java/org/fcitx/fcitx5/android/utils/` are uncommented unless Android API behavior needs explanation.

**JSDoc/TSDoc:**
- Not applicable. This is a Kotlin/Java/C++/Gradle repository.
- Kotlin KDoc is sparse and reserved for warnings or generated API notes, such as `addKdoc()` output in `codegen/src/main/java/org/fcitx/fcitx5/android/codegen/GenKeyMapping.kt` and lifecycle warnings in `build-logic/convention/src/main/kotlin/NativeBuildTasks.kt`.

## Function Design

**Size:** Keep utility functions small and single-purpose in `app/src/main/java/org/fcitx/fcitx5/android/utils/`. Larger Android services and UI components, such as `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt` and `app/src/main/java/org/fcitx/fcitx5/android/input/bar/KawaiiBarComponent.kt`, organize behavior as private helper methods around lifecycle callbacks.

**Parameters:** Prefer typed values and lambdas over unstructured maps. Use default parameter values for optional behavior: `DynamicListAdapter` constructor defaults in `app/src/main/java/org/fcitx/fcitx5/android/ui/common/DynamicListAdapter.kt`, `Theme.Builtin.deriveCustomBackground()` in `app/src/main/java/org/fcitx/fcitx5/android/data/theme/Theme.kt`, `Context.toast()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Toast.kt`.

**Return Values:** Use `Result<T>` for operations with recoverable failure, nullable types for absence, `Nothing` for helper functions that always throw, and `Unit` for Android lifecycle/event methods. Examples are `UserDataManager.import()` in `app/src/main/java/org/fcitx/fcitx5/android/data/UserDataManager.kt`, `ClipboardDao.get()` in `app/src/main/java/org/fcitx/fcitx5/android/data/clipboard/db/ClipboardDao.kt`, and `errorRuntime()` in `app/src/main/java/org/fcitx/fcitx5/android/utils/Exception.kt`.

## Module Design

**Exports:** Kotlin modules expose concrete classes, objects, extension functions, and Android components directly from package paths. Use package-level visibility modifiers (`private`, `internal`) rather than barrel exports. Codegen uses `internal` for processors and public provider classes in `codegen/src/main/java/org/fcitx/fcitx5/android/codegen/GenKeyMapping.kt`.

**Fragment Menus:** Own toolbar commands in lifecycle-bound `MenuProvider`s registered with `viewLifecycleOwner`, rather than storing action callbacks in `MainViewModel`. Use `app/src/main/java/org/fcitx/fcitx5/android/ui/main/EditDeleteMenuProvider.kt` for shared edit/delete behavior and `MainFragment.kt` for the main screen's about menu pattern.

**Barrel Files:** Barrel files are not used. Add new files directly under the package that owns the behavior, such as `app/src/main/java/org/fcitx/fcitx5/android/data/`, `app/src/main/java/org/fcitx/fcitx5/android/input/`, `app/src/main/java/org/fcitx/fcitx5/android/ui/`, or `app/src/main/java/org/fcitx/fcitx5/android/utils/`.

---

*Convention analysis: 2026-11-17*
