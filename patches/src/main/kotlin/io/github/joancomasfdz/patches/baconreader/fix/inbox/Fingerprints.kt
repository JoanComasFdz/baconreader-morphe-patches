package io.github.joancomasfdz.patches.baconreader.fix.inbox

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.methodCall

/** "Mark read" / "Mark unread" of an inbox item. */
internal object SetPostStatusFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/adapters/InboxHelper;",
    name = "setPostStatus",
    returnType = "V",
)

/** Shows the menu of an inbox item: Utils.getAlertBuilder(context), then builder.setAdapter(menu, listener). */
internal object InboxMenuFingerprint : Fingerprint(
    definingClass = "Lcom/onelouder/baconreader/adapters/InboxHelper;",
    name = "onOpen",
    returnType = "V",
    filters = listOf(
        methodCall(name = "getAlertBuilder"),
        methodCall(name = "setAdapter", location = MatchAfterWithin(6)),
    ),
)
