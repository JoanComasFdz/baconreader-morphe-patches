// Compile-only stand-ins for BaconReader classes that the extension calls.
plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.joancomasfdz.extension.baconreader.stub"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
}
