package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class ChinaHatSettingsScreen extends BaseSettingsScreen {
   public ChinaHatSettingsScreen(Screen parent) {
      super(parent, "China Hat Settings");
   }

   @Override
   protected void buildSettings() {
      this.addColorPicker("Hat Color", () -> VoidCyanClient.chinaHatRed << 16 | VoidCyanClient.chinaHatGreen << 8 | VoidCyanClient.chinaHatBlue, rgb -> {
         VoidCyanClient.chinaHatRed = rgb >> 16 & 255;
         VoidCyanClient.chinaHatGreen = rgb >> 8 & 255;
         VoidCyanClient.chinaHatBlue = rgb & 255;
      });
      this.addSlider("Size", 0.5F, 3.0F, () -> VoidCyanClient.chinaHatSize, val -> VoidCyanClient.chinaHatSize = val);
      this.addBoolean("Filled", () -> VoidCyanClient.chinaHatFilled, val -> VoidCyanClient.chinaHatFilled = val);
   }
}
