package com.voidcyan.client.util;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;

/**
 * Universal Click GUI scaling.
 *
 * The Click GUIs render in a logical coordinate space sized like a vanilla GUI screen:
 * framebuffer / effectiveScale, where effectiveScale = (game GUI Scale factor from
 * Video Settings) x (dedicated Click GUI multiplier, {@link VoidCyanClient#clickGuiScale}).
 *
 * Because the game scale factor is re-read every frame, moving the in-game
 * "GUI Scale" video setting resizes the Click GUI exactly like vanilla menus —
 * the two stay in sync — while the dedicated slider fine-tunes on top of it.
 *
 * Coordinate mapping note: Minecraft already delivers screen-space mouse coordinates
 * divided by the game GUI Scale factor. So coordinates arriving at these screens are
 * only divided by the dedicated {@link #multiplier()} to land in the logical space,
 * and the render matrix likewise scales by only the multiplier (the game scale is
 * already baked into the baseline coordinate space). Dividing by the full effective
 * scale here would double-apply the game scale and desync every hit region.
 */
public final class GuiScaleManager {
   private static float gameScaleFactor = 3.0F;
   private static float multiplier = 1.0F;

   private GuiScaleManager() {
   }

   /** Refreshes from the live window; call at the start of each frame or input event. */
   public static void update(MinecraftClient client) {
      gameScaleFactor = client != null && client.getWindow() != null ? client.getWindow().getScaleFactor() : 3.0F;
      multiplier = VoidCyanClient.clickGuiScale > 0.0F ? VoidCyanClient.clickGuiScale : 1.0F;
   }

   /** The game's GUI Scale factor (from Video Settings / the window). */
   public static float gameScale() {
      return gameScaleFactor;
   }

   /** The dedicated Click GUI multiplier applied on top of the game scale. */
   public static float multiplier() {
      return multiplier;
   }

   /** (game GUI Scale) x (dedicated Click GUI multiplier). */
   public static float effectiveScale() {
      return gameScaleFactor * multiplier;
   }

   /** Logical space width (vanilla-style, computed from the framebuffer). */
   public static int logicalWidth(MinecraftClient client) {
      return client != null && client.getWindow() != null
         ? Math.max(1, (int)((float)client.getWindow().getFramebufferWidth() / effectiveScale()))
         : 480;
   }

   /** Logical space height (vanilla-style, computed from the framebuffer). */
   public static int logicalHeight(MinecraftClient client) {
      return client != null && client.getWindow() != null
         ? Math.max(1, (int)((float)client.getWindow().getFramebufferHeight() / effectiveScale()))
         : 320;
   }

   /**
    * Maps a coordinate that is already in vanilla screen space (Minecraft has already
    * divided framebuffer pixels by the game GUI Scale) into this GUI's logical space.
    * Applies only the dedicated multiplier.
    */
   public static double toLogical(double coord) {
      return coord / multiplier;
   }

   /** Maps a vanilla Click (already game-scaled) into the GUI's logical space. */
   public static Click toLogical(Click click) {
      return new Click(
         (int)toLogical((double)click.x()),
         (int)toLogical((double)click.y()),
         new MouseInput(click.button(), click.buttonInfo().modifiers())
      );
   }
}
