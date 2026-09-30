package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.screen.FreelookSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerEntity.class})
public class MixinPlayerEntity {
   @Inject(
      method = {"shouldRenderName"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldRenderName(CallbackInfoReturnable<Boolean> cir) {
      PlayerEntity thisPlayer = (PlayerEntity)(Object)this;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.player != null) {
         if (VoidCyanClient.isFreelookEnabled
            && VoidCyanClient.freelookShowOwnNametag
            && (VoidCyanClient.freelookActive || client.currentScreen instanceof FreelookSettingsScreen)
            && thisPlayer == client.player) {
            cir.setReturnValue(true);
         }
      }
   }
}
