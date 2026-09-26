# Telegram 12.10.5 Fingerprint Port Matrix

Target: org.telegram.messenger 12.10.5 / versionCode 71052.

## Checkpoint

- Rushi signature inventory checked: 79 named/signature-based fingerprints.
- Exact class + method + descriptor definitions found in the shipped 12.10.5 DEX: 51.
- Old definitions absent: 28.
- Exact matches are not automatically accepted as behavioral matches.

## Confirmed exact targets

UserConfig.isPremium():Z
MessagesController.isPremiumUser(User):Z
AndroidUtilities.getCertificateSHA256Fingerprint():String
MessagesController.isSponsoredDisabled():Z
MessageObject.isSponsored():Z
VideoAds.load():V
SharedConfig.isAppUpdateAvailable():Z
SharedConfig.setNewAppVersionAvailable(TL_help_appUpdate):Z
MessagesController.checkPromoInfoInternal(Z):V
MessagesController.isChatNoForwards(J):Z
MessagesController.isChatNoForwards(Chat):Z
MessagesController.isPeerNoForwards(J):Z
MessageObject.canForwardMessage():Z
MessagesController.getRestrictionReason(ArrayList):String
MessagesController.isSensitive(ArrayList):Z
MessagesController.showCantOpenAlert(r2,String):V
MessagesController.checkChannelError(String,J):V
MessagesController.showSensitiveContent():Z
MessageObject.isSensitive():Z
MessageObject.isHiddenSensitive():Z
MessagesController.setContentSettings(Z):V
MessagesStorage.markMessagesAsDeleted(J,I,Z,Z):ArrayList
MessagesStorage.markMessagesAsDeleted(J,ArrayList,Z,Z,I,I):ArrayList
MessagesController.deleteMessagesByPush(J,ArrayList,J):V
MessageObject.isSecretMedia():Z
MessageObject.isSecretMedia(Message):Z
MessageObject.isSecretPhotoOrVideo(Message):Z
MessageObject.shouldEncryptPhotoOrVideo(I,Message):Z
MessageObject.isVoiceOnce():Z
MessageObject.isRoundOnce():Z
FileLoadOperation.updateParams():V
ApplicationLoader.onCreate():V
MessagesController.storiesEnabled():Z
MessagesController.storyEntitiesAllowed():Z
MessagesController.storyEntitiesAllowed(User):Z
TranslateController.isTranslateDialogHidden(J):Z
ProfileActivity.isSwipeBackEnabled(MotionEvent):Z
MediaDataController.loadPinnedMessages(J,I,I):V
MessageObject.isVoice():Z
MessageObject.isMusic():Z
MessageObject.needDrawBluredPreview():Z
SendMessagesHelper.sendScreenshotMessage(User,I,Message):V
SecretChatHelper.sendScreenshotMessage(EncryptedChat,ArrayList,Message):V
MessagesController.isUserNoForwards(J):Z
MessagesController.isUserNoForwards(UserFull):Z
UserConfig.getMaxAccountCount():I
UserConfig.hasPremiumOnAccounts():Z
SharedConfig.getDevicePerformanceClass():I
MessagesController.getSponsoredMessages(J):SponsoredMessagesInfo
MessageObject.updateMessageText():V
MessagesController.premiumFeaturesBlocked():Z

## Descriptor migrations

12.10.5 uses obfuscated UI/support descriptors in several backend APIs.

- checkSensitive: BaseFragment -> r2
- checkCanOpenChat overloads: BaseFragment -> r2; Browser.Progress -> ee/f
- showCantOpenAlert: r2,String
- NotificationsController.removeDeletedMessagesFromNotifications: LongSparseArray -> z/f

## Semantic relocations

- ChatActivity behavior is in Lorg/telegram/ui/ko;. Evidence includes isSwipeBackEnabled, forwarding/send path s8, sponsored flow s6, and no-forward/can-forward callers.
- Rich HTML paste moved from ChatActivityEnterView.handleRichHtmlPaste() to Lorg/telegram/ui/Components/eu;.onTextContextMenuItem(I). The bytecode directly checks ClipboardManager text/html and converts HTML to a SpannableStringBuilder.
- DialogCell behavior is in Lorg/telegram/ui/Cells/u2;. The method t() has two getRestrictionReason() calls and c0() has one; each invoke is followed by move-result-object.
- ChatPullingDownDrawable behavior is strongly mapped to Lorg/telegram/ui/hq;: c(J,I,I,Z,[I):Dialog, b(Canvas,I,I):V, e():Z.
- SecretMediaViewer.closePhoto(boolean,boolean) maps to Lorg/telegram/ui/SecretMediaViewer;.e(ZZ):Z. Its Runnable onClose field is obfuscated as n0 and is executed/cleared in the method.

## Important patch-specific findings

Remove Ads: old ChatActivity sponsored methods are absent, but MessagesController.isSponsoredDisabled(), getSponsoredMessages(), MessageObject.isSponsored(), and VideoAds.load() survive. ko.s6() is the concrete UI sponsored-message call chain.

Auto-update: SharedConfig targets survive; LaunchActivity.checkAppUpdate and BlockingUpdateView.show do not survive under the old descriptors and need semantic relocation.

Anti-delete: both MessagesStorage.markMessagesAsDeleted overloads survive. Their bytecode branches on the boolean flag before dispatching asynchronous/internal deletion, so the Rushi strategy has a concrete behavioral target.

Anti-disappearing: old ChatActivity sendSecretMediaDelete/sendSecretMessageRead methods are gone. SecretMediaViewer.e(ZZ) remains and directly executes the obfuscated onClose Runnable; this needs a rebuilt fingerprint rather than a renamed old one.

Channel switching: source ChatPullingDownDrawable still exists, but the optimized DEX uses obfuscated hq. Its static c(J,I,I,Z,[I):Dialog is the current getNextUnreadDialog implementation.

Rich HTML paste: the current onTextContextMenuItem implementation is a confirmed semantic match, not an inferred name match.

## Certificate verification

12.10.5 META-INF/BNDLTOOL.RSA X.509 SHA-256:

49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1

This exactly matches the Rushi Telegram messenger certificate hash. No 12.10.5 certificate-hash update is required.

## Rule

No target is marked 100% working from a signature match alone. Final acceptance requires instruction-level register/control-flow validation, caller/callee review, patch-interaction review, and a successful Morphe patch/build verification.