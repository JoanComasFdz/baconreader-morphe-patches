package io.github.joancomasfdz.patches.baconreader.fix.spoilers

import app.morphe.patcher.Fingerprint

/** If an element of a comment's or post's HTML is a spoiler (>!...!<) the app hides. */
internal object IsInternalSpoilerFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/parser/RedditSpanner;",
    name = "isInternalSpoiler",
    returnType = "Z",
)
