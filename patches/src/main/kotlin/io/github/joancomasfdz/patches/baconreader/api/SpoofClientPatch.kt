package io.github.joancomasfdz.patches.baconreader.api

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patches.all.misc.string.replaceStringPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER
import io.github.joancomasfdz.patches.shared.Constants.COMPATIBILITY_BACONREADER_PREMIUM

private const val REDIRECT_URI = "http://baconreader.com/auth"

// Ported from ReVanced Patches (GPLv3): https://gitlab.com/ReVanced/revanced-patches
@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID.",
) {
    compatibleWith(COMPATIBILITY_BACONREADER, COMPATIBILITY_BACONREADER_PREMIUM)

    dependsOn(
        // Redirects from SSL to WWW domain are bugged causing auth problems.
        // Manually rewrite the URLs to fix this.
        replaceStringPatch("ssl.reddit.com", "www.reddit.com", comparison = StringComparisonType.CONTAINS),
    )

    val clientIdOption = stringOption(
        key = "clientId",
        title = "OAuth client ID",
        description = "The Reddit OAuth client ID. " +
            "You can get your client ID from https://www.reddit.com/prefs/apps. " +
            "The application type has to be \"Installed app\" " +
            "and the redirect URI has to be set to \"$REDIRECT_URI\".",
        required = true,
    )

    execute {
        val clientId = clientIdOption.value!!

        fun Fingerprint.replaceClientId(replacementString: String) {
            val clientIdIndex = instructionMatches.first().index
            val clientIdRegister = method.getInstruction<OneRegisterInstruction>(clientIdIndex).registerA

            method.replaceInstruction(
                clientIdIndex,
                "const-string v$clientIdRegister, \"$replacementString\"",
            )
        }

        // Patch client id in authorization url.
        GetAuthorizationUrlFingerprint.replaceClientId("client_id=$clientId")

        // Patch client id for access token request.
        RequestTokenFingerprint.replaceClientId(clientId)
    }
}
