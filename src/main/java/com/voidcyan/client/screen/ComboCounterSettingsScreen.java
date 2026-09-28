package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ComboCounterSettingsScreen extends BaseSettingsScreen {
   public ComboCounterSettingsScreen(Screen parent) {
      super(parent, "Combo Counter Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Version", new String[]{"1.9", "1.8"}, () -> VoidCyanClient.comboVersion, val -> VoidCyanClient.comboVersion = val);
      this.addIntSlider("Timeout", 500, 10000, () -> VoidCyanClient.comboTimeout, val -> VoidCyanClient.comboTimeout = val);
      this.addKeybind("Reset Counter", () -> VoidCyanClient.comboResetKeybind, val -> VoidCyanClient.comboResetKeybind = val);
   }
}
