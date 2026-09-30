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
      this.addColorPicker("Color", () -> VoidCyanClient.playerTrailColor, rgb -> {
         VoidCyanClient.playerTrailColor = rgb;
         VoidCyanClient.playerTrailColorR = rgb >> 16 & 255;
         VoidCyanClient.playerTrailColorG = rgb >> 8 & 255;
         VoidCyanClient.playerTrailColorB = rgb & 255;
      });
      this.addSlider("Duration", 0.5F, 30.0F, () -> VoidCyanClient.playerTrailDuration, val -> VoidCyanClient.playerTrailDuration = val);
      this.addBoolean("Show Others", () -> VoidCyanClient.playerTrailShowOthers, val -> VoidCyanClient.playerTrailShowOthers = val);
      this.addBoolean("Show Self", () -> VoidCyanClient.playerTrailShowSelf, val -> VoidCyanClient.playerTrailShowSelf = val);
   }
}
