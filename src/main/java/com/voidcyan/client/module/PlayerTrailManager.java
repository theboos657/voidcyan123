package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class PlayerTrailManager {
   private static final Map<UUID, PlayerTrailManager.TrailData> trails = new HashMap<>();

   private PlayerTrailManager() {
   }

   public static void updateTrails() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         long now = System.currentTimeMillis();
         float trailDuration = VoidCyanClient.playerTrailDuration * 1000.0F;
         if (VoidCyanClient.playerTrailShowSelf) {
            if (!trails.containsKey(client.player.getUuid())) {
               trails.put(client.player.getUuid(), new PlayerTrailManager.TrailData(client.player.getUuid()));
            }
         } else {
            trails.remove(client.player.getUuid());
         }

         if (VoidCyanClient.playerTrailShowOthers) {
            client.world.getPlayers().forEach(player -> {
               if (player != client.player && !trails.containsKey(player.getUuid())) {
                  trails.put(player.getUuid(), new PlayerTrailManager.TrailData(player.getUuid()));
               }
            });
         }

         trails.values()
            .forEach(
               trail -> {
                  PlayerEntity player = client.world.getPlayerByUuid(trail.playerUUID);
                  if (player != null) {
                     boolean shouldAddPoint = false;
                     if (trail.positions.isEmpty()) {
                        shouldAddPoint = true;
                     } else {
                        PlayerTrailManager.TrailPoint lastPoint = trail.positions.get(trail.positions.size() - 1);
                        long timeSinceLast = now - lastPoint.timestamp;
                        shouldAddPoint = timeSinceLast >= 100L;
                        double distFromLast = Math.sqrt(
                           Math.pow(player.getX() - lastPoint.x, 2.0) + Math.pow(player.getY() - lastPoint.y, 2.0) + Math.pow(player.getZ() - lastPoint.z, 2.0)
                        );
                        if (distFromLast > 0.1) {
                           shouldAddPoint = true;
                        }
                     }

                     if (shouldAddPoint) {
                        trail.positions.add(new PlayerTrailManager.TrailPoint(player.getX(), player.getY() + getTrailAnchorHeight(player), player.getZ(), now));
                     }
                  }

                  trail.positions.removeIf(p -> (float)(now - p.timestamp) > trailDuration);
               }
            );
         trails.entrySet().removeIf(entry -> entry.getValue().positions.isEmpty());
      }
   }

   public static void renderTrails(WorldRenderContext context) {
      if (!trails.isEmpty()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null) {
            MatrixStack matrices = context.matrices();
            VertexConsumerProvider consumers = context.consumers();
            if (consumers != null) {
               Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
               long now = System.currentTimeMillis();
               float trailDuration = VoidCyanClient.playerTrailDuration * 1000.0F;
               VertexConsumer lineConsumer = consumers.getBuffer(RenderLayers.lines());

               for (PlayerTrailManager.TrailData trail : trails.values()) {
                  trail.positions.removeIf(p -> (float)(now - p.timestamp) > trailDuration);
                  if (trail.positions.size() >= 2) {
                     int color = VoidCyanClient.playerTrailColor;
                     int r = color >> 16 & 0xFF;
                     int g = color >> 8 & 0xFF;
                     int b = color & 0xFF;

                     for (int i = 1; i < trail.positions.size(); i++) {
                        PlayerTrailManager.TrailPoint previous = trail.positions.get(i - 1);
                        PlayerTrailManager.TrailPoint point = trail.positions.get(i);
                        float age = (float)(now - point.timestamp) / 1000.0F;
                        float alpha = Math.max(0.0F, 1.0F - age / VoidCyanClient.playerTrailDuration);
                        int alphaInt = Math.round(alpha * 255.0F);
                        float x1 = (float)(previous.x - cameraPos.x);
                        float y1 = (float)(previous.y - cameraPos.y);
                        float z1 = (float)(previous.z - cameraPos.z);
                        float x2 = (float)(point.x - cameraPos.x);
                        float y2 = (float)(point.y - cameraPos.y);
                        float z2 = (float)(point.z - cameraPos.z);
                        float normalX = x2 - x1;
                        float normalY = y2 - y1;
                        float normalZ = z2 - z1;
                        float length = (float)Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
                        if (!(length < 1.0E-4F)) {
                           normalX /= length;
                           normalY /= length;
                           normalZ /= length;
                           Matrix4f matrix = matrices.peek().getPositionMatrix();
                           addLineVertex(lineConsumer, matrix, x1, y1, z1, r, g, b, alphaInt, normalX, normalY, normalZ);
                           addLineVertex(lineConsumer, matrix, x2, y2, z2, r, g, b, alphaInt, normalX, normalY, normalZ);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static double getTrailAnchorHeight(PlayerEntity player) {
      return switch (VoidCyanClient.playerTrailAnchor) {
         case 0 -> player.getHeight() * 0.88;
         case 2 -> 0.12;
         default -> player.getHeight() * 0.5;
      };
   }

   private static void addLineVertex(
      VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int alpha, float normalX, float normalY, float normalZ
   ) {
      consumer.vertex(matrix, x, y, z).color(r, g, b, alpha).normal(normalX, normalY, normalZ).lineWidth(6.0F);
   }

   private static class TrailData {
      final UUID playerUUID;
      final List<PlayerTrailManager.TrailPoint> positions;

      TrailData(UUID playerUUID) {
         this.playerUUID = playerUUID;
         this.positions = new ArrayList<>();
      }
   }

   private static class TrailPoint {
      final double x;
      final double y;
      final double z;
      final long timestamp;

      TrailPoint(double x, double y, double z, long timestamp) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.timestamp = timestamp;
      }
   }
}
