package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class TransparentShieldSettingsScreen extends BaseSettingsScreen {
   public TransparentShieldSettingsScreen(Screen parent) {
      super(parent, "Transparent Shield Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("Opacity %", 5, 100, () -> VoidCyanClient.transparentShieldOpacity, val -> VoidCyanClient.transparentShieldOpacity = val);
   }
}
