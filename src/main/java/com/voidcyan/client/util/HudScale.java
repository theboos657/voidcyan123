package com.voidcyan.client.util;

import net.minecraft.client.MinecraftClient;

/**
 * HUD layout space that does not depend on the vanilla "GUI Scale" video setting.
 *
 * HUD elements are positioned in a logical space that is always {@link #LOGICAL_H} units tall (and as wide as the
 * window's aspect ratio makes it), so a given layout looks the same at every GUI Scale and on every window size.
 * 270 is the height of the vanilla scaled space at 1080p / GUI Scale 4, which is what the default layout was made on.
 */
public final class HudScale {
   public static final float LOGICAL_H = 270.0F;

   private HudScale() {
   }

   /** Multiplier from logical HUD units to vanilla scaled units. */
   public static float k() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null || mc.getWindow() == null) return 1.0F;
      float sf = (float)mc.getWindow().getScaleFactor();
      float fbH = mc.getWindow().getFramebufferHeight();
      if (sf <= 0.0F || fbH <= 0.0F) return 1.0F;
      return Math.max(0.2F, Math.min(10.0F, fbH / LOGICAL_H / sf));
   }

   public static int logicalWidth() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null || mc.getWindow() == null) return 480;
      return Math.max(1, Math.round(mc.getWindow().getScaledWidth() / k()));
   }

   public static int logicalHeight() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null || mc.getWindow() == null) return 270;
      return Math.max(1, Math.round(mc.getWindow().getScaledHeight() / k()));
   }
}
