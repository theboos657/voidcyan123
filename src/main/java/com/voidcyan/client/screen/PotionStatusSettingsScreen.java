package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class PotionStatusSettingsScreen extends BaseSettingsScreen {
   public PotionStatusSettingsScreen(Screen parent) {
      super(parent, "Potion Status Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Icon Type", new String[]{"Letter", "Icon"}, () -> VoidCyanClient.potionStatusIconType, val -> VoidCyanClient.potionStatusIconType = val);
      this.addEnum(
         "Orientation",
         new String[]{"Horizontal", "Vertical"},
         () -> VoidCyanClient.potionStatusOrientation,
         val -> VoidCyanClient.potionStatusOrientation = val
      );
      this.addBoolean("Show Amplifier", () -> VoidCyanClient.potionStatusShowAmplifier, val -> VoidCyanClient.potionStatusShowAmplifier = val);
      this.addBoolean("Show Duration", () -> VoidCyanClient.potionStatusShowDuration, val -> VoidCyanClient.potionStatusShowDuration = val);
   }
}
