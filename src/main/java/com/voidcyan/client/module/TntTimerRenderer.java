package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class TntTimerRenderer {
   public static int maxDistance = 64;

   private TntTimerRenderer() {
   }

   public static void renderHud(DrawContext context) {
      if (VoidCyanClient.isTntTimerEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            if (client.currentScreen == null) {
               int screenW = client.getWindow().getScaledWidth();
               int screenH = client.getWindow().getScaledHeight();
               Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
               double maxDistSq = (double)maxDistance * maxDistance;

               for (Entity entity : client.world.getEntities()) {
                  if (entity instanceof TntEntity tnt) {
                     double ex = entity.getX();
                     double ey = entity.getY() + entity.getHeight() + 0.35;
                     double ez = entity.getZ();
                     double dx = ex - cameraPos.x;
                     double dy = ey - cameraPos.y;
                     double dz = ez - cameraPos.z;
                     if (!(dx * dx + dy * dy + dz * dz > maxDistSq)) {
                        Vec3d projected = client.gameRenderer.project(new Vec3d(ex, ey, ez));
                        if (!(projected.z <= 0.0) && !(projected.z > 1.0)) {
                           int screenX = (int)((projected.x * 0.5 + 0.5) * screenW);
                           int screenY = (int)((1.0 - (projected.y * 0.5 + 0.5)) * screenH);
                           if (screenX >= -40 && screenX <= screenW + 40 && screenY >= -40 && screenY <= screenH + 40) {
                              float seconds = tnt.getFuse() / 20.0F;
                              String label = String.format("%.1fs", Math.max(0.0F, seconds));
                              int textW = client.textRenderer.getWidth(label);
                              int boxW = textW + 10;
                              int boxH = 16;
                              int boxX = screenX - boxW / 2;
                              int boxY = screenY - boxH / 2;
                              int color = colorForSeconds(seconds);
                              context.fill(boxX, boxY, boxX + boxW, boxY + boxH, -1442840576);
                              context.fill(boxX, boxY, boxX + boxW, boxY + 1, -16711681);
                              context.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, -16711681);
                              context.fill(boxX, boxY, boxX + 1, boxY + boxH, -16711681);
                              context.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, -16711681);
                              context.drawTextWithShadow(client.textRenderer, Text.literal(label), boxX + 5, boxY + 4, color);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int colorForSeconds(float seconds) {
      if (seconds <= 1.0F) {
         return -43691;
      } else {
         return seconds <= 2.0F ? -171 : -11141291;
      }
   }
}
