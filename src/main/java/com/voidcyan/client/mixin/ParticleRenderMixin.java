package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ParticleManager.class})
public class ParticleRenderMixin {
   @Inject(
      method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAddParticleEffect(
      ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir
   ) {
      if (!VoidCyanClient.isParticlesEnabled) {
         cir.setReturnValue(null);
      } else {
         ParticleType<?> type = parameters.getType();
         if (VoidCyanClient.isExplosionParticlesEnabled || type != ParticleTypes.EXPLOSION && type != ParticleTypes.EXPLOSION_EMITTER) {
            if (VoidCyanClient.isPotionParticlesEnabled || type != ParticleTypes.ENTITY_EFFECT && type != ParticleTypes.INSTANT_EFFECT) {
               if (VoidCyanClient.isCriticalParticlesEnabled || type != ParticleTypes.CRIT && type != ParticleTypes.ENCHANTED_HIT) {
                  if (!VoidCyanClient.isDamageParticlesEnabled && type == ParticleTypes.DAMAGE_INDICATOR) {
                     cir.setReturnValue(null);
                  }
               } else {
                  cir.setReturnValue(null);
               }
            } else {
               cir.setReturnValue(null);
            }
         } else {
            cir.setReturnValue(null);
         }
      }
   }
}
