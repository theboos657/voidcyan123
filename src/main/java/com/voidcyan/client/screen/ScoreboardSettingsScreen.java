package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ScoreboardSettingsScreen extends BaseSettingsScreen {
   public ScoreboardSettingsScreen(Screen parent) {
      super(parent, "Scoreboard Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isScoreboardEnabled, val -> VoidCyanClient.isScoreboardEnabled = val);
      this.addIntSlider("X Offset", -200, 200, () -> VoidCyanClient.scoreboardOffsetX, val -> VoidCyanClient.scoreboardOffsetX = val);
      this.addIntSlider("Y Offset", -200, 200, () -> VoidCyanClient.scoreboardOffsetY, val -> VoidCyanClient.scoreboardOffsetY = val);
      this.addColorPicker("Default Color", () -> VoidCyanClient.scoreboardColor, val -> VoidCyanClient.scoreboardColor = 0xFF000000 | val);
   }
}
