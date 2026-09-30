package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.ChatBubbleRenderer;
import com.voidcyan.client.util.NameReplacer;
import java.util.List;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatHud.class})
public class ChatHudMixin {
   @Shadow
   private List<ChatHudLine> messages;
   @Shadow
   private List<ChatHudLine.Visible> visibleMessages;
   private static String lastMessage = "";
   private static int repeatCount = 1;

   @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
   private void onAddMessage(Text message, CallbackInfo ci) {
      if (!VoidCyanClient.isChatModuleEnabled) return;
      String plain = message.getString();
      String sender = VoidCyanClient.extractChatSender(plain);
      if (sender != null) {
         if (VoidCyanClient.isChatBubblesEnabled) {
            ChatBubbleRenderer.addMessage(sender, VoidCyanClient.chatBody(plain, sender));
         }

         if (VoidCyanClient.isChatRangeFilterEnabled && !VoidCyanClient.isSenderInRange(sender)) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
   private void keepChatOnRejoin(boolean clearHistory, CallbackInfo ci) {
      if (VoidCyanClient.isChatPersistEnabled) ci.cancel();
   }

   @ModifyConstant(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", constant = @Constant(intValue = 100))
   private int modifyMaxMessages(int original) {
      return VoidCyanClient.isChatModuleEnabled && VoidCyanClient.isChatUnlimitedHistoryEnabled ? 100000 : original;
   }

   @ModifyVariable(
      method = {"addMessage"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text modifyChatMessage(Text message) {
      if (VoidCyanClient.isChatModuleEnabled) {
         String plain = message.getString();
         String blocked = VoidCyanClient.filterHarmfulPlain(plain);
         if (!blocked.equals(plain)) {
            return Text.literal(blocked);
         }

         if (VoidCyanClient.isChatRepeatCompactEnabled) {
            if (plain.equals(lastMessage)) {
               repeatCount++;
               if (!this.messages.isEmpty()) this.messages.removeFirst();
               if (!this.visibleMessages.isEmpty()) {
                  this.visibleMessages.removeFirst();
                  while (!this.visibleMessages.isEmpty() && !this.visibleMessages.get(0).endOfEntry()) this.visibleMessages.removeFirst();
               }

               message = message.copy().append(Text.literal(" " + repeatCount + "x").withColor(VoidCyanClient.chatRepeatCountColor));
            } else {
               lastMessage = plain;
               repeatCount = 1;
            }
         }
      }

      if (VoidCyanClient.isPeerNickEnabled && !VoidCyanClient.peerNicknames.isEmpty()) {
         for (String pair : VoidCyanClient.peerNicknames.split(",")) {
            int i = pair.indexOf('=');
            if (i > 0 && i < pair.length() - 1) message = NameReplacer.replaceName(message, pair.substring(0, i).trim(), pair.substring(i + 1).trim());
         }
      }

      return NameReplacer.replaceInText(message);
   }
}
