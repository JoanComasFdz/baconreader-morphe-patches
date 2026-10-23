package io.github.joancomasfdz.patches.baconreader.misc.extension

import app.morphe.patches.all.misc.extension.activityOnCreateExtensionHook
import app.morphe.patches.all.misc.extension.sharedExtensionPatch

// Passes the application context to the extension code when BaconReader starts.
internal val sharedExtensionPatch = sharedExtensionPatch(
    "baconreader",
    activityOnCreateExtensionHook("Lcom/onelouder/baconreader/BaconReader;"),
)
