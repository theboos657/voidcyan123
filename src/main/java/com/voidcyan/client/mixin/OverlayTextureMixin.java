package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OverlayTexture.class})
public class OverlayTextureMixin {
   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void voidcyan$onInit(CallbackInfo ci) {
      OverlayTexture self = (OverlayTexture)(Object)this;
      VoidCyanClient.overlayTextureInstance = self;
      VoidCyanClient.updateHitColorTexture();
   }
}
