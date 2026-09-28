package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class PlayerTrailSettingsScreen extends BaseSettingsScreen {
   public PlayerTrailSettingsScreen(Screen parent) {
      super(parent, "Player Trail Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("Anchor", 0, 100, () -> VoidCyanClient.playerTrailAnchor, val -> VoidCyanClient.playerTrailAnchor = val);
      this.addIntSlider("Color", 0, 100, () -> VoidCyanClient.playerTrailColor, val -> VoidCyanClient.playerTrailColor = val);
      this.addIntSlider("Color B", 0, 100, () -> VoidCyanClient.playerTrailColorB, val -> VoidCyanClient.playerTrailColorB = val);
      this.addIntSlider("Color G", 0, 100, () -> VoidCyanClient.playerTrailColorG, val -> VoidCyanClient.playerTrailColorG = val);
      this.addIntSlider("Color R", 0, 100, () -> VoidCyanClient.playerTrailColorR, val -> VoidCyanClient.playerTrailColorR = val);
      this.addSlider("Duration", 0.5F, 30.0F, () -> VoidCyanClient.playerTrailDuration, val -> VoidCyanClient.playerTrailDuration = val);
      this.addBoolean("Show Others", () -> VoidCyanClient.playerTrailShowOthers, val -> VoidCyanClient.playerTrailShowOthers = val);
      this.addBoolean("Show Self", () -> VoidCyanClient.playerTrailShowSelf, val -> VoidCyanClient.playerTrailShowSelf = val);
   }
}
