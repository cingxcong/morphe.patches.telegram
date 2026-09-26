package app.template.patches.telegram.ghost

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerSendTypingFingerprint
import app.template.patches.telegram.MessagesControllerSendTypingWithStringFingerprint

@Suppress("unused")
val telegramHideTypingPatch = bytecodePatch(
    name = "Hide typing indicator",
    description = "Hides your typing indicator from other users in all chats by silencing the controller-level sendTyping dispatcher.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // 4-arg overload is the wrapper used by ChatActivityEnterView.
        MessagesControllerSendTypingFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // 5-arg overload is the real dispatch implementation and is also called
        // directly by another 12.10.5 UI path.
        MessagesControllerSendTypingWithStringFingerprint.methodOrNull?.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)
    }
}
