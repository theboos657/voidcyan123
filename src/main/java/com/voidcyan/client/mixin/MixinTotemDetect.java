package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.module.TargetHudRenderer;
import com.voidcyan.client.module.TotemTraceManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class MixinTotemDetect {
   @Unique
   private static final Map<UUID, Long> lastPopTimes = new HashMap<>();

   @Inject(
      method = {"onEntityStatus"},
      at = {@At("HEAD")}
   )
   private void voidcyan$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
      if (packet.getStatus() == 35) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (!client.isOnThread()) {
            return;
         }
         if (client.world != null && client.player != null) {
            if (packet.getEntity(client.world) instanceof PlayerEntity player) {
               long now = System.currentTimeMillis();
               Long lastTime = lastPopTimes.get(player.getUuid());
               if (lastTime != null && now - lastTime < 500L) {
                  return;
               }
               lastPopTimes.put(player.getUuid(), now);

               TargetHudRenderer.recordTotemPop(player);
               com.voidcyan.client.module.NameTagItemsRenderer.recordTotemPop(player.getUuid());
               boolean isSelf = player.getUuid().equals(client.player.getUuid());
               if (isSelf) {
                  VoidCyanClient.sessionPops++;
                  VoidCyanClient.allTimePops++;
                  VoidCyanClient.saveConfig();
               }

               // Effects module: fire "totem" trigger effects on any totem pop
               com.voidcyan.client.CritEffectsManager.onTotemPop(player);

               if (VoidCyanClient.isTotemPopNotifierEnabled && VoidCyanClient.isNotificationsEnabled) {
                  int totalPops = TargetHudRenderer.getTotemPops(player);
                  String playerName = player.getName().getString();
                  VoidCyanClient.showNotification(playerName + " popped " + totalPops + " totem" + (totalPops > 1 ? "s" : ""));
               }

               if (VoidCyanClient.isTotemPopColorEnabled) {
                  if (isSelf && VoidCyanClient.totemPopColorDetectSelf) {
                     VoidCyanClient.flashTotemPopColor(player);
                  } else if (!isSelf && VoidCyanClient.totemPopColorDetectOthers) {
                     VoidCyanClient.flashTotemPopColor(player);
                  }
               }

               if (VoidCyanClient.isTotemTraceEnabled) {
                  if (!isSelf || VoidCyanClient.totemTraceDetectSelf) {
                     if (isSelf || VoidCyanClient.totemTraceDetectOthers) {
                        TotemTraceManager.addTrace(player);
                     }
                  }
               }
            }
         }
      }
   }
}
