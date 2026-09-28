package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class TotemTraceManager {
   private static final CopyOnWriteArrayList<GhostPlayerEntity> ghosts = new CopyOnWriteArrayList<>();
   private static final Map<GhostPlayerEntity, Long> ghostExpiryTimes = new ConcurrentHashMap<>();
   private static final Map<GhostPlayerEntity, Integer> ghostColors = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastTraceTimes = new ConcurrentHashMap<>();
   private static final long TRACE_DEDUPLICATION_MS = 750L;

   private TotemTraceManager() {
   }

   public static void addTrace(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld now = client.world;
      if (now instanceof ClientWorld) {
         long var9 = System.currentTimeMillis();
         UUID playerId = player.getUuid();
         Long lastTraceTime = lastTraceTimes.get(playerId);
         if (lastTraceTime == null || var9 - lastTraceTime >= 750L) {
            lastTraceTimes.put(playerId, var9);
            lastTraceTimes.entrySet().removeIf(entry -> var9 - entry.getValue() > 5000L);
            GhostPlayerEntity ghost = new GhostPlayerEntity(now, player.getGameProfile());
            ghost.setGhostPosition(player.getX(), player.getY(), player.getZ());
            ghost.setGhostRotation(player.getYaw(), player.getPitch(), player.getHeadYaw(), player.getBodyYaw());
            if (player instanceof AbstractClientPlayerEntity acpe) {
               ghost.setCapturedSkin(acpe.getSkin());
            }

            if (VoidCyanClient.totemTraceShowArmor) {
               ghost.copyInventory(player.getInventory());
            }

            ghosts.add(ghost);
            ghostExpiryTimes.put(ghost, var9 + VoidCyanClient.totemTraceDuration * 1000L);
            ghostColors.put(ghost, VoidCyanClient.getTotemTraceColor());
         }
      }
   }

   public static void renderAll3D(WorldRenderContext context) {
      if (!ghosts.isEmpty() && VoidCyanClient.isTotemTraceEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null && client.player != null) {
            EntityRenderManager erm = client.getEntityRenderDispatcher();
            if (erm != null) {
               Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
               MatrixStack matrices = context.matrices();
               if (matrices != null) {
                  CameraRenderState cameraState = context.worldState().cameraRenderState;
                  float tickDelta = client.getRenderTickCounter().getDynamicDeltaTicks();
                  long now = System.currentTimeMillis();
                  ghosts.removeIf(ghostx -> {
                     if (now > ghostExpiryTimes.getOrDefault(ghostx, 0L)) {
                        ghostx.discard();
                        ghostExpiryTimes.remove(ghostx);
                        ghostColors.remove(ghostx);
                        return true;
                     } else {
                        return false;
                     }
                  });

                  for (GhostPlayerEntity ghost : ghosts) {
                     renderGhost(context, erm, ghost, cameraPos, cameraState, matrices, tickDelta);
                  }
               }
            }
         }
      }
   }

   private static void renderGhost(
      WorldRenderContext context,
      EntityRenderManager erm,
      GhostPlayerEntity ghost,
      Vec3d cameraPos,
      CameraRenderState cameraState,
      MatrixStack matrices,
      float tickDelta
   ) {
      double offsetX = ghost.getX() - cameraPos.x;
      double offsetY = ghost.getY() - cameraPos.y;
      double offsetZ = ghost.getZ() - cameraPos.z;
      EntityRenderState state = erm.getAndUpdateRenderState(ghost, tickDelta);
      if (VoidCyanClient.totemTraceShowColor) {
         state.outlineColor = ghostColors.getOrDefault(ghost, VoidCyanClient.getTotemTraceColor());
      } else {
         state.outlineColor = 0;
      }

      erm.render(state, cameraState, offsetX, offsetY, offsetZ, matrices, context.commandQueue());
   }

   public static void clear() {
      for (GhostPlayerEntity ghost : ghosts) {
         ghost.discard();
      }

      ghosts.clear();
      ghostExpiryTimes.clear();
      ghostColors.clear();
      lastTraceTimes.clear();
   }
}
