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
         long nowNs = System.nanoTime();
         double dt = voidcyan$lastZoomNs == 0L ? 0.016 : Math.min(0.1, (nowNs - voidcyan$lastZoomNs) / 1.0E9);
         voidcyan$lastZoomNs = nowNs;
         double target = com.voidcyan.client.FeatureModules.zoomTarget(Math.max(2.0, VoidCyanClient.zoomLevel), VoidCyanClient.zoomActive, dt);
         double cur = Math.max(1.0, VoidCyanClient.currentZoomMultiplier);
         if (VoidCyanClient.zoomSmoothAnimation) {
            double speed = Math.max(0.01, Math.min(1.0, VoidCyanClient.zoomAnimationSpeed));
            // Interpolate in log space so 1x -> 1000x feels even.
            double next = Math.exp(Math.log(cur) + (Math.log(target) - Math.log(cur)) * speed);
            VoidCyanClient.currentZoomMultiplier = Math.abs(Math.log(target) - Math.log(next)) < 0.002 ? target : next;
         } else {
            VoidCyanClient.currentZoomMultiplier = target;
         }

         if (VoidCyanClient.currentZoomMultiplier > 1.001) {
            cir.setReturnValue(Math.max(0.05F, (float)(fov / VoidCyanClient.currentZoomMultiplier)));
         }
      } else {
         VoidCyanClient.currentZoomMultiplier = 1.0;
      }
   }

   private static long voidcyan$lastZoomNs = 0L;

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
