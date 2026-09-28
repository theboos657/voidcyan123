package com.voidcyan.client.module;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class AttackHudRenderer {
   public static void render(DrawContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.player != null && client.world != null) {
         if (!client.options.hudHidden) {
            if (VoidCyanClient.isAttackHudEnabled) {
               float progress = client.player.getAttackCooldownProgress(0.0F);
               int mode = VoidCyanClient.attackHudMode;
               int size = VoidCyanClient.attackHudSize;
               int x = VoidCyanClient.attackHudX;
               int y = VoidCyanClient.attackHudY;
               switch (mode) {
                  case 0:
                     renderItemMode(context, client, progress, x, y, size);
                     break;
                  case 1:
                     renderCustomMode(context, progress, x, y, size);
                     break;
                  case 2:
                     renderVanillaMode(context, progress, x, y, size);
               }
            }
         }
      }
   }

   private static void renderItemMode(DrawContext context, MinecraftClient client, float progress, int x, int y, int size) {
      ItemStack held = client.player.getMainHandStack();
      int cx = x + size / 2;
      int cy = y + size / 2;
      int r = size / 2;
      fillCircle(context, cx, cy, r, -2013265920);
      int primaryColor = VoidCyanClient.getPrimaryColor();
      drawArc(context, cx, cy, r - 2, r, progress, primaryColor);
      int itemSize = size * 2 / 3;
      int itemX = x + (size - itemSize) / 2;
      int itemY = y + (size - itemSize) / 2;
      context.getMatrices().pushMatrix();
      float scale = itemSize / 16.0F;
      context.getMatrices().scale(scale, scale);
      context.drawItem(held, (int)(itemX / scale), (int)(itemY / scale));
      context.getMatrices().popMatrix();
   }

   private static void renderCustomMode(DrawContext context, float progress, int x, int y, int size) {
      int barH = Math.max(4, size / 5);
      int barY = y + size - barH;
      context.fill(x, y, x + size, barY + barH, -2013265920);
      int primaryColor = VoidCyanClient.getPrimaryColor();
      int fillW = (int)(size * progress);
      if (fillW > 0) {
         context.fill(x, barY, x + fillW, barY + barH, primaryColor | 0xFF000000);
      }

      context.fill(x, barY, x + size, barY + 1, -3355444);
      context.fill(x, barY + barH - 1, x + size, barY + barH, -3355444);
      context.fill(x, barY, x + 1, barY + barH, -3355444);
      context.fill(x + size - 1, barY, x + size, barY + barH, -3355444);
      if (size >= 30) {
         int pct = Math.round(progress * 100.0F);
         String txt = pct + "%";
         int textColor = progress >= 1.0F ? primaryColor | 0xFF000000 : -5592406;
         context.drawTextWithShadow(
            MinecraftClient.getInstance().textRenderer,
            txt,
            x + (size - MinecraftClient.getInstance().textRenderer.getWidth(txt)) / 2,
            y + (size - barH) / 2 - 4,
            textColor
         );
      }
   }

   private static void renderVanillaMode(DrawContext context, float progress, int x, int y, int size) {
      int cx = x + size / 2;
      int cy = y + size / 2;
      int r = size / 2;
      fillCircle(context, cx, cy, r, -2013265920);
      int alpha = progress >= 1.0F ? 255 : 204;
      int sweepColor = alpha << 24 | 16777215;
      drawArc(context, cx, cy, 0, r - 4, progress, sweepColor);
      drawRing(context, cx, cy, r - 1, r, -11184811);
   }

   private static void fillCircle(DrawContext ctx, int cx, int cy, int r, int color) {
      for (int dy = -r; dy <= r; dy++) {
         int half = (int)Math.sqrt(r * r - dy * dy);
         ctx.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
      }
   }

   private static void drawRing(DrawContext ctx, int cx, int cy, int innerR, int outerR, int color) {
      for (int dy = -outerR; dy <= outerR; dy++) {
         int outerHalf = (int)Math.sqrt(Math.max(0, outerR * outerR - dy * dy));
         int innerHalf = (int)Math.sqrt(Math.max(0, innerR * innerR - dy * dy));
         ctx.fill(cx - outerHalf, cy + dy, cx - innerHalf, cy + dy + 1, color);
         ctx.fill(cx + innerHalf, cy + dy, cx + outerHalf, cy + dy + 1, color);
      }
   }

   private static void drawArc(DrawContext ctx, int cx, int cy, int innerR, int outerR, float progress, int color) {
      if (!(progress <= 0.0F)) {
         float sweepAngle = progress * (float) (Math.PI * 2);
         int steps = Math.max(36, outerR * 4);

         for (int i = 0; i < steps; i++) {
            float a0 = (float)i / steps * (float) (Math.PI * 2) - (float) (Math.PI / 2);
            float a1 = (float)(i + 1) / steps * (float) (Math.PI * 2) - (float) (Math.PI / 2);
            float segStart = a0 + (float) (Math.PI / 2);
            if (segStart < 0.0F) {
               segStart += (float) (Math.PI * 2);
            }

            float segEnd = a1 + (float) (Math.PI / 2);
            if (segEnd < 0.0F) {
               segEnd += (float) (Math.PI * 2);
            }

            if (!(segStart > sweepAngle)) {
               for (int dr = innerR; dr <= outerR; dr++) {
                  int px0 = cx + (int)(Math.cos(a0) * dr);
                  int py0 = cy + (int)(Math.sin(a0) * dr);
                  int px1 = cx + (int)(Math.cos(a1) * dr);
                  int py1 = cy + (int)(Math.sin(a1) * dr);
                  ctx.fill(px0, py0, px0 + 1, py0 + 1, color);
                  ctx.fill(px1, py1, px1 + 1, py1 + 1, color);
               }
            }
         }
      }
   }
}
