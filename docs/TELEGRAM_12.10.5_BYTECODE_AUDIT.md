# Telegram 12.10.5 Bytecode Audit — Initial Checkpoint

## Target artifact

- Package: `org.telegram.messenger`
- Version: 12.10.5
- VersionCode: 71052
- APK size: 111,414,316 bytes
- SHA-256: `fd741e054251ab7499a1b4ac55df6be8880aa054fba5c62a196557ab4050db5b`
- DEX files: `classes.dex`, `classes2.dex`, `classes3.dex`, `classes4.dex`

### DEX SHA-256

| DEX | Size | SHA-256 |
|---|---:|---|
| classes.dex | 11,158,876 | `ff6cdc0f0c9659245ac2db55d3cb349096de726fff920f1f7e813b51817ccf67` |
| classes2.dex | 505,148 | `1bc643a435b2222ae38dcb2bf2b85e8aabd529f9b28d1cf8e1244405619bdb91` |
| classes3.dex | 12,524,416 | `a290313c2bcb17b981849342ab86e481ff247e6d250ac66a27249ef7b570eacc` |
| classes4.dex | 2,910,296 | `0f9049f5ad65d08099caa5ed2ac9a166b5c79bf10546a30d42fe0db2a5ac73bc` |

## Important result: 12.10.5 is not a blind fingerprint-renaming target

The current Rushi Telegram fingerprints are documented as targeting Telegram 12.10.1 / versionCode 70382. Several backend methods still exist with compatible signatures, but multiple UI targets have been renamed, relocated, or removed.

The strongest confirmed examples:

### ChatActivity

12.10.5 does **not** support the earlier candidate mapping:

`ChatActivity -> Lorg/telegram/ui/po;`

The class:

`Lorg/telegram/ui/ko;`

inherits from:

`Lorg/telegram/ui/ActionBar/r2;`

and therefore matches the obfuscated runtime shape of the main fragment.

Verified method:

`Lorg/telegram/ui/ko;->isSwipeBackEnabled(Landroid/view/MotionEvent;)Z`

The old 12.10.3 candidate mapping to `ui.po` must not be carried forward.

The forwarding method was also found semantically by signature:

`Lorg/telegram/ui/ko;->s8(Ljava/util/ArrayList;ZZZIJ)V`

Its bytecode calls:

`SendMessagesHelper.sendMessage(ArrayList, J, Z, Z, Z, I, I, MessageObject, I, J, J, MessageSuggestionParams)`

This is strong evidence that `ko.s8` is the forwarding/send path corresponding to the old `forwardMessages` fingerprint. It must still be validated against the complete method control flow before patching.

### Rich HTML paste

The old Rushi fingerprint:

`ChatActivityEnterView.handleRichHtmlPaste():Z`

does not exist in the 12.10.5 source search.

The current Telegram 12.10.5 source implements the rich-HTML paste behavior inside:

`EditTextCaption.onTextContextMenuItem(int)`

The APK contains:

`Lorg/telegram/ui/Components/eu;->onTextContextMenuItem(I)Z`

and `eu` inherits from `EditTextBoldCursor`.

The source-level behavior was verified: the method checks `android.R.id.paste`, requires HTML clipboard MIME type, converts HTML with `CopyUtilities.fromHTML`, applies emoji/quote handling, replaces the selection, and returns true.

Therefore the previous candidate mapping to `Components.iu.onTextContextMenuItem(I)` is rejected for 12.10.5. The likely semantic target is `Components.eu.onTextContextMenuItem(I)`, pending instruction-level fingerprint construction.

### DialogCell

`Lorg/telegram/ui/Cells/DialogCell;` is not present in the optimized APK descriptor set.

The semantic DialogCell target was located by its call graph:

`Lorg/telegram/ui/Cells/u2;`

Relevant methods:

- `t():V` — calls `MessagesController.getRestrictionReason(ArrayList)` twice.
- `c0():V` — calls `MessagesController.getRestrictionReason(ArrayList)` once.
- `L(I, CharSequence, String, Z):SpannableStringBuilder` — the obfuscated method matching the source-level `getMessageStringFormatted` role.

The source-level DialogCell implementation still exists in Telegram 12.10.5. The obfuscated APK therefore requires semantic fingerprints rather than the old class/name fingerprint.

### MessagesController

The following important backend methods still exist in 12.10.5:

- `isPremiumUser(User):Z`
- `isSponsoredDisabled():Z`
- `getSponsoredMessages(J):SponsoredMessagesInfo`
- `checkPromoInfoInternal(Z):V`
- `isChatNoForwards(J):Z`
- `isChatNoForwards(Chat):Z`
- `isPeerNoForwards(J):Z`
- `isUserNoForwards(J):Z`
- `isUserNoForwards(UserFull):Z`
- `getRestrictionReason(ArrayList):String`
- `isSensitive(ArrayList):Z`
- `showSensitiveContent():Z`
- `setContentSettings(Z):V`
- `checkChannelError(String,J):V`
- `checkSensitive(r2,J,Runnable,Runnable):V`
- `showCantOpenAlert(r2,String):V`
- `checkCanOpenChat(Bundle,r2):Z`
- `checkCanOpenChat(Bundle,r2,MessageObject):Z`
- `checkCanOpenChat(Bundle,r2,MessageObject,ee/f):Z`
- `deleteMessagesByPush(J,ArrayList,J):V`
- `storiesEnabled():Z`
- `storyEntitiesAllowed():Z`
- `storyEntitiesAllowed(User):Z`
- `premiumFeaturesBlocked():Z`
- `sendTyping(J,J,I,I):Z`

Several descriptors now use obfuscated UI/support classes, notably `r2` in place of the source-level `BaseFragment` and `ee/f` in the fourth `checkCanOpenChat` overload. Those descriptor changes must be reflected in fingerprints.

### MessageObject

Important methods remain present:

- `canForwardMessage():Z`
- `isSponsored():Z`
- `isSecretMedia():Z`
- `isSecretMedia(TLRPC$Message):Z`
- `isSecretPhotoOrVideo(TLRPC$Message):Z`
- `shouldEncryptPhotoOrVideo():Z`
- `shouldEncryptPhotoOrVideo(I,TLRPC$Message):Z`
- `isVoiceOnce():Z`
- `isRoundOnce():Z`
- `isVoice():Z`
- `isMusic():Z`
- `needDrawBluredPreview():Z`
- `isSensitive():Z`
- `isHiddenSensitive():Z`
- `updateMessageText():V`

The overloaded `updateMessageText(AbstractMap,AbstractMap,z/f,z/f):V` also exists. Its descriptor is no longer the empty-parameter overload used by the Rushi fingerprint.

### FileLoadOperation

`FileLoadOperation.updateParams():V` exists and is called from several internal paths, including constructors and `start()`. This target remains structurally relevant for the download-boost patch.

### SharedConfig

All three Rushi auto-update backend targets remain:

- `isAppUpdateAvailable():Z`
- `setNewAppVersionAvailable(TL_help_appUpdate):Z`
- `getDevicePerformanceClass():I`

The UI-side auto-update targets such as the old `LaunchActivity.checkAppUpdate` fingerprint were not found by exact method name in the optimized APK and need semantic re-location.

## Call-graph evidence

Selected target consumers in 12.10.5:

- `MessagesController.getRestrictionReason(ArrayList)`: 16 detected callers across the DEX set.
  - DialogCell-equivalent `Cells/u2.t`: 2 calls.
  - DialogCell-equivalent `Cells/u2.c0`: 1 call.
  - `MessageObject.updateMessageText`: 1 call.
  - `MessagesController.checkCanOpenChat`: 2 calls.
  - `MessagesController.openChatOrProfileWith`: 2 calls.
  - additional UI/PhotoViewer/LaunchActivity paths.

- `MessagesController.isPeerNoForwards(J)`: 13 detected callers, including `ui/ko`, message cells, PhotoViewer, and other UI paths.

- `MessagesController.isPremiumUser(User)`: 13 detected callers, including `ui/ko`, DialogCell-equivalent `Cells/u2`, ProfileActivity and other UI classes.

- `MessagesController.getSponsoredMessages(J)`: detected callers include `ui/ko` methods `Da` and `s6`.

- `MessagesController.isSponsoredDisabled()`: `ui/ko.s6` directly checks it before calling `getSponsoredMessages(J)`.

- `MessageObject.isVoice()`: 50+ callers, including multiple media/playback paths.

- `MessageObject.isMusic()`: 50+ callers, including MediaController playback and playlist paths.

- `MessageObject.shouldEncryptPhotoOrVideo()`: multiple media-loading/playback callers.

These counts demonstrate why a global method replacement must be evaluated against downstream callers and not only the fingerprint method body.

## Sponsored-message refactor evidence

The old Rushi targets:

- `ChatActivity.addSponsoredMessages(Z)`
- `ChatActivity.getSponsoredMessagesCount():I`

were not found by exact method name.

Instead, `ui/ko.s6()` contains the relevant sponsored-message flow:

1. checks channel/bot state,
2. checks current account Premium status,
3. obtains MessagesController,
4. checks `isSponsoredDisabled()`,
5. obtains `getSponsoredMessages(dialogId)`,
6. processes the returned message list,
7. updates message layout,
8. eventually invokes internal UI methods.

This is a concrete example of Telegram moving the behavior path rather than simply changing an obfuscated class name.

## Telegram 12.10.5 source changes

Official Telegram commit:

`dc780e81ed1261c369c27870e8e0999a1eb0b600`

The 12.10.5 source update is substantial: 10,364 additions and 725 deletions.

The largest changes are concentrated around the new round-video camera subsystem:

- `RoundVideoCameraController.java`
- `RoundVideoSession.java`
- `RoundVideoOverlayRenderer.java`
- `InstantCameraView2.java`
- `RoundVideoGlProcessor.java`
- `RoundVideoCodecRecorder.java`
- `RoundVideoMp4Writer.java`
- `RoundVideoSettingsActivity.java`
- `TelegramRoundVideoUpload.java`
- `InstantCameraViewBase.java`
- `RoundVideoRemuxer.java`
- `RoundVideoSwitchTimingStore.java`

`ChatActivity.java` was also modified to use the new `InstantCameraViewBase` architecture and recording UI frame callbacks.

`ChatActivityEnterView.java` gained an external round-video frame clock and changed its recording-dot animation to support externally supplied frame timing.

This confirms that Telegram 12.10.5 contains genuine architectural changes around recording/camera behavior, not merely a version/obfuscation change.

## Current status

No 12.10.5 fingerprint is considered final solely because a method name/signature exists.

Next phase:

1. Build a complete Rushi-fingerprint inventory.
2. Resolve each target against the actual 12.10.5 class/method graph.
3. For obfuscated UI targets, fingerprint semantic bytecode patterns.
4. Verify invoke/result/register/control-flow relationships.
5. Identify patch collisions and shared call paths.
6. Rebuild only the fingerprints whose behavioral target still exists.
7. Mark removed/restructured targets explicitly instead of forcing matches.
8. Run the Morphe build/dry-run against the actual 12.10.5 APK before calling a patch functional.

**This document is an analysis checkpoint, not a claim that the 12.10.5 patch set is complete or runtime verified.**
