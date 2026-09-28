package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class VisualSettingsScreen extends BaseSettingsScreen {
   public VisualSettingsScreen(Screen parent) {
      super(parent, "Visual Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Custom FOV Enabled", () -> VoidCyanClient.isCustomFovEnabled, val -> VoidCyanClient.isCustomFovEnabled = val);
      this.addIntSlider("Custom FOV", 10, 300, () -> VoidCyanClient.customFov, val -> VoidCyanClient.customFov = val);
   }
}
