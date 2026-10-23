package io.github.joancomasfdz.patches.baconreader.api

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.string

internal object GetAuthorizationUrlFingerprint : Fingerprint(
    filters = listOf(
        string("client_id=zACVn0dSFGdWqQ"),
    ),
)

internal object RequestTokenFingerprint : Fingerprint(
    filters = listOf(
        string("zACVn0dSFGdWqQ"),
        string("kDm2tYpu9DqyWFFyPlNcXGEni4k", StringComparisonType.CONTAINS),
    ),
)
