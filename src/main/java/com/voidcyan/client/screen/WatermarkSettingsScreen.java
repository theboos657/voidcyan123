package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class WatermarkSettingsScreen extends BaseSettingsScreen {
   public WatermarkSettingsScreen(Screen parent) {
      super(parent, "Watermark Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enable Watermark", () -> VoidCyanClient.isWatermarkEnabled, val -> VoidCyanClient.isWatermarkEnabled = val);
      this.addBoolean("Animated Color", () -> VoidCyanClient.watermarkAnimatedColor, val -> VoidCyanClient.watermarkAnimatedColor = val);
      this.addBoolean("Use Theme Color", () -> VoidCyanClient.watermarkUseThemeColor, val -> VoidCyanClient.watermarkUseThemeColor = val);
   }
}
