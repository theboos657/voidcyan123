package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class HealthHeartsRenderer {
   private static final Identifier TEX_FULL = Identifier.of("voidcyan", "textures/gui/heart_full.png");
   private static final Identifier TEX_HALF = Identifier.of("voidcyan", "textures/gui/heart_half.png");
   private static final Identifier TEX_EMPTY = Identifier.of("voidcyan", "textures/gui/heart_empty.png");
   private static final Identifier TEX_GOLD_FULL = Identifier.of("voidcyan", "textures/gui/heart_gold_full.png");
   private static final Identifier TEX_GOLD_HALF = Identifier.of("voidcyan", "textures/gui/heart_gold_half.png");

   public static void renderAll3D(WorldRenderContext ctx) {
      if (VoidCyanClient.isHealthIndicatorsEnabled) {
         if (VoidCyanClient.healthIndicatorsMode == 1) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
               Camera camera = client.gameRenderer.getCamera();
               if (camera != null) {
                  Vec3d cameraPos = camera.getCameraPos();
                  Quaternionf cameraRotation = camera.getRotation();
                  MatrixStack matrices = ctx.matrices();
                  VertexConsumerProvider consumers = ctx.consumers();
                  if (matrices != null && consumers != null) {
                     double maxDistSq = VoidCyanClient.healthIndicatorsRange * VoidCyanClient.healthIndicatorsRange;

                     for (Entity entity : client.world.getEntities()) {
                        if (entity instanceof LivingEntity living
                           && !VoidCyanClient.isBotOrFloatingText(entity)
                           && (living != client.player || !client.options.getPerspective().isFirstPerson())) {
                           double dx = living.getX() - cameraPos.x;
                           double dy = living.getY() - cameraPos.y;
                           double dz = living.getZ() - cameraPos.z;
                           if (!(dx * dx + dy * dy + dz * dz > maxDistSq)) {
                              float health = living.getHealth();
                              float absorption = living.getAbsorptionAmount();
                              float maxH = living.getMaxHealth();
                              float entityHeight = living.getHeight();
                              float halfWidth = living.getWidth() / 2.0F;
                              if (VoidCyanClient.healthIndicatorsType == 0) {
                                 float barYWorld = entityHeight + 0.8F;
                                 double targetY = dy + barYWorld;
                                 double dist = Math.sqrt(dx * dx + targetY * targetY + dz * dz);
                                 float maxOffset = (float)Math.max(0.01, dist - 0.2);
                                 float offset = Math.min(halfWidth + 0.15F, maxOffset);
                                 double renderX = dx;
                                 double renderY = targetY;
                                 double renderZ = dz;
                                 float scale = 1.0F;
                                 if (dist > 0.001) {
                                    renderX = dx - dx / dist * offset;
                                    renderY = targetY - targetY / dist * offset;
                                    renderZ = dz - dz / dist * offset;
                                    scale = (float)((dist - offset) / dist);
                                 }

                                 matrices.push();
                                 matrices.translate(renderX, renderY, renderZ);
                                 matrices.multiply(cameraRotation);
                                 matrices.scale(scale, scale, scale);
                                 Entry entry = matrices.peek();
                                 drawHearts(entry, consumers, 0.0F, 0.0F, health, absorption, maxH);
                                 matrices.pop();
                              } else {
                                 double distXZ = Math.sqrt(dx * dx + dz * dz);
                                 float maxOffset = (float)Math.max(0.01, distXZ - 0.2);
                                 float offset = Math.min(halfWidth + 0.15F, maxOffset);
                                 double renderX = dx;
                                 double renderZ = dz;
                                 float scale = 1.0F;
                                 if (distXZ > 0.001) {
                                    renderX = dx - dx / distXZ * offset;
                                    renderZ = dz - dz / distXZ * offset;
                                    scale = (float)((distXZ - offset) / distXZ);
                                 }

                                 matrices.push();
                                 matrices.translate(renderX, dy, renderZ);
                                 double toCamX = cameraPos.x - living.getX();
                                 double toCamZ = cameraPos.z - living.getZ();
                                 float yaw = (float)Math.atan2(toCamZ, toCamX) - (float) (Math.PI / 2);
                                 matrices.multiply(RotationAxis.NEGATIVE_Y.rotation(yaw));
                                 matrices.scale(scale, scale, scale);
                                 Entry entry = matrices.peek();
                                 float offsetX = VoidCyanClient.healthIndicatorsType == 1 ? -(halfWidth + 0.6F) : halfWidth + 0.6F;
                                 float barY = entityHeight / 2.0F;
                                 drawHearts(entry, consumers, offsetX, barY, health, absorption, maxH);
                                 matrices.pop();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void drawHearts(Entry entry, VertexConsumerProvider consumers, float centerX, float y, float health, float abs, float maxH) {
      float heartSize = 0.15F;
      float spacing = 0.02F;
      int baseSlots = (int)Math.ceil(maxH / 2.0F);
      if (baseSlots == 0) {
         baseSlots = 1;
      }

      int absSlots = (int)Math.ceil(abs / 2.0F);
      boolean fuse = VoidCyanClient.healthIndicatorsFuseAbsorption;
      if (fuse) {
         int totalSlots = baseSlots + absSlots;
         drawHeartGrid(entry, consumers, centerX, y, health, abs, maxH, 0, totalSlots, heartSize, spacing, baseSlots);
      } else {
         drawHeartGrid(entry, consumers, centerX, y, health, 0.0F, maxH, 0, baseSlots, heartSize, spacing, baseSlots);
         if (absSlots > 0) {
            int healthRows = (baseSlots - 1) / 10 + 1;
            float absY = y + healthRows * (heartSize + spacing);
            drawHeartGrid(entry, consumers, centerX, absY, 0.0F, abs, 0.0F, baseSlots, baseSlots + absSlots, heartSize, spacing, baseSlots);
         }
      }
   }

   private static void drawHeartGrid(
      Entry entry,
      VertexConsumerProvider consumers,
      float centerX,
      float y,
      float health,
      float abs,
      float maxH,
      int startSlot,
      int endSlot,
      float heartSize,
      float spacing,
      int baseSlots
   ) {
      int totalSlotsToDraw = endSlot - startSlot;
      int maxRows = (totalSlotsToDraw - 1) / 10;

      for (int row = 0; row <= maxRows; row++) {
         int slotsInRow = row == maxRows ? (totalSlotsToDraw % 10 == 0 ? 10 : totalSlotsToDraw % 10) : 10;
         float totalWidth = slotsInRow * heartSize + (slotsInRow - 1) * spacing;
         float startX = centerX - totalWidth / 2.0F;
         float rowY = y + row * (heartSize + spacing);

         for (int i = 0; i < slotsInRow; i++) {
            int localIndex = row * 10 + i;
            int globalIndex = startSlot + localIndex;
            float slotX = startX + i * (heartSize + spacing);
            Identifier tex;
            if (globalIndex >= baseSlots) {
               int absSlotIndex = globalIndex - baseSlots;
               if (abs >= (absSlotIndex + 1) * 2) {
                  tex = TEX_GOLD_FULL;
               } else if (abs >= absSlotIndex * 2 + 1) {
                  tex = TEX_GOLD_HALF;
               } else {
                  tex = TEX_EMPTY;
               }
            } else if (health >= (globalIndex + 1) * 2) {
               tex = TEX_FULL;
            } else if (health >= globalIndex * 2 + 1) {
               tex = TEX_HALF;
            } else {
               tex = TEX_EMPTY;
            }

            VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(tex));
            drawQuad(entry, vc, slotX, rowY, slotX + heartSize, rowY + heartSize);
         }
      }
   }

   private static void drawQuad(Entry entry, VertexConsumer vc, float x1, float y1, float x2, float y2) {
      float z = 0.0F;
      vc.vertex(entry, x1, y1, z).color(1.0F, 1.0F, 1.0F, 1.0F).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y1, z).color(1.0F, 1.0F, 1.0F, 1.0F).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y2, z).color(1.0F, 1.0F, 1.0F, 1.0F).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x1, y2, z).color(1.0F, 1.0F, 1.0F, 1.0F).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
   }
}
