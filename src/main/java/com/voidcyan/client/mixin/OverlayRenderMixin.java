package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class OverlayRenderMixin {
   @Inject(
      method = {"renderPortalOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderPortalOverlay(DrawContext context, float f, CallbackInfo ci) {
      if (!VoidCyanClient.isPortalEnabled) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
      if (!VoidCyanClient.isPumpkinEnabled && texture.getPath().equals("textures/misc/pumpkinblur.png")) {
         ci.cancel();
      }
   }
}
