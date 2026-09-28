package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class ChinaHatRenderer {

   private ChinaHatRenderer() {
   }

   public static void renderAll(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (VoidCyanClient.isChinaHatEnabled) {
            Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
            VertexConsumerProvider consumers = context.consumers();
            if (consumers != null) {
               MatrixStack matrices = context.matrices();
               float r = VoidCyanClient.chinaHatRed / 255.0F;
               float g = VoidCyanClient.chinaHatGreen / 255.0F;
               float b = VoidCyanClient.chinaHatBlue / 255.0F;
               float size = Math.max(0.1F, VoidCyanClient.chinaHatSize);
               float brimRadius = 0.55F * size;
               float topY = 0.45F * size;
               float brimY = 0.0F;

               for (PlayerEntity player : client.world.getPlayers()) {
                  if ((player != client.player || !client.options.getPerspective().isFirstPerson()) && !player.isRemoved() && player.isAlive()) {
                     double px = player.getX() - cameraPos.x;
                     double py = player.getY() - cameraPos.y + player.getHeight() + 0.05;
                     double pz = player.getZ() - cameraPos.z;
                     matrices.push();
                     matrices.translate(px, py, pz);
                     matrices.multiply(new Quaternionf().rotationY(-player.getYaw() * (float) (Math.PI / 180.0)));
                     Matrix4f mat = matrices.peek().getPositionMatrix();
                     if (VoidCyanClient.chinaHatFilled) {
                        VertexConsumer filled = consumers.getBuffer(RenderLayers.translucentMovingBlock());
                        float alpha = 0.85F;
                        Sprite sprite = MinecraftClient.getInstance()
                           .getBlockRenderManager()
                           .getModels()
                           .getModelParticleSprite(Blocks.WHITE_CONCRETE.getDefaultState());
                        float u = sprite.getFrameU(0.5F);
                        float v = sprite.getFrameV(0.5F);

                        for (int i = 0; i < 24; i++) {
                           float a1 = (float)((Math.PI * 2) * i / 24.0);
                           float a2 = (float)((Math.PI * 2) * (i + 1) / 24.0);
                           float x1 = (float)Math.cos(a1) * brimRadius;
                           float z1 = (float)Math.sin(a1) * brimRadius;
                           float x2 = (float)Math.cos(a2) * brimRadius;
                           float z2 = (float)Math.sin(a2) * brimRadius;
                           filled.vertex(mat, 0.0F, brimY, 0.0F).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(0.0F, 1.0F, 0.0F);
                           filled.vertex(mat, x1, brimY, z1).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(0.0F, 1.0F, 0.0F);
                           filled.vertex(mat, x2, brimY, z2).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(0.0F, 1.0F, 0.0F);
                           filled.vertex(mat, x2, brimY, z2).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(0.0F, 1.0F, 0.0F);
                           float nx = (float)Math.cos((a1 + a2) / 2.0F);
                           float nz = (float)Math.sin((a1 + a2) / 2.0F);
                           filled.vertex(mat, x1, brimY, z1).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(nx, 0.5F, nz);
                           filled.vertex(mat, x2, brimY, z2).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(nx, 0.5F, nz);
                           filled.vertex(mat, 0.0F, topY, 0.0F).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(nx, 0.5F, nz);
                           filled.vertex(mat, 0.0F, topY, 0.0F).color(r, g, b, alpha).texture(u, v).overlay(0, 10).light(15728880).normal(nx, 0.5F, nz);
                        }
                     } else {
                        VertexConsumer lines = consumers.getBuffer(RenderLayers.linesTranslucent());

                        for (int i = 0; i < 24; i++) {
                           float a1 = (float)((Math.PI * 2) * i / 24.0);
                           float a2 = (float)((Math.PI * 2) * (i + 1) / 24.0);
                           float x1 = (float)Math.cos(a1) * brimRadius;
                           float z1 = (float)Math.sin(a1) * brimRadius;
                           float x2 = (float)Math.cos(a2) * brimRadius;
                           float z2 = (float)Math.sin(a2) * brimRadius;
                           lines.vertex(mat, x1, brimY, z1).color(r, g, b, 1.0F).normal(0.0F, 1.0F, 0.0F).lineWidth(2.0F);
                           lines.vertex(mat, x2, brimY, z2).color(r, g, b, 1.0F).normal(0.0F, 1.0F, 0.0F).lineWidth(2.0F);
                        }

                        for (int i = 0; i < 24; i++) {
                           float a = (float)((Math.PI * 2) * i / 24.0);
                           float bx = (float)Math.cos(a) * brimRadius;
                           float bz = (float)Math.sin(a) * brimRadius;
                           lines.vertex(mat, bx, brimY, bz).color(r, g, b, 1.0F).normal(0.0F, 1.0F, 0.0F).lineWidth(2.0F);
                           lines.vertex(mat, 0.0F, topY, 0.0F).color(r, g, b, 1.0F).normal(0.0F, 1.0F, 0.0F).lineWidth(2.0F);
                        }
                     }

                     matrices.pop();
                  }
               }
            }
         }
      }
   }
}
