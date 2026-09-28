package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LightmapTextureManager.class})
public class MixinLightmapTextureManager {
   @Inject(
      method = {"getBrightness"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetBrightness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
      if (VoidCyanClient.isFullbrightEnabled) {
         cir.setReturnValue(1.0F);
      }
   }
}
