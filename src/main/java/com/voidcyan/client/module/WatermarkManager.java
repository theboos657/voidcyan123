package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class WatermarkManager {

   public static int getWidth(MinecraftClient client) {
      return client != null && client.textRenderer != null ? client.textRenderer.getWidth("VoidCyan Client v1.0.0") + 4 : 100;
   }

   public static int getHeight() {
      return 12;
   }

   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.textRenderer != null) {
         int color;
         if (VoidCyanClient.watermarkUseThemeColor) {
            if (VoidCyanClient.watermarkAnimatedColor) {
               long time = System.currentTimeMillis();
               color = getAnimatedThemeColor(time, 0);
            } else {
               color = 0xFF000000 | VoidCyanClient.getPrimaryColor();
            }
         } else {
            color = 0xFF000000 | VoidCyanClient.watermarkCustomColor;
         }

         context.fill(0, 0, getWidth(client), getHeight(), 1610612736);
         context.drawTextWithShadow(client.textRenderer, "VoidCyan Client v1.0.0", 2, 2, color);
      }
   }

   public static int getThemeColor(long time, int offset) {
      float speed = 2000.0F;
      float cycle = (float)((time + offset) % (long)speed) / speed;
      float wave = Math.abs(cycle * 2.0F - 1.0F);
      int primary = VoidCyanClient.getPrimaryColor() & 16777215;
      int baseR = primary >> 16 & 0xFF;
      int baseG = primary >> 8 & 0xFF;
      int baseB = primary & 0xFF;
      int r = (int)Math.min(255.0F, baseR + (255 - baseR) * wave);
      int g = (int)Math.min(255.0F, baseG + (255 - baseG) * wave);
      int b = (int)Math.min(255.0F, baseB + (255 - baseB) * wave);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static int getAnimatedThemeColor(long time, int offset) {
      return getThemeColor(time, offset);
   }
}
