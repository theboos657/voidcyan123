package com.voidcyan.client.mixin;

import com.voidcyan.client.FeatureModules;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fog changer: remove fog or set a custom fog distance. Float params are env start/end, render start/end, sky end, cloud end. */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
   private static final String TARGET = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V";

   private static float voidcyan$adjust(int idx, float value) {
      if (!FeatureModules.on[FeatureModules.FOG] || FeatureModules.fogMode == 0) return value;
      if (FeatureModules.fogMode == 1) return 1.0E7F;
      float end = FeatureModules.fogDistance;
      return switch (idx) {
         case 0, 2 -> end * 0.5F;
         case 1, 3 -> end;
         default -> value;
      };
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float voidcyan$f0(float v) {
      return voidcyan$adjust(0, v);
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 1)
   private float voidcyan$f1(float v) {
      return voidcyan$adjust(1, v);
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 2)
   private float voidcyan$f2(float v) {
      return voidcyan$adjust(2, v);
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 3)
   private float voidcyan$f3(float v) {
      return voidcyan$adjust(3, v);
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 4)
   private float voidcyan$f4(float v) {
      return voidcyan$adjust(4, v);
   }

   @ModifyVariable(method = TARGET, at = @At("HEAD"), argsOnly = true, ordinal = 5)
   private float voidcyan$f5(float v) {
      return voidcyan$adjust(5, v);
   }
}
