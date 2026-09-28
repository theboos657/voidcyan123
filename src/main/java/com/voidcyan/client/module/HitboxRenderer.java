package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class HitboxRenderer {
   private static float crystalAlphaProgress = 0.0F;
   private static final float CRYSTAL_APPEAR_SPEED = 0.18F;
   private static final float CRYSTAL_DISMISS_SPEED = 0.1F;

   public static void renderAll(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         if (VoidCyanClient.hitboxShowCrystal) {
            crystalAlphaProgress = crystalAlphaProgress + (1.0F - crystalAlphaProgress) * 0.18F;
            if (crystalAlphaProgress > 0.999F) {
               crystalAlphaProgress = 1.0F;
            }
         } else {
            crystalAlphaProgress = crystalAlphaProgress - crystalAlphaProgress * 0.1F;
            if (crystalAlphaProgress < 0.001F) {
               crystalAlphaProgress = 0.0F;
            }
         }

         MatrixStack matrices = context.matrices();
         VertexConsumerProvider consumers = context.consumers();
         if (consumers != null) {
            VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayers.linesTranslucent());
            Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
            int a = 255;
            int r = VoidCyanClient.hitboxRed;
            int g = VoidCyanClient.hitboxGreen;
            int b = VoidCyanClient.hitboxBlue;
            int color = a << 24 | r << 16 | g << 8 | b;
            int crystalAlpha = (int)(crystalAlphaProgress * 255.0F);
            int crystalColor = crystalAlpha << 24
               | VoidCyanClient.crystalHitboxRed << 16
               | VoidCyanClient.crystalHitboxGreen << 8
               | VoidCyanClient.crystalHitboxBlue;
            float thickness = VoidCyanClient.hitboxThickness;
            boolean renderCrystals = crystalAlphaProgress > 0.0F;

            for (Entity entity : client.world.getEntities()) {
               boolean isPlayer = entity instanceof PlayerEntity;
               boolean isCrystal = entity instanceof EndCrystalEntity;
               if ((isPlayer || isCrystal && renderCrystals) && (entity != client.getCameraEntity() || !client.options.getPerspective().isFirstPerson())) {
                  double distance = entity.squaredDistanceTo(client.player);
                  double maxDistanceSquared = VoidCyanClient.customHitboxRange * VoidCyanClient.customHitboxRange;
                  if (!(distance > maxDistanceSquared)) {
                     int finalColor;
                     if (isCrystal) {
                        finalColor = crystalColor;
                     } else if (VoidCyanClient.isCustomHitboxDynamic) {
                        boolean isTargeted = false;
                        if (client.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == entity) {
                           isTargeted = true;
                        }

                        finalColor = isTargeted ? -65536 : -16777216;
                     } else {
                        finalColor = color;
                     }

                     Box box = entity.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                     VoxelShape shape = VoxelShapes.cuboid(box);
                     VertexRendering.drawOutline(matrices, vertexConsumer, shape, 0.0, 0.0, 0.0, finalColor, thickness);
                  }
               }
            }
         }
      }
   }
}
