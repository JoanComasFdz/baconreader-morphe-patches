package io.github.joancomasfdz.patches.baconreader.fix.savemedia

import app.morphe.patcher.Fingerprint

internal const val SAVE_IMAGE_SERVICE_CLASS = "Lcom/onelouder/baconreader/imageutils/SaveImageService;"

internal object DownloadToFileFingerprint : Fingerprint(
    definingClass = SAVE_IMAGE_SERVICE_CLASS,
    name = "getSingleFile",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;", "Landroidx/documentfile/provider/DocumentFile;"),
)
