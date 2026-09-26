package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerIsTranslationsAutoEnabledFingerprint
import app.template.patches.telegram.MessagesControllerIsTranslationsManualEnabledFingerprint

@Suppress("unused")
val telegramEnableTranslationsPatch = bytecodePatch(
    name = "Enable translations",
    description = "Keeps Telegram's automatic and manual message-translation gates enabled.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY)

    execute {
        // 12.10.5: these are the controller-level gates consumed by
        // TranslateController and the translation settings UI.
        MessagesControllerIsTranslationsAutoEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        MessagesControllerIsTranslationsManualEnabledFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
