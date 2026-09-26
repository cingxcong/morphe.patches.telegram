package app.template.patches.telegram.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsChatFingerprint
import app.template.patches.telegram.MessagesControllerIsChatNoForwardsLongFingerprint
import app.template.patches.telegram.MessagesControllerIsPeerNoForwardsFingerprint
import app.template.patches.telegram.MessagesControllerIsUserNoForwardsLongFingerprint
import app.template.patches.telegram.MessagesControllerIsUserNoForwardsUserFullFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val telegramBypassContentRestrictionsPatch = bytecodePatch(
    name = "Bypass content restrictions",
    description = "Allows saving and forwarding content from restricted channels, chats, and users.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)

    execute {
        // isChatNoForwards — both overloads
        listOf(
            MessagesControllerIsChatNoForwardsLongFingerprint,
            MessagesControllerIsChatNoForwardsChatFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // isUserNoForwards — both overloads (DM forward restrictions)
        listOf(
            MessagesControllerIsUserNoForwardsLongFingerprint,
            MessagesControllerIsUserNoForwardsUserFullFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // isPeerNoForwards — all three call sites
        listOf(
            MessagesControllerIsPeerNoForwardsFingerprint,
        ).forEach {
            it.method.addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }


        // UI helper methods were folded into the obfuscated ChatActivity in 12.10.5.
        // Controller-level isPeerNoForwards + canForwardMessage + field reads cover the behavior.

        // Patch all TLRPC$Message.noforwards field reads → false
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Message;",
            name = "noforwards",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }

        // Patch all TLRPC$Chat.noforwards field reads → false
        Fingerprint(filters = listOf(fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lorg/telegram/tgnet/TLRPC\$Chat;",
            name = "noforwards",
        ))).matchAllOrNull()?.forEach { match ->
            match.method.apply {
                match.instructionMatches.map { it.index }.reversed().forEach { idx ->
                    val reg = getInstruction<TwoRegisterInstruction>(idx).registerA
                    replaceInstruction(idx, "const/4 v$reg, 0x0")
                }
            }
        }
    }
}
