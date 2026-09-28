package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WaterFogModifier.class})
public class WaterFogMixin {
   @Inject(
      method = {"applyStartEndModifier"},
      at = {@At("RETURN")}
   )
   private void onApplyStartEndModifier(FogData fogData, Camera camera, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci) {
      if (!VoidCyanClient.isWaterFogEnabled) {
         fogData.environmentalStart = viewDistance * 0.9F;
         fogData.environmentalEnd = viewDistance;
         fogData.renderDistanceStart = viewDistance * 0.9F;
         fogData.renderDistanceEnd = viewDistance;
      }
   }
}
