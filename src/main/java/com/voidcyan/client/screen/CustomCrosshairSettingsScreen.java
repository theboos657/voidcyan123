package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class CustomCrosshairSettingsScreen extends BaseSettingsScreen {
   public CustomCrosshairSettingsScreen(Screen parent) {
      super(parent, "Custom Crosshair Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("Alpha", 0, 255, () -> VoidCyanClient.crosshairAlpha, val -> VoidCyanClient.crosshairAlpha = val);
      this.addIntSlider("Blue", 0, 255, () -> VoidCyanClient.crosshairBlue, val -> VoidCyanClient.crosshairBlue = val);
      this.addBoolean("Crosshair Dot", () -> VoidCyanClient.crosshairDot, val -> VoidCyanClient.crosshairDot = val);
      this.addIntSlider("Green", 0, 255, () -> VoidCyanClient.crosshairGreen, val -> VoidCyanClient.crosshairGreen = val);
      this.addIntSlider("Red", 0, 255, () -> VoidCyanClient.crosshairRed, val -> VoidCyanClient.crosshairRed = val);
      this.addIntSlider("Size", 1, 20, () -> VoidCyanClient.crosshairSize, val -> VoidCyanClient.crosshairSize = val);
      this.addIntSlider("Crosshair Style", 0, 100, () -> VoidCyanClient.crosshairStyle, val -> VoidCyanClient.crosshairStyle = val);
      this.addIntSlider("Crosshair Thickness", 1, 5, () -> VoidCyanClient.crosshairThickness, val -> VoidCyanClient.crosshairThickness = val);
   }
}
