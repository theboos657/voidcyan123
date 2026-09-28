package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class FpsCounterSettingsScreen extends BaseSettingsScreen {
   public FpsCounterSettingsScreen(Screen parent) {
      super(parent, "FPS Counter Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("HUD Update Rate (FPS)", 0, 60, () -> VoidCyanClient.hudFpsLimit, val -> VoidCyanClient.hudFpsLimit = val);
   }
}
