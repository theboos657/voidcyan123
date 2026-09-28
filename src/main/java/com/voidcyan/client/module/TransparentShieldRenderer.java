package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class TransparentShieldRenderer {

   private TransparentShieldRenderer() {
   }

   public static void render(DrawContext context) {
      if (VoidCyanClient.isTransparentShieldEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            float health = client.player.getHealth();
            float maxHealth = client.player.getMaxHealth();
            float fillPercent = Math.min(1.0F, health / maxHealth);
            int fillWidth = (int)(182.0F * fillPercent);
            int color = VoidCyanClient.transparentShieldStatusColor;
            int outlineColor = VoidCyanClient.transparentShieldStatusOutlineColor;
            int outlineRed = outlineColor >> 16 & 0xFF;
            int outlineGreen = outlineColor >> 8 & 0xFF;
            int outlineBlue = outlineColor & 0xFF;
            context.fill(0, 0, 182, 1, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
            context.fill(0, 8, 182, 9, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
            context.fill(0, 0, 1, 9, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
            context.fill(181, 0, 182, 9, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
            int fillRed = color >> 16 & 0xFF;
            int fillGreen = color >> 8 & 0xFF;
            int fillBlue = color & 0xFF;
            context.fill(0, 0, fillWidth, 9, -2147483648 | fillRed << 16 | fillGreen << 8 | fillBlue);
         }
      }
   }

   public static void renderAt(DrawContext context, int boxX, int boxY, int boxWidth, int boxHeight) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         float health = client.player.getHealth();
         float maxHealth = client.player.getMaxHealth();
         float fillPercent = Math.min(1.0F, health / maxHealth);
         int fillWidth = (int)(boxWidth * fillPercent);
         int color = VoidCyanClient.transparentShieldStatusColor;
         int outlineColor = VoidCyanClient.transparentShieldStatusOutlineColor;
         int outlineRed = outlineColor >> 16 & 0xFF;
         int outlineGreen = outlineColor >> 8 & 0xFF;
         int outlineBlue = outlineColor & 0xFF;
         context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
         context.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
         context.fill(boxX, boxY, boxX + 1, boxY + boxHeight, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
         context.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF000000 | outlineRed << 16 | outlineGreen << 8 | outlineBlue);
         int fillRed = color >> 16 & 0xFF;
         int fillGreen = color >> 8 & 0xFF;
         int fillBlue = color & 0xFF;
         context.fill(boxX, boxY, boxX + fillWidth, boxY + boxHeight, -2147483648 | fillRed << 16 | fillGreen << 8 | fillBlue);
      }
   }
}
