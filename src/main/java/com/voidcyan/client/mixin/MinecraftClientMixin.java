package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.NameProtect;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      MinecraftClient client = (MinecraftClient)(Object)this;
      NameProtect.INSTANCE.setEnabled(VoidCyanClient.isNickHiderEnabled);
      if (VoidCyanClient.isNickHiderEnabled && client.world != null && client.player != null) {
         VoidCyanClient.updateNameProtectMappings(client);
      }
   }
}
