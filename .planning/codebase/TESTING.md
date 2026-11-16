# Testing Patterns

**Analysis Date:** 2026-11-17
**Revision:** `eb5b5021` (incremental review from `1d13d2e9`)

## Test Framework

**Runner:**
- JUnit 4.13.2 for JVM unit tests under `app/src/test/java/`.
- AndroidX Test runner 1.7.0 for instrumentation tests under `app/src/androidTest/java/`.
- Native C++ tests use CMake/CTest-style `add_test()` targets in upstream native trees such as `lib/fcitx5/src/main/cpp/fcitx5/test/CMakeLists.txt`, `lib/libime/src/main/cpp/libime/test/CMakeLists.txt`, and `plugin/chewing/src/main/cpp/fcitx5-chewing/test/CMakeLists.txt`.
- Config: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `lib/fcitx5/src/main/cpp/CMakeLists.txt`.

**Assertion Library:**
- Kotlin app tests use `org.junit.Assert` and `@Test` from JUnit 4: `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`, `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`.
- Native C++ tests use `FCITX_ASSERT`: `lib/libime/src/main/cpp/libime/test/testinputbuffer.cpp`, `plugin/chewing/src/main/cpp/fcitx5-chewing/test/testchewing.cpp`.

**Run Commands:**
```bash
./gradlew :app:testDebugUnitTest              # Run app JVM unit tests
./gradlew :app:connectedDebugAndroidTest      # Run app instrumentation tests on a connected device/emulator
./gradlew :app:lintDebug                      # Run Android lint for the app debug variant
./gradlew :app:assembleRelease                # CI build check used by pull_request workflow
./gradlew :assembleReleasePlugins             # CI plugin build check used by pull_request workflow
```

## Test File Organization

**Location:**
- App JVM unit tests are co-located under the app module at `app/src/test/java/org/fcitx/fcitx5/android/`.
- App instrumentation tests are under `app/src/androidTest/java/org/fcitx/fcitx5/android/`.
- Native C++ tests are inside each upstream/native component's `test/` directory, for example `lib/fcitx5/src/main/cpp/fcitx5/test/`, `lib/libime/src/main/cpp/libime/test/`, `plugin/anthy/src/main/cpp/fcitx5-anthy/test/`, and `plugin/chewing/src/main/cpp/fcitx5-chewing/test/`.
- Third-party tests exist under vendored code such as `lib/fcitx5/src/main/cpp/fcitx5/third_party/yoga/`; do not use those as app test style references.

**Naming:**
- Kotlin tests use `*Test.kt`: `StringEscapeTest.kt`, `ThemeSerializationTest.kt`, `FcitxTest.kt`.
- Kotlin test classes match file names and use PascalCase: `StringEscapeTest`, `ThemeSerializationTest`, `FcitxTest`.
- Native tests use lowercase `test*.cpp` executable names: `testinputbuffer.cpp`, `testchewing.cpp`, `testpinyinime_unit.cpp`.

**Structure:**
```text
app/src/test/java/org/fcitx/fcitx5/android/
├── StringEscapeTest.kt
└── ThemeSerializationTest.kt

app/src/androidTest/java/org/fcitx/fcitx5/android/
└── FcitxTest.kt

lib/*/src/main/cpp/*/test/
├── CMakeLists.txt
├── test*.cpp
└── testdir.h.in
```

## Test Structure

**Suite Organization:**
```kotlin
class StringEscapeTest {

    private val data = listOf(
        "\"" to """"\""""",
        "abc" to """abc"""
    )

    @Test
    fun testEscapeForValue() {
        data.forEach {
            Assert.assertEquals(FcitxUtils.escapeForValue(it.first), it.second)
        }
    }
}
```

**Patterns:**
- Keep shared JVM test data as private properties inside the test class, as in `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`.
- Use private extension helpers to hide serialization setup inside tests: `Theme.Custom.toJson()` and `String.toCustomTheme()` in `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`.
- Use `@BeforeClass`/`@AfterClass` for expensive instrumentation resources and `@Before`/`@After` for per-test state restore: `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.
- Use `runBlocking` around coroutine-based instrumentation tests and helper methods: `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.
- Use `Assert.assertEquals(message, expected, actual)` when the assertion needs migration or round-trip context: `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`.

## Mocking

**Framework:** Not detected for app tests. No Mockito, MockK, Robolectric, Turbine, or coroutine test dispatcher dependency is declared in `app/build.gradle.kts` or `gradle/libs.versions.toml`.

**Patterns:**
```kotlin
private companion object {
    lateinit var fcitx: Fcitx
    val fcitxEventChannel = Channel<FcitxEvent<*>>(capacity = Channel.CONFLATED)
    val scope = MainScope()

    @BeforeClass
    @JvmStatic
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fcitx = Fcitx(context)
        fcitx.eventFlow.onEach { fcitxEventChannel.send(it) }.launchIn(scope)
        fcitx.start()
    }
}
```

**What to Mock:**
- Prefer real serializers, managers, and data objects for JVM tests when they do not require Android framework services: `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`.
- Prefer real `Fcitx` integration in instrumentation tests when behavior depends on native engine state: `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.
- Introduce a mocking library only after adding its dependency to `gradle/libs.versions.toml` and the relevant module `build.gradle.kts`.

**What NOT to Mock:**
- Do not mock `FcitxUtils.escapeForValue()`/`unescapeForValue()` round trips; assert actual output pairs as in `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`.
- Do not mock native input-method behavior in instrumentation tests that validate committed strings, candidate lists, or preedit state; use the real `Fcitx` instance pattern in `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt`.

## Fixtures and Factories

**Test Data:**
```kotlin
private val data = listOf(
    "\"" to """"\""""",
    "\"\"\n" to """"\"\"\n"""",
    "abc" to """abc""",
    "ab\"c" to """"ab\"c"""",
    "a c" to """"a c""""
)
```

**Location:**
- Small JVM fixtures live inline in the test class: `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`.
- Larger JSON migration fixtures live as raw triple-quoted strings inside the test method that owns the fixture: `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`.
- Native fixture files live beside native tests: `lib/fcitx5/src/main/cpp/fcitx5/test/testdir.h.in`, `plugin/chewing/src/main/cpp/fcitx5-chewing/test/inputmethod/CMakeLists.txt`, `lib/fcitx5-chinese-addons/src/main/cpp/fcitx5-chinese-addons/test/inputmethod/sim.conf`.
- Room schema snapshots are committed under `app/schemas/org.fcitx.fcitx5.android.data.clipboard.db.ClipboardDatabase/`; use this directory when Room schema-affecting changes are made.

## Coverage

**Requirements:** None enforced. No JaCoCo configuration, Gradle coverage task, or coverage threshold is detected in `build.gradle.kts`, `app/build.gradle.kts`, or `gradle/libs.versions.toml`.

**View Coverage:**
```bash
# Not configured. Add JaCoCo/Android coverage configuration before relying on coverage reports.
```

## Test Types

**Unit Tests:**
- Scope: pure Kotlin/JVM behavior that can run without Android instrumentation.
- Current examples: serialization migration and round-trip behavior in `app/src/test/java/org/fcitx/fcitx5/android/ThemeSerializationTest.kt`; Fcitx string escaping in `app/src/test/java/org/fcitx/fcitx5/android/StringEscapeTest.kt`.
- String escaping fixtures follow upstream Fcitx 5.1.21 and include non-ASCII text with trailing space and the full supported control-character escape set; the port lives in `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxUtils.kt`.
- Add new tests under `app/src/test/java/org/fcitx/fcitx5/android/` unless a narrower package already exists.

**Integration Tests:**
- Scope: Android instrumentation plus native Fcitx engine behavior.
- Current example: `app/src/androidTest/java/org/fcitx/fcitx5/android/FcitxTest.kt` starts a real `Fcitx` instance, enables input methods, sends keys, waits on event flows, and verifies commit/candidate/preedit behavior.
- Use instrumentation tests for behavior requiring `InstrumentationRegistry`, Android context, input method state, or JNI/native runtime behavior.

**E2E Tests:**
- No UI automation framework such as Espresso, UI Automator, or Compose test is detected in app dependencies.
- CI pull requests build release APKs and plugins on Ubuntu, macOS, and Windows in `.github/workflows/pull_request.yml`; this is build verification, not end-to-end user-flow testing.

## Common Patterns

**Async Testing:**
```kotlin
@Test
fun testPinyin(): Unit = runBlocking {
    fcitx.setEnabledIme(arrayOf("pinyin"))
    sendString("nihaoshijie")
    fcitx.select(0)
    val commitString = receiveFirstCommitString()?.data
    Assert.assertEquals("你好世界", commitString)
    fcitx.reset()
}
```

**Error Testing:**
```kotlin
@Test
fun version1() {
    val (decoded, migrated) = raw.toCustomTheme()
    Assert.assertEquals("Migration should happen", true, migrated)
    Assert.assertEquals("Round trip", decoded, decoded.toJson().toCustomTheme().first)
}
```

**Native Testing:**
```cpp
void test_utf8() {
    using namespace libime;
    InputBuffer buffer;
    buffer.type("\xe4\xbd\xa0\xe5\xa5\xbd");
    FCITX_ASSERT(buffer.size() == 2);
    bool throwed = false;
    try {
        buffer.type("\xe4\xbd");
    } catch (const std::invalid_argument &e) {
        throwed = true;
    }
    FCITX_ASSERT(throwed);
}
```

---

*Testing analysis: 2026-11-17*
