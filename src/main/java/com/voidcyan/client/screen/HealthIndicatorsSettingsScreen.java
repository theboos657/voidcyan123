package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class HealthIndicatorsSettingsScreen extends BaseSettingsScreen {
   public HealthIndicatorsSettingsScreen(Screen parent) {
      super(parent, "Health Indicators Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Anti Bot", () -> VoidCyanClient.antiBot, val -> VoidCyanClient.antiBot = val);
      this.addEnum("Mode", new String[]{"Bar", "Hearts"}, () -> VoidCyanClient.healthIndicatorsMode, val -> VoidCyanClient.healthIndicatorsMode = val);
      this.addBoolean("Fuse Absorption Hearts", () -> VoidCyanClient.healthIndicatorsFuseAbsorption, val -> VoidCyanClient.healthIndicatorsFuseAbsorption = val);
      this.addIntSlider("Range", 5, 50, () -> VoidCyanClient.healthIndicatorsRange, val -> VoidCyanClient.healthIndicatorsRange = val);
      this.addEnum(
         "Position", new String[]{"Above", "Left", "Right"}, () -> VoidCyanClient.healthIndicatorsType, val -> VoidCyanClient.healthIndicatorsType = val
      );
      this.addBoolean("Indicators Enabled", () -> VoidCyanClient.isHealthIndicatorsEnabled, val -> VoidCyanClient.isHealthIndicatorsEnabled = val);
   }
}
