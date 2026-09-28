package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class TotemPopColorSettingsScreen extends BaseSettingsScreen {
   public TotemPopColorSettingsScreen(Screen parent) {
      super(parent, "Totem Pop Color Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isTotemPopColorEnabled, val -> VoidCyanClient.isTotemPopColorEnabled = val);
      this.addBoolean("Apply to Self", () -> VoidCyanClient.totemPopColorDetectSelf, val -> VoidCyanClient.totemPopColorDetectSelf = val);
      this.addBoolean("Apply to Others", () -> VoidCyanClient.totemPopColorDetectOthers, val -> VoidCyanClient.totemPopColorDetectOthers = val);
      this.addBoolean("Apply to Armor", () -> VoidCyanClient.totemPopColorApplyToArmor, val -> VoidCyanClient.totemPopColorApplyToArmor = val);
      this.addBoolean(
         "Apply to Armor Stands", () -> VoidCyanClient.totemPopColorApplyToArmorStands, val -> VoidCyanClient.totemPopColorApplyToArmorStands = val
      );
      this.addIntSlider("Transparency", 0, 255, () -> VoidCyanClient.totemPopColorAlpha, val -> VoidCyanClient.totemPopColorAlpha = val);
      this.addIntSlider("Duration", 1, 40, () -> VoidCyanClient.totemPopColorDuration, val -> VoidCyanClient.totemPopColorDuration = val);
      this.addIntSlider("Color Red", 0, 255, () -> VoidCyanClient.totemPopColorRed, val -> VoidCyanClient.totemPopColorRed = val);
      this.addIntSlider("Color Green", 0, 255, () -> VoidCyanClient.totemPopColorGreen, val -> VoidCyanClient.totemPopColorGreen = val);
      this.addIntSlider("Color Blue", 0, 255, () -> VoidCyanClient.totemPopColorBlue, val -> VoidCyanClient.totemPopColorBlue = val);
   }
}
