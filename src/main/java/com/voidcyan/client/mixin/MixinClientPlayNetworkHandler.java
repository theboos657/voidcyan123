package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.DeathInfoManager;
import com.voidcyan.client.module.LogoutSpotsManager;
import com.voidcyan.client.module.TargetHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class MixinClientPlayNetworkHandler {
   @Inject(
      method = {"onDeathMessage"},
      at = {@At("TAIL")}
   )
   private void voidcyan$captureDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         boolean isOwnDeath = packet.playerId() == mc.player.getId();
         if (isOwnDeath) {
            VoidCyanClient.sessionDeaths++;
            VoidCyanClient.allTimeDeaths++;
            String deathType = VoidCyanClient.getKillType(mc.player);
            if (deathType.equals("crystal")) {
               VoidCyanClient.sessionCrystalDeaths++;
               VoidCyanClient.allTimeCrystalDeaths++;
            }

            TargetHudRenderer.resetItemTracker();
            VoidCyanClient.saveConfig();
            if (VoidCyanClient.isDeathInfoEnabled) {
               DeathInfoManager.recordDeathLocation(mc);
            }

            DeathInfoManager.recordDeathMessage(packet, mc);
         } else {
            String deathMsg = packet.message().getString();
            if (deathMsg.contains(mc.player.getName().getString())) {
               VoidCyanClient.sessionKills++;
               VoidCyanClient.allTimeKills++;
               // Effects module: fire "kill" trigger effects
               com.voidcyan.client.CritEffectsManager.onKill(mc);
               long now = System.currentTimeMillis();
               if (!deathMsg.toLowerCase().contains("crystal")
                  && !deathMsg.toLowerCase().contains("blown up")
                  && (!(VoidCyanClient.lastAttackedEntity instanceof EndCrystalEntity) || now - VoidCyanClient.lastAttackTime >= 5000L)) {
                  if (deathMsg.toLowerCase().contains("respawn anchor")
                     || deathMsg.toLowerCase().contains("intentional game design")
                     || VoidCyanClient.lastInteractPos != null && now - VoidCyanClient.lastInteractTime < 5000L) {
                     VoidCyanClient.sessionAnchorKills++;
                     VoidCyanClient.allTimeAnchorKills++;
                  }
               } else {
                  VoidCyanClient.sessionCrystalKills++;
                  VoidCyanClient.allTimeCrystalKills++;
               }

               VoidCyanClient.saveConfig();
            }
         }
      }
   }

   @Inject(
      method = {"onItemPickupAnimation"},
      at = {@At("HEAD")}
   )
   private void voidcyan$onItemPickupAnimation(net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
      // Effects module: fire "xp" trigger effects when the local player
      // collects an XP orb (orbs ride the same pickup-animation packet).
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.world != null
         && packet.getCollectorEntityId() == mc.player.getId()
         && mc.world.getEntityById(packet.getEntityId()) instanceof net.minecraft.entity.ExperienceOrbEntity) {
         net.minecraft.entity.Entity orb = mc.world.getEntityById(packet.getEntityId());
         com.voidcyan.client.CritEffectsManager.fire("xp", mc.world, orb.getX(), orb.getY() + 0.2, orb.getZ());
      }
   }

   @Inject(
      method = {"onPlayerRemove"},
      at = {@At("TAIL")}
   )
   private void voidcyan$onPlayerRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
      if (VoidCyanClient.isLogoutSpotsEnabled) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null) {
            packet.profileIds().forEach(uuid -> {
               PlayerEntity player = mc.world.getPlayerByUuid(uuid);
               if (player != null) {
                  LogoutSpotsManager.recordLogout(player);
               }
            });
         }
      }
   }

   @Inject(
      method = {"onEntitySpawn"},
      at = {@At("TAIL")}
   )
   private void voidcyan$onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
      if (packet.getEntityType() == EntityType.ENDER_PEARL) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null) {
            TargetHudRenderer.onPearlThrown(packet.getX(), packet.getY(), packet.getZ());
         }
      }
   }
}
