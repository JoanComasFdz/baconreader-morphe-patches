// SPDX-FileCopyrightText: 2026 JoanComasFdz
// SPDX-License-Identifier: GPL-3.0-only

package io.github.joancomasfdz.patches.baconreader.layout.postvideo

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import io.github.joancomasfdz.patches.baconreader.misc.extension.EXTENSION_PACKAGE
import io.github.joancomasfdz.patches.baconreader.misc.extension.sharedExtensionPatch
import io.github.joancomasfdz.patches.baconreader.misc.settings.markPatchIncluded
import io.github.joancomasfdz.patches.baconreader.misc.settings.settingsPatch
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER_PREMIUM

private const val EXTENSION_CLASS_DESCRIPTOR = "$EXTENSION_PACKAGE/postvideo/PostVideoFullScreenPatch;"

/**
 * The full screen's player view is made in code: this id, added to the app's ids, names it, so the e2e tests find it.
 * The extension finds it by name. Shared by the full screens of videos in comments and in posts.
 */
internal val videoFullScreenIdResourcePatch = resourcePatch {
    execute {
        document("res/values/ids.xml").use { ids ->
            ids.getElementsByTagName("resources").item(0).appendChild(
                ids.createElement("item").apply {
                    setAttribute("type", "id")
                    setAttribute("name", "baconreader_patches_video_full_screen")
                },
            )
        }
    }
}

// BaconReader's player in a post's detail view has no full screen: a video only shows as wide as the post, and the
// screen doesn't turn for a video wider than high.
@Suppress("unused")
val fullScreenPostVideosPatch = bytecodePatch(
    name = "Full screen post videos",
    description = "Adds a full screen button to the video of a post's detail view, as on videos in comments.",
) {
    dependsOn(sharedExtensionPatch, settingsPatch, videoFullScreenIdResourcePatch)

    compatibleWith(COMPATIBILITY_BACONREADER, COMPATIBILITY_BACONREADER_PREMIUM)

    execute {
        markPatchIncluded("fullScreenPostVideos")

        // onCreateVideo(player): the extension adds the button over the post's player view.
        OnCreateVideoFingerprint.method.addInstruction(
            0,
            "invoke-static { p0, p1 }, $EXTENSION_CLASS_DESCRIPTOR->addButton(Ljava/lang/Object;Ljava/lang/Object;)V",
        )
        // destroyVideo(boolean): the full screen closes before the player is released.
        DestroyVideoFingerprint.method.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->release(Ljava/lang/Object;)V",
        )
    }
}
