package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {GameRenderer.class},
   priority = 1100
)
public class MixinGameRenderer {
   @Inject(
      method = {"getFov"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void getZoomFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
      float fov = cir.getReturnValueF();
      if (VoidCyanClient.isCustomFovEnabled) {
         fov = VoidCyanClient.customFov;
         cir.setReturnValue(fov);
      }

      if (VoidCyanClient.isZoomEnabled) {
         double target = VoidCyanClient.zoomActive ? VoidCyanClient.zoomLevel : 1.0;
         if (VoidCyanClient.zoomSmoothAnimation) {
            double speed = Math.max(0.01, Math.min(1.0, VoidCyanClient.zoomAnimationSpeed));
            if (VoidCyanClient.currentZoomMultiplier != target) {
               VoidCyanClient.currentZoomMultiplier += (target - VoidCyanClient.currentZoomMultiplier) * speed;
               if (Math.abs(VoidCyanClient.currentZoomMultiplier - target) < 0.005) {
                  VoidCyanClient.currentZoomMultiplier = target;
               }
            }
         } else {
            VoidCyanClient.currentZoomMultiplier = target;
         }

         if (VoidCyanClient.currentZoomMultiplier > 1.001) {
            cir.setReturnValue((float)(fov / VoidCyanClient.currentZoomMultiplier));
         }
      } else {
         VoidCyanClient.currentZoomMultiplier = 1.0;
      }
   }

   @Inject(
      method = {"tiltViewWhenHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (!VoidCyanClient.isHurtcamEnabled) {
         ci.cancel();
      }
   }
}
