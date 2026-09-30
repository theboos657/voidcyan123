package com.voidcyan.client.util;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;

/**
 * Universal Click GUI scaling, independent of the vanilla "GUI Scale" video setting.
 *
 * Logical width/height = raw framebuffer pixels / dedicated multiplier
 * ({@link VoidCyanClient#clickGuiScale}). Framebuffer pixel dimensions don't change
 * with the video setting, so the layout is identical at every GUI Scale (1x-4x+);
 * only the multiplier controls size, matching normal "scale slider" semantics
 * (higher = bigger).
 *
 * Vanilla mouse coords and the DrawContext arrive in vanilla-scaled space (1 unit =
 * gameScaleFactor physical pixels), so both the render matrix and mouse mapping
 * carry a gameScaleFactor/multiplier conversion between that space and this one.
 */
public final class GuiScaleManager {
   private static float gameScaleFactor = 1.0F;
   private static float multiplier = 1.0F;

   private GuiScaleManager() {
   }

   /** Refreshes from the live window; call at the start of each frame or input event. */
   public static void update(MinecraftClient client) {
      gameScaleFactor = client != null && client.getWindow() != null ? client.getWindow().getScaleFactor() : 1.0F;
      multiplier = VoidCyanClient.clickGuiScale > 0.0F ? VoidCyanClient.clickGuiScale : 1.0F;
      // Cool profile: keep text readable on high-res screens (1080p -> 1.5x).
      if (VoidCyanClient.guiType <= 1 && client != null && client.getWindow() != null) {
         multiplier *= Math.max(1.0F, client.getWindow().getFramebufferHeight() / (VoidCyanClient.guiType == 0 ? 1000.0F : 720.0F));
      }
   }

   /** The dedicated Click GUI multiplier; the only control over rendered size. */
   public static float multiplier() {
      return multiplier;
   }

   /** Converts a logical-space length to vanilla-screen-space (for the render matrix). */
   public static float renderScale() {
      return multiplier / gameScaleFactor;
   }

   /** Logical space width: raw framebuffer pixels / multiplier, ignoring GUI Scale. */
   public static int logicalWidth(MinecraftClient client) {
      return client != null && client.getWindow() != null
         ? Math.max(1, (int)((float)client.getWindow().getFramebufferWidth() / multiplier))
         : 960;
   }

   /** Logical space height: raw framebuffer pixels / multiplier, ignoring GUI Scale. */
   public static int logicalHeight(MinecraftClient client) {
      return client != null && client.getWindow() != null
         ? Math.max(1, (int)((float)client.getWindow().getFramebufferHeight() / multiplier))
         : 540;
   }

   /** Maps a vanilla-screen-space coordinate into this GUI's logical space. */
   public static double toLogical(double coord) {
      return coord * gameScaleFactor / multiplier;
   }

   /** Maps a vanilla Click into the GUI's logical space. */
   public static Click toLogical(Click click) {
      return new Click(
         (int)toLogical((double)click.x()),
         (int)toLogical((double)click.y()),
         new MouseInput(click.button(), click.buttonInfo().modifiers())
      );
   }
}
