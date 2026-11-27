// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.patches.baconreader.layout.postvideo

import app.morphe.patcher.Fingerprint

/** Binds the player to the post's player view and wires its controls, once the video plays. */
internal object OnCreateVideoFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/PostMediaHandler;",
    name = "onCreateVideo",
    returnType = "V",
    parameters = listOf("Lcom/google/android/exoplayer2/SimpleExoPlayer;"),
)

/** Releases the post's player, when the post closes or its media reloads. */
internal object DestroyVideoFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/PostMediaHandler;",
    name = "destroyVideo",
    returnType = "V",
    parameters = listOf("Z"),
)
