package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class IrlClockSettingsScreen extends BaseSettingsScreen {
   public IrlClockSettingsScreen(Screen parent) {
      super(parent, "IRL Clock Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Clock24 Hour", () -> VoidCyanClient.isIrlClock24Hour, val -> VoidCyanClient.isIrlClock24Hour = val);
      this.addBoolean("Date Enabled", () -> VoidCyanClient.isIrlDateEnabled, val -> VoidCyanClient.isIrlDateEnabled = val);
   }
}
