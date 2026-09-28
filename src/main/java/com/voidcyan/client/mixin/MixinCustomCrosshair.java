package com.voidcyan.client.mixin;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class MixinCustomCrosshair {
   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      if (VoidCyanClient.isCustomCrosshairEnabled) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.options.getPerspective().isFirstPerson()) {
            ci.cancel();
            this.renderCustomCrosshair(context, client);
         }
      }
   }

   private void renderCustomCrosshair(DrawContext context, MinecraftClient client) {
      int screenW = client.getWindow().getScaledWidth();
      int screenH = client.getWindow().getScaledHeight();
      int cx = screenW / 2;
      int cy = screenH / 2;
      int r = VoidCyanClient.crosshairRed;
      int g = VoidCyanClient.crosshairGreen;
      int b = VoidCyanClient.crosshairBlue;
      int a = VoidCyanClient.crosshairAlpha;
      int color = a << 24 | r << 16 | g << 8 | b;
      int size = VoidCyanClient.crosshairSize;
      int thick = Math.max(1, VoidCyanClient.crosshairThickness);
      switch (VoidCyanClient.crosshairStyle) {
         case 0:
            context.fill(cx - size, cy - thick / 2, cx + size + 1, cy + thick / 2 + 1, color);
            context.fill(cx - thick / 2, cy - size, cx + thick / 2 + 1, cy + size + 1, color);
            if (VoidCyanClient.crosshairDot) {
               context.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
            break;
         case 1:
            context.fill(cx - thick, cy - thick, cx + thick + 1, cy + thick + 1, color);
            break;
         case 2:
            int radius = size;

            for (int angle = 0; angle < 360; angle += 5) {
               double rad = Math.toRadians(angle);
               int px = cx + (int)(Math.cos(rad) * radius);
               int py = cy + (int)(Math.sin(rad) * radius);
               context.fill(px - thick, py - thick, px + thick + 1, py + thick + 1, color);
            }

            if (VoidCyanClient.crosshairDot) {
               context.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
            }
      }
   }
}
