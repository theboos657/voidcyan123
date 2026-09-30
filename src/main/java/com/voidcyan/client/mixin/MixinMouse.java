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
         // Multiplicative steps: a notch changes zoom by the same percentage at 2x and at 900x.
         double factor = 1.0 + 0.12 * VoidCyanClient.zoomScrollSensitivity;
         double level = Math.max(1.0, com.voidcyan.client.FeatureModules.zoomRuntime);
         level = vertical > 0.0 ? level * factor : level / factor;
         level = Math.max(1.0, Math.min(1000.0, level));
         com.voidcyan.client.FeatureModules.zoomRuntime = level;
         com.voidcyan.client.FeatureModules.zoomScrolled = true;
         VoidCyanClient.saveConfig();
         ci.cancel();
      }
   }

   @org.spongepowered.asm.mixin.Shadow
   private double cursorDeltaX;
   @org.spongepowered.asm.mixin.Shadow
   private double cursorDeltaY;

   /** Dynamic zoom sensitivity: look speed shrinks with the zoom so the view moves the same on screen at any zoom. */
   @Inject(method = {"updateMouse"}, at = {@At("HEAD")})
   private void voidcyan$zoomSensitivity(double timeDelta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
      if (VoidCyanClient.isZoomEnabled && VoidCyanClient.currentZoomMultiplier > 1.01) {
         double f = 1.0 / VoidCyanClient.currentZoomMultiplier;
         this.cursorDeltaX *= f;
         this.cursorDeltaY *= f;
      }
   }
}
