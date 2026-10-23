package io.github.joancomasfdz.patches.baconreader.fix.memes

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

internal object MemeImagesStaticInitializerFingerprint : Fingerprint(
    definingClass = "Lcom/handmark/drawchat/bitmapfun/provider/Images;",
    name = "<clinit>",
    filters = listOf(
        opcode(Opcode.RETURN_VOID),
    ),
)

internal object MemeGridItemClickFingerprint : Fingerprint(
    definingClass = "Lcom/handmark/drawchat/bitmapfun/ui/ImageGridFragment;",
    name = "onItemClick",
    returnType = "V",
    parameters = listOf("Landroid/widget/AdapterView;", "Landroid/view/View;", "I", "J"),
)
