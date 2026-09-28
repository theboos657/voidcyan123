package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class BlockOverlaySettingsScreen extends BaseSettingsScreen {
   public BlockOverlaySettingsScreen(Screen parent) {
      super(parent, "Block Overlay Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Mode", new String[]{"Full Block", "Air Exposed"}, () -> VoidCyanClient.blockOverlayMode, val -> VoidCyanClient.blockOverlayMode = val);
      this.addBoolean("Outline", () -> VoidCyanClient.blockOverlayOutline, val -> VoidCyanClient.blockOverlayOutline = val);
      this.addBoolean("Fill", () -> VoidCyanClient.blockOverlayFill, val -> VoidCyanClient.blockOverlayFill = val);
      this.addBoolean("Glow", () -> VoidCyanClient.blockOverlayGlow, val -> VoidCyanClient.blockOverlayGlow = val);
      this.addIntSlider("Red", 0, 255, () -> VoidCyanClient.blockOverlayRed, val -> VoidCyanClient.blockOverlayRed = val);
      this.addIntSlider("Green", 0, 255, () -> VoidCyanClient.blockOverlayGreen, val -> VoidCyanClient.blockOverlayGreen = val);
      this.addIntSlider("Blue", 0, 255, () -> VoidCyanClient.blockOverlayBlue, val -> VoidCyanClient.blockOverlayBlue = val);
      this.addSlider("Thickness", 0.5F, 8.0F, () -> VoidCyanClient.blockOverlayThickness, val -> VoidCyanClient.blockOverlayThickness = val);
      this.addIntSlider("Fill Opacity %", 0, 100, () -> VoidCyanClient.blockOverlayFillOpacity, val -> VoidCyanClient.blockOverlayFillOpacity = val);
      this.addSlider("Glow Strength", 0.5F, 3.0F, () -> VoidCyanClient.blockOverlayGlowStrength, val -> VoidCyanClient.blockOverlayGlowStrength = val);
   }
}
