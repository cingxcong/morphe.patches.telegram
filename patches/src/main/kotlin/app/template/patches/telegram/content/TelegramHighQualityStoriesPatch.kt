package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerIsStoryQualityFullFingerprint

@Suppress("unused")
val telegramHighQualityStoriesPatch = bytecodePatch(
    name = "High quality stories",
    description = "Always selects Telegram's full-quality story document when a higher-quality alternate is supplied by the server.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY)

    execute {
        // 12.10.5: isStoryQualityFull() is consumed only by
        // TLRPC$MessageMedia.getDocument(). It selects alt_documents[0] when
        // the server supplied alternate story documents; otherwise the normal
        // media document remains the fallback.
        MessagesControllerIsStoryQualityFullFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
