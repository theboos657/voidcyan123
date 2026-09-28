package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class TextHudRenderer {
   public static int getWidth() {
      MinecraftClient client = MinecraftClient.getInstance();
      String text = VoidCyanClient.textHudString == null ? "" : VoidCyanClient.textHudString;
      return client.textRenderer.getWidth(text) + 4;
   }

   public static int getHeight() {
      return 12;
   }

   public static void render(DrawContext context, int x, int y) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         String text = VoidCyanClient.textHudString == null ? "" : VoidCyanClient.textHudString;
         int width = getWidth();
         int height = getHeight();
         int bgColor = VoidCyanClient.textHudBgColor;
         int bgAlpha = VoidCyanClient.textHudBgAlpha;
         int bgFinal = Math.max(0, Math.min(255, bgAlpha)) << 24 | bgColor & 16777215;
         int textColor = VoidCyanClient.textHudTextColor | 0xFF000000;
         context.fill(x, y, x + width, y + height, bgFinal);
         context.drawTextWithShadow(client.textRenderer, text, x + 2, y + 2, textColor);
      }
   }
}
