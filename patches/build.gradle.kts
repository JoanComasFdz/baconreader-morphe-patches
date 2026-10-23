group = "io.github.joancomasfdz"

patches {
    about {
        name = "JoanComasFdz BaconReader Patches"
        description = "BaconReader patches for use with Morphe"
        source = "git@github.com:JoanComasFdz/baconreader-morphe-patches.git"
        author = "Joan Comas"
        contact = "https://github.com/JoanComasFdz/baconreader-morphe-patches/issues"
        website = "https://github.com/JoanComasFdz/baconreader-morphe-patches"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
