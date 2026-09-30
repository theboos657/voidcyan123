package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class HitColorSettingsScreen extends BaseSettingsScreen {
   public HitColorSettingsScreen(Screen parent) {
      super(parent, "Hit Color Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isHitColorEnabled, val -> {
         VoidCyanClient.isHitColorEnabled = val;
         VoidCyanClient.onHitColorChanged();
      });
      this.addBoolean("Apply to Self", () -> VoidCyanClient.hitColorApplyToSelf, val -> VoidCyanClient.hitColorApplyToSelf = val);
      this.addBoolean("Apply to Armor", () -> VoidCyanClient.hitColorApplyToArmor, val -> VoidCyanClient.hitColorApplyToArmor = val);
      this.addBoolean("Apply to Armor Stands", () -> VoidCyanClient.hitColorApplyToArmorStands, val -> VoidCyanClient.hitColorApplyToArmorStands = val);
      this.addIntSlider("Transparency", 0, 255, () -> VoidCyanClient.hitColorAlpha, val -> {
         VoidCyanClient.hitColorAlpha = val;
         VoidCyanClient.onHitColorChanged();
      });
      this.addIntSlider("Duration", 1, 40, () -> VoidCyanClient.hitColorDuration, val -> VoidCyanClient.hitColorDuration = val);
      this.addColorPicker("Color", () -> VoidCyanClient.hitColorRed << 16 | VoidCyanClient.hitColorGreen << 8 | VoidCyanClient.hitColorBlue, rgb -> {
         VoidCyanClient.hitColorRed = rgb >> 16 & 255;
         VoidCyanClient.hitColorGreen = rgb >> 8 & 255;
         VoidCyanClient.hitColorBlue = rgb & 255;
         VoidCyanClient.onHitColorChanged();
      });
   }
}
