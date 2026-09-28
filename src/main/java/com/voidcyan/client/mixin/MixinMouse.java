package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.DeathInfoManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MixinMouse {
   @Shadow
   private MinecraftClient client;

   @Shadow
   public double getScaledX(Window window) {
      return 0.0;
   }

   @Shadow
   public double getScaledY(Window window) {
      return 0.0;
   }

   @Inject(
      method = {"onMouseButton"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void voidcyan$deathInfoClick(long window, MouseInput input, int action, CallbackInfo ci) {
      if (action == 1 && input.button() == 0) {
         if (this.client != null && !this.client.mouse.isCursorLocked()) {
            if (VoidCyanClient.isDeathInfoEnabled && VoidCyanClient.hasDeathInfo) {
               Window win = this.client.getWindow();
               double mx = this.getScaledX(win);
               double my = this.getScaledY(win);
               if (DeathInfoManager.handleClick(mx, my, input.button())) {
                  ci.cancel();
               }
            }
         }
      }
   }

   @Inject(
      method = {"onMouseScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (VoidCyanClient.isZoomEnabled && VoidCyanClient.zoomActive && vertical != 0.0) {
         double zoomIncrement = VoidCyanClient.zoomScrollSensitivity;
         if (vertical > 0.0) {
            VoidCyanClient.zoomLevel += zoomIncrement;
         } else if (vertical < 0.0) {
            VoidCyanClient.zoomLevel -= zoomIncrement;
         }

         if (VoidCyanClient.zoomLevel < 1.0) {
            VoidCyanClient.zoomLevel = 1.0;
         } else if (VoidCyanClient.zoomLevel > 100.0) {
            VoidCyanClient.zoomLevel = 100.0;
         }

         VoidCyanClient.saveConfig();
         ci.cancel();
      }
   }
}
