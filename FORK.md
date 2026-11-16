# TypeDuck Android Fork

## Ownership

TypeDuck behavior belongs in TypeDuck-owned files. Keep edits to inherited IME
infrastructure focused on integration. Use a short `TypeDuck:` comment when the
reason for an integration change is not obvious; paired `TYPEDUCK BEGIN/END`
comments are reserved for substantial embedded blocks. Product-owned surfaces
may be replaced completely instead of distributing hooks throughout upstream code.

## Branches and Remotes

- `origin`: `https://github.com/TypeDuck-HK/fcitx5-android`
- `upstream`: `https://github.com/fcitx5-android/fcitx5-android`
- `main`: TypeDuck integration and default branch.
- `codex/<topic>`: short-lived implementation or upstream-sync branches.

Merge upstream revisions on a dedicated sync branch. Keep upstream merges separate
from TypeDuck adaptation commits; preserve published history. Review changes to
IME lifecycle, editor interaction, threading, JNI and build tooling during every
sync. Do not push TypeDuck changes to upstream.

## Native Build Pipeline

`TypeDuck-HK/fcitx5-android-prebuilder` builds the native dependencies.
`TypeDuck-HK/fcitx-android-cpp-prebuilt` stores its generated outputs.
The Android repo pins those outputs at `lib/fcitx5/src/main/cpp/prebuilt`.

The internal `lib/rime` module builds the Fcitx Rime bridge against TypeDuck's
static librime and dictionary lookup filter. It is bundled in the app, not
distributed as an additional APK. `lib/rime/src/main/schema` pins the complete
TypeDuck schema package; the app packages its runtime data together.

Commit prebuilder sources before generating artifacts. Publish generated outputs
before updating the Android prebuilt pin, so a fresh recursive clone is reproducible.
Keep schema, engine and filter revisions pinned and verify them together.

## Local Build

Use JDK 17, Android SDK 36, Build Tools 36.1.0, NDK 28.0.13004108,
and SDK CMake 3.31.6. `Versions.kt` is authoritative for Android tool versions.
Install Extra CMake Modules and GNU gettext on the host.

```sh
git submodule update --init --recursive
./gradlew :app:assembleDebug -PbuildABI=arm64-v8a
```

The prebuilder additionally requires GHC/Cabal and the dependencies listed in its
README. Run its selected artifact target, not targets for removed engines.

The upstream Jenkins/F-Droid distribution metadata is not a TypeDuck release
configuration. TypeDuck application identity, signing and release distribution
must be configured before publishing an APK to users.
