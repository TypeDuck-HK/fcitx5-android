plugins {
    id("org.fcitx.fcitx5.android.lib-convention")
    id("org.fcitx.fcitx5.android.native-lib-convention")
}

android {
    namespace = "hk.eduhk.typeduck.rime"

    defaultConfig {
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                targets("rime")
            }
        }
    }

    prefab {
        create("rime") {
            libraryName = "librime"
            headers = "src/main/cpp/fcitx5-rime/src"
        }
    }
}

dependencies {
    implementation(project(":lib:fcitx5"))
}
