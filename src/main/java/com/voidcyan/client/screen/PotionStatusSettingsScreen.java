package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class PotionStatusSettingsScreen extends BaseSettingsScreen {
   public PotionStatusSettingsScreen(Screen parent) {
      super(parent, "Potion Status Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Style", new String[]{"Custom", "Vanilla"}, () -> VoidCyanClient.potionStatusStyle, val -> VoidCyanClient.potionStatusStyle = val);
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
