package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class FpsGraphSettingsScreen extends BaseSettingsScreen {
   public FpsGraphSettingsScreen(Screen parent) {
      super(parent, "FPS Graph Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Show Background", () -> VoidCyanClient.fpsGraphBackground, val -> VoidCyanClient.fpsGraphBackground = val);
      this.addBoolean("Show Fps01 Percent", () -> VoidCyanClient.showFps01Percent, val -> VoidCyanClient.showFps01Percent = val);
      this.addBoolean("Show Fps1 Percent", () -> VoidCyanClient.showFps1Percent, val -> VoidCyanClient.showFps1Percent = val);
      this.addBoolean("Show Fps Avg", () -> VoidCyanClient.showFpsAvg, val -> VoidCyanClient.showFpsAvg = val);
      this.addBoolean("Show Fps Min Max", () -> VoidCyanClient.showFpsMinMax, val -> VoidCyanClient.showFpsMinMax = val);
   }
}
