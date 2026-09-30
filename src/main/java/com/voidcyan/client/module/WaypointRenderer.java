package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import com.voidcyan.client.util.WaypointManager;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class WaypointRenderer {
   private static boolean depthMethodsResolved = false;
   private static Method disableDepthTestMethod = null;
   private static Method enableDepthTestMethod = null;

   public static void renderAll3D(WorldRenderContext context) {
      if (VoidCyanClient.isWaypointsEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null && client.player != null) {
            String currentDim = client.world.getRegistryKey().getValue().getPath();
            Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
            MatrixStack matrices = context.matrices();
            VertexConsumerProvider consumers = context.consumers();
            if (matrices != null && consumers != null) {
               VertexConsumer lineBuffer = consumers.getBuffer(RenderLayers.linesTranslucent());

               for (WaypointManager.Waypoint wp : WaypointManager.waypoints) {
                  if (wp.enabled && wp.dimension.equals(currentDim)) {
                     double minX = wp.x - wp.radius;
                     double minY = wp.y - wp.radius;
                     double minZ = wp.z - wp.radius;
                     double maxX = wp.x + wp.radius + 1.0;
                     double maxY = wp.y + wp.radius + 1.0;
                     double maxZ = wp.z + wp.radius + 1.0;
                     Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
                     VoxelShape shape = VoxelShapes.cuboid(box);
                     if (wp.showBlockDisplay) {
                        boolean throughWalls = wp.renderThroughWalls;
                        if (throughWalls) {
                           setDepthTestEnabled(false);
                        }

                        if (wp.outlineOpacity > 0) {
                           int outlineColor = wp.outlineOpacity * 255 / 100 << 24 | wp.color & 16777215;
                           VertexConsumer outlineConsumer = throughWalls ? consumers.getBuffer(RenderLayers.lines()) : lineBuffer;
                           VertexRendering.drawOutline(
                              matrices, outlineConsumer, shape, box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z, outlineColor, 2.0F
                           );
                        }

                        if (wp.fillOpacity > 0) {
                           int fillColor = wp.fillOpacity * 255 / 100 << 24 | wp.color & 16777215;
                           float fillThickness = 3.0F;
                           VertexConsumer fillConsumer = throughWalls ? consumers.getBuffer(RenderLayers.lines()) : lineBuffer;
                           VertexRendering.drawOutline(
                              matrices, fillConsumer, shape, box.minX - cameraPos.x, box.minY - cameraPos.y, box.minZ - cameraPos.z, fillColor, fillThickness
                           );
                        }

                        if (throughWalls) {
                           setDepthTestEnabled(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void setDepthTestEnabled(boolean enabled) {
      if (!depthMethodsResolved) {
         depthMethodsResolved = true;

         try {
            Class<?> rs = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
            disableDepthTestMethod = rs.getMethod("disableDepthTest");
            enableDepthTestMethod = rs.getMethod("enableDepthTest");
         } catch (Throwable var3) {
            disableDepthTestMethod = null;
            enableDepthTestMethod = null;
         }
      }

      try {
         if (enabled) {
            if (enableDepthTestMethod != null) {
               enableDepthTestMethod.invoke(null);
            }
         } else if (disableDepthTestMethod != null) {
            disableDepthTestMethod.invoke(null);
         }
      } catch (Throwable var2) {
      }
   }
}
