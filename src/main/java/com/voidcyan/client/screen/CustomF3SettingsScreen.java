package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class CustomF3SettingsScreen extends BaseSettingsScreen {
   public CustomF3SettingsScreen(Screen parent) {
      super(parent, "Vanilla F3 Options");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("F3 Enabled", () -> VoidCyanClient.isCustomF3Enabled, val -> VoidCyanClient.isCustomF3Enabled = val);
   }
}
