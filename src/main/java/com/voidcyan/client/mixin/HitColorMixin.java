package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.EntityRenderStateAccessor;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntityRenderer.class})
public class HitColorMixin {
   @Inject(
      method = {"getOverlay"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private static void voidcyan$getOverlay(LivingEntityRenderState state, float whiteOverlayProgress, CallbackInfoReturnable<Integer> cir) {
      if (state instanceof EntityRenderStateAccessor accessor) {
         int id = accessor.voidcyan$getEntityId();
         if (id != -1) {
            boolean appliedCustom = false;
            if (VoidCyanClient.shouldApplyTotemPopColor(id)) {
               VoidCyanClient.writeTotemPopColorToTexture();
               int u = OverlayTexture.getU(whiteOverlayProgress);
               cir.setReturnValue(OverlayTexture.packUv(u, 3));
               appliedCustom = true;
            } else if (VoidCyanClient.shouldApplyDamageColor(id)) {
               VoidCyanClient.writeDamageColorToTexture();
               int u = OverlayTexture.getU(whiteOverlayProgress);
               cir.setReturnValue(OverlayTexture.packUv(u, 3));
               appliedCustom = true;
            } else if (VoidCyanClient.shouldApplyHitColor(id)) {
               VoidCyanClient.writeHitColorToTexture();
               int u = OverlayTexture.getU(whiteOverlayProgress);
               cir.setReturnValue(OverlayTexture.packUv(u, 3));
               appliedCustom = true;
            }

            if (!appliedCustom) {
               int currentUv = (Integer)cir.getReturnValue();
               int v = currentUv & 65535;
               if (v == 3) {
                  VoidCyanClient.writeVanillaRedToTexture();
               }
            }
         }
      }
   }
}
