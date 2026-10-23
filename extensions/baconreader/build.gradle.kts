extension {
    name = "extensions/baconreader.mpe"
}

android {
    namespace = "io.github.joancomasfdz.extension.baconreader"

    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    // Provided at runtime by extensions/shared.mpe.
    compileOnly(libs.morphe.extensions.library)
    compileOnly(libs.annotation)
    // Provided at runtime by BaconReader.
    compileOnly(libs.okhttp)
}
