package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class FreelookSettingsScreen extends BaseSettingsScreen {
   public FreelookSettingsScreen(Screen parent) {
      super(parent, "Freelook Settings");
   }

   @Override
   protected boolean includeAutoToggleKeybind() {
      return false;
   }

   @Override
   protected void buildSettings() {
      this.addKeybind("Freelook Key", () -> VoidCyanClient.freelookKey, val -> VoidCyanClient.freelookKey = val);
      this.addBoolean("Show Own Nametag", () -> VoidCyanClient.freelookShowOwnNametag, val -> VoidCyanClient.freelookShowOwnNametag = val);
      this.addBoolean("Toggle Mode", () -> VoidCyanClient.freelookToggleMode, val -> VoidCyanClient.freelookToggleMode = val);
   }
}
