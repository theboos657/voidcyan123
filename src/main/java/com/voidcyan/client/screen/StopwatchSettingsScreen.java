package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class StopwatchSettingsScreen extends BaseSettingsScreen {
   public StopwatchSettingsScreen(Screen parent) {
      super(parent, "Stopwatch Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Show Background", () -> VoidCyanClient.stopwatchBackground, val -> VoidCyanClient.stopwatchBackground = val);
      this.addBoolean("Running", () -> VoidCyanClient.stopwatchRunning, val -> VoidCyanClient.stopwatchRunning = val);
   }
}
