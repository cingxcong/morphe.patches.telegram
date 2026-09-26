package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerStoriesEnabledFingerprint
import app.template.patches.telegram.MessagesControllerStoryEntitiesAllowedFingerprint
import app.template.patches.telegram.MessagesControllerStoryEntitiesAllowedUserFingerprint

@Suppress("unused")
val telegramStoriesFeaturesPatch = bytecodePatch(
    name = "Enable story features",
    description = "Keeps Telegram's story availability and story-entity capability gates enabled.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY)

    execute {
        MessagesControllerStoriesEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        MessagesControllerStoryEntitiesAllowedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        MessagesControllerStoryEntitiesAllowedUserFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
