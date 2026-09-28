package com.voidcyan.client.module.damagehearts;

import com.voidcyan.client.VoidCyanClient;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public class HeartRenderer {
   private static final Identifier HEART_TEX = Identifier.of("voidcyan", "textures/gui/heart.png");

   public static void renderAll3D(WorldRenderContext ctx) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         Camera camera = client.gameRenderer.getCamera();
         if (camera != null) {
            Vec3d cameraPos = camera.getCameraPos();
            Quaternionf cameraRotation = camera.getRotation();
            MatrixStack matrices = ctx.matrices();
            VertexConsumerProvider consumers = ctx.consumers();
            if (matrices != null && consumers != null) {
               DamageHeartsModule.isEnabled = VoidCyanClient.isDamageHeartsEnabled;
               List<FloatingHeart> hearts = DamageHeartsModule.getHeartManager().getHearts();
               if (!hearts.isEmpty()) {
                  for (FloatingHeart heart : hearts) {
                     if (!(heart.alpha <= 0.01F)) {
                        double dx = heart.position.x - cameraPos.x;
                        double dy = heart.position.y - cameraPos.y;
                        double dz = heart.position.z - cameraPos.z;
                        double sqDist = dx * dx + dy * dy + dz * dz;
                        if (!(sqDist > 4096.0)) {
                           double dist = Math.sqrt(sqDist);
                           float maxOffset = (float)Math.max(0.01, dist - 0.2);
                           float offset = Math.min(0.6F, maxOffset);
                           double renderX = dx;
                           double renderY = dy;
                           double renderZ = dz;
                           float distanceScale = 1.0F;
                           if (dist > 0.001) {
                              renderX = dx - dx / dist * offset;
                              renderY = dy - dy / dist * offset;
                              renderZ = dz - dz / dist * offset;
                              distanceScale = (float)((dist - offset) / dist);
                           }

                           matrices.push();
                           matrices.translate(renderX, renderY, renderZ);
                           matrices.multiply(cameraRotation);
                           long age = System.currentTimeMillis() - heart.spawnTime;
                           float popScale = 1.0F;
                           if (age < 150L) {
                              popScale = 1.0F + (1.0F - (float)age / 150.0F) * 0.5F;
                           }

                           float baseScale = 0.35F * heart.scale * popScale * DamageHeartsModule.heartScale * distanceScale;
                           matrices.scale(baseScale, baseScale, baseScale);
                           VertexConsumer vc = consumers.getBuffer(RenderLayers.entityTranslucent(HEART_TEX));
                           float u1 = 0.0F;
                           float v1 = 0.0F;
                           float u2 = 1.0F;
                           float v2 = 1.0F;
                           float halfSize = 0.5F;
                           Entry entry = matrices.peek();
                           vc.vertex(entry, -halfSize, -halfSize, 0.0F)
                              .color(1.0F, 1.0F, 1.0F, heart.alpha)
                              .texture(u1, v2)
                              .overlay(OverlayTexture.DEFAULT_UV)
                              .light(15728880)
                              .normal(0.0F, 1.0F, 0.0F);
                           vc.vertex(entry, halfSize, -halfSize, 0.0F)
                              .color(1.0F, 1.0F, 1.0F, heart.alpha)
                              .texture(u2, v2)
                              .overlay(OverlayTexture.DEFAULT_UV)
                              .light(15728880)
                              .normal(0.0F, 1.0F, 0.0F);
                           vc.vertex(entry, halfSize, halfSize, 0.0F)
                              .color(1.0F, 1.0F, 1.0F, heart.alpha)
                              .texture(u2, v1)
                              .overlay(OverlayTexture.DEFAULT_UV)
                              .light(15728880)
                              .normal(0.0F, 1.0F, 0.0F);
                           vc.vertex(entry, -halfSize, halfSize, 0.0F)
                              .color(1.0F, 1.0F, 1.0F, heart.alpha)
                              .texture(u1, v1)
                              .overlay(OverlayTexture.DEFAULT_UV)
                              .light(15728880)
                              .normal(0.0F, 1.0F, 0.0F);
                           matrices.pop();
                           if (DamageHeartsModule.showNumeric) {
                              matrices.push();
                              matrices.translate(renderX, renderY + 0.3, renderZ);
                              matrices.multiply(cameraRotation);
                              matrices.scale(-0.025F * popScale, -0.025F * popScale, 0.025F * popScale);
                              String dmgText = String.format("%.1f", heart.damage);
                              if (dmgText.endsWith(".0")) {
                                 dmgText = dmgText.substring(0, dmgText.length() - 2);
                              }

                              float textWidth = client.textRenderer.getWidth(dmgText);
                              int textColor = (int)(heart.alpha * 255.0F) << 24 | heart.color & 16777215;
                              client.textRenderer
                                 .draw(
                                    dmgText,
                                    -textWidth / 2.0F,
                                    0.0F,
                                    textColor,
                                    false,
                                    matrices.peek().getPositionMatrix(),
                                    consumers,
                                    TextLayerType.NORMAL,
                                    0,
                                    15728880
                                 );
                              matrices.pop();
                           }
                        }
                     }
                  }

                  if (consumers instanceof Immediate immediate) {
                     immediate.draw();
                  }
               }
            }
         }
      }
   }
}
