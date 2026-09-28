package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class DeathInfoSettingsScreen extends BaseSettingsScreen {
   public DeathInfoSettingsScreen(Screen parent) {
      super(parent, "Death Info Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Show On Screen", () -> VoidCyanClient.deathInfoShowOnScreen, val -> VoidCyanClient.deathInfoShowOnScreen = val);
   }
}
