package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.DeathInfoManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DeathScreen.class})
public class MixinDeathScreen {
   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void voidcyan$captureDeath(Text message, boolean isHardcore, ClientPlayerEntity decedent, CallbackInfo ci) {
      if (VoidCyanClient.isDeathInfoEnabled) {
         DeathInfoManager.recordDeathFromDeathScreen(message, decedent);
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void voidcyan$renderDeathInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (VoidCyanClient.isDeathInfoEnabled && VoidCyanClient.hasDeathInfo) {
         DeathScreen screen = (DeathScreen)(Object)this;
         DeathInfoManager.renderOnDeathScreen(context, screen.width, screen.height);
      }
   }
}
