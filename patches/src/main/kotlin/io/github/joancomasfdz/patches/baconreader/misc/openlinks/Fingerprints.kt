package io.github.joancomasfdz.patches.baconreader.misc.openlinks

import app.morphe.patcher.Fingerprint

internal object MainActivityOnResumeFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/ui/main/MainActivity;",
    name = "onResume",
    returnType = "V",
    parameters = listOf(),
)
