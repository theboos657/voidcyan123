package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class FreelookMixin {
   @Shadow
   private double cursorDeltaX;
   @Shadow
   private double cursorDeltaY;

   @Inject(
      method = {"updateMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      if (VoidCyanClient.isFreelookEnabled) {
         if (VoidCyanClient.freelookActive) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
               double cursorDeltaX = this.cursorDeltaX;
               double cursorDeltaY = this.cursorDeltaY;
               double sensitivity = (Double)client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
               double scale = sensitivity * sensitivity * sensitivity * 8.0 * 0.15;
               VoidCyanClient.freelookYaw += (float)(cursorDeltaX * scale);
               VoidCyanClient.freelookPitch += (float)(cursorDeltaY * scale);
               VoidCyanClient.freelookPitch = Math.max(-90.0F, Math.min(90.0F, VoidCyanClient.freelookPitch));
               this.cursorDeltaX = 0.0;
               this.cursorDeltaY = 0.0;
               ci.cancel();
            }
         }
      }
   }
}
