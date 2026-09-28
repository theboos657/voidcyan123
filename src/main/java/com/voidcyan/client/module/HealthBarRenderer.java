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

public class HealthBarRenderer {
   private static final Identifier WHITE_TEX = Identifier.of("voidcyan", "textures/gui/white.png");

   public static void renderAll3D(WorldRenderContext ctx) {
      if (VoidCyanClient.isHealthIndicatorsEnabled) {
         if (VoidCyanClient.healthIndicatorsMode == 0) {
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
                           double sqDist = dx * dx + dy * dy + dz * dz;
                           if (!(sqDist > maxDistSq)) {
                              float health = living.getHealth();
                              float absorption = living.getAbsorptionAmount();
                              float maxHealth = living.getMaxHealth();
                              float barMax = Math.max(maxHealth, health + absorption);
                              float goldenPct = absorption / barMax;
                              float cyanPct = health / barMax;
                              float entityHeight = living.getHeight();
                              float halfWidth = living.getWidth() / 2.0F;
                              VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(WHITE_TEX));
                              if (VoidCyanClient.healthIndicatorsType == 0) {
                                 float barYWorld = entityHeight + 0.8F;
                                 double targetY = dy + barYWorld;
                                 double dist = Math.sqrt(dx * dx + targetY * targetY + dz * dz);
                                 float maxOffset = (float)Math.max(0.01, dist - 0.2);
                                 float offset = Math.min(living.getWidth() / 2.0F + 0.15F, maxOffset);
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
                                 float barWidth = 1.0F;
                                 float barHeight = 0.15F;
                                 float barYLocal = 0.0F;
                                 drawQuad(
                                    entry,
                                    vc,
                                    -barWidth / 2.0F - 0.02F,
                                    barYLocal - 0.02F,
                                    0.0F,
                                    barWidth / 2.0F + 0.02F,
                                    barYLocal + barHeight + 0.02F,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    0.6F
                                 );
                                 drawQuad(entry, vc, -barWidth / 2.0F, barYLocal, 0.001F, barWidth / 2.0F, barYLocal + barHeight, 0.2F, 0.2F, 0.2F, 1.0F);
                                 float goldenWidth = barWidth * goldenPct;
                                 if (goldenWidth > 0.0F) {
                                    drawQuad(
                                       entry,
                                       vc,
                                       -barWidth / 2.0F,
                                       barYLocal,
                                       0.002F,
                                       -barWidth / 2.0F + goldenWidth,
                                       barYLocal + barHeight,
                                       1.0F,
                                       0.84F,
                                       0.0F,
                                       1.0F
                                    );
                                 }

                                 float cyanWidth = barWidth * cyanPct;
                                 if (cyanWidth > 0.0F) {
                                    drawQuad(
                                       entry,
                                       vc,
                                       -barWidth / 2.0F + goldenWidth,
                                       barYLocal,
                                       0.002F,
                                       -barWidth / 2.0F + goldenWidth + cyanWidth,
                                       barYLocal + barHeight,
                                       0.33F,
                                       1.0F,
                                       1.0F,
                                       1.0F
                                    );
                                 }

                                 matrices.pop();
                              } else {
                                 double distXZ = Math.sqrt(dx * dx + dz * dz);
                                 float maxOffsetx = (float)Math.max(0.01, distXZ - 0.2);
                                 float offsetx = Math.min(living.getWidth() / 2.0F + 0.15F, maxOffsetx);
                                 double renderXx = dx;
                                 double renderZx = dz;
                                 float scalex = 1.0F;
                                 if (distXZ > 0.001) {
                                    renderXx = dx - dx / distXZ * offsetx;
                                    renderZx = dz - dz / distXZ * offsetx;
                                    scalex = (float)((distXZ - offsetx) / distXZ);
                                 }

                                 matrices.push();
                                 matrices.translate(renderXx, dy, renderZx);
                                 double toCamX = cameraPos.x - living.getX();
                                 double toCamZ = cameraPos.z - living.getZ();
                                 float yaw = (float)Math.atan2(toCamZ, toCamX) - (float) (Math.PI / 2);
                                 matrices.multiply(RotationAxis.NEGATIVE_Y.rotation(yaw));
                                 matrices.scale(scalex, scalex, scalex);
                                 Entry entryx = matrices.peek();
                                 float offsetX = VoidCyanClient.healthIndicatorsType == 1 ? -(halfWidth + 0.3F) : halfWidth + 0.3F;
                                 float barWidthx = 0.15F;
                                 float barY = 0.0F;
                                 drawQuad(
                                    entryx,
                                    vc,
                                    offsetX - barWidthx / 2.0F - 0.02F,
                                    barY - 0.02F,
                                    0.0F,
                                    offsetX + barWidthx / 2.0F + 0.02F,
                                    barY + entityHeight + 0.02F,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    0.6F
                                 );
                                 drawQuad(
                                    entryx,
                                    vc,
                                    offsetX - barWidthx / 2.0F,
                                    barY,
                                    0.001F,
                                    offsetX + barWidthx / 2.0F,
                                    barY + entityHeight,
                                    0.2F,
                                    0.2F,
                                    0.2F,
                                    1.0F
                                 );
                                 float goldenHeight = entityHeight * goldenPct;
                                 if (goldenHeight > 0.0F) {
                                    drawQuad(
                                       entryx,
                                       vc,
                                       offsetX - barWidthx / 2.0F,
                                       barY,
                                       0.002F,
                                       offsetX + barWidthx / 2.0F,
                                       barY + goldenHeight,
                                       1.0F,
                                       0.84F,
                                       0.0F,
                                       1.0F
                                    );
                                 }

                                 float cyanHeight = entityHeight * cyanPct;
                                 if (cyanHeight > 0.0F) {
                                    drawQuad(
                                       entryx,
                                       vc,
                                       offsetX - barWidthx / 2.0F,
                                       barY + goldenHeight,
                                       0.002F,
                                       offsetX + barWidthx / 2.0F,
                                       barY + goldenHeight + cyanHeight,
                                       0.33F,
                                       1.0F,
                                       1.0F,
                                       1.0F
                                    );
                                 }

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

   private static void drawQuad(Entry entry, VertexConsumer vc, float x1, float y1, float z, float x2, float y2, float r, float g, float b, float a) {
      vc.vertex(entry, x1, y1, z).color(r, g, b, a).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y1, z).color(r, g, b, a).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x2, y2, z).color(r, g, b, a).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
      vc.vertex(entry, x1, y2, z).color(r, g, b, a).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0.0F, 1.0F, 0.0F);
   }
}
