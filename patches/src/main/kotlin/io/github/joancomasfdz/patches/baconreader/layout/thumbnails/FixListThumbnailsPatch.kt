package io.github.joancomasfdz.patches.baconreader.layout.thumbnails

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import io.github.joancomasfdz.patches.baconreader.misc.extension.EXTENSION_PACKAGE
import io.github.joancomasfdz.patches.baconreader.misc.extension.sharedExtensionPatch
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER_PREMIUM

private const val EXTENSION_CLASS_DESCRIPTOR = "$EXTENSION_PACKAGE/FixListThumbnailsPatch;"

// Ported from ReVanced Patches (GPLv3): https://gitlab.com/ReVanced/revanced-patches
@Suppress("unused")
val fixListThumbnailsPatch = bytecodePatch(
    name = "Fix list thumbnails",
    description = "Fixes empty thumbnails in the post list, shows a preview as thumbnail for link posts " +
        "and crossposted galleries, and blurs the thumbnails of NSFW and spoiler posts.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(COMPATIBILITY_BACONREADER, COMPATIBILITY_BACONREADER_PREMIUM)

    execute {
        // Utils.canShowThumbnail(Link) is checked before every list and widget thumbnail.
        // Fix the thumbnail URL there, and show blurred NSFW thumbnails the app would hide.
        CanShowThumbnailFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->fixThumbnail(Ljava/lang/Object;)V
                    invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->forceShow(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :original
                    return v0
                """,
                ExternalLabel("original", getInstruction(0)),
            )
        }

        // LinkProcessor.loadThumbnail(Context, Link, ImageView) loads the thumbnail of a list row.
        LoadThumbnailFingerprint.let { fingerprint ->
            fingerprint.method.apply {
                // The thumbnail is loaded once: a network hiccup leaves it empty. Try again when it fails,
                // and look up the thumbnails of RedGIFs posts, which Reddit does not always send.
                // Done before the instructions are added at the start, which move the matched index.
                val intoIndex = fingerprint.instructionMatches.first().index
                val into = getInstruction<FiveRegisterInstruction>(intoIndex)
                if (getInstruction(intoIndex + 1).opcode == Opcode.MOVE_RESULT_OBJECT) removeInstruction(intoIndex + 1)
                replaceInstruction(
                    intoIndex,
                    "invoke-static { v${into.registerC}, v${into.registerD}, p1 }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->loadThumbnail(Ljava/lang/Object;Landroid/widget/ImageView;Ljava/lang/Object;)V",
                )

                addInstructions(
                    0,
                    """
                        invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->fixThumbnail(Ljava/lang/Object;)V
                        invoke-static { p1, p2 }, $EXTENSION_CLASS_DESCRIPTOR->beforeLoad(Ljava/lang/Object;Landroid/widget/ImageView;)V
                    """,
                )
            }
        }

        // The post list and the post header show the app's gallery indicator when Link.isGallery is true.
        // Crossposts of galleries are not marked as galleries: check the crossposted post too.
        listOf(LinkHolderUpdateViewFingerprint, DetailLinkFragmentResetHeaderFingerprint).forEach { fingerprint ->
            val method = fingerprint.method
            val index = fingerprint.instructionMatches.first().index
            val instruction = method.getInstruction<TwoRegisterInstruction>(index)
            method.replaceInstruction(
                index,
                "invoke-static { v${instruction.registerB} }, $EXTENSION_CLASS_DESCRIPTOR->isGallery(Ljava/lang/Object;)Ljava/lang/Boolean;",
            )
            method.addInstruction(index + 1, "move-result-object v${instruction.registerA}")
        }
    }
}
