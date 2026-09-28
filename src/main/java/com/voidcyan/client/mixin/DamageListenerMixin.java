package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public class DamageListenerMixin {
   @Inject(
      method = {"setHealth"},
      at = {@At("HEAD")}
   )
   private void onSetHealth(float health, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (entity.getEntityWorld() != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null) {
            float currentHealth = entity.getHealth();
            if (health < currentHealth && VoidCyanClient.isDamageColorEnabled) {
               VoidCyanClient.flashDamageColor(entity);
            }

            if (entity instanceof PlayerEntity player && player == client.player && currentHealth <= 0.0F && health > 0.0F) {
               int id = entity.getId();
               if (!VoidCyanClient.damageCountedThisTick.contains(id)) {
                  VoidCyanClient.damageCountedThisTick.add(id);
                  VoidCyanClient.sessionPops++;
                  VoidCyanClient.allTimePops++;
                  VoidCyanClient.saveConfig();
               }
            }
         }
      }
   }
}
