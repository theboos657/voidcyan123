package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class DamageColorSettingsScreen extends BaseSettingsScreen {
   public DamageColorSettingsScreen(Screen parent) {
      super(parent, "Damage Color Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Enabled", () -> VoidCyanClient.isDamageColorEnabled, val -> VoidCyanClient.isDamageColorEnabled = val);
      this.addBoolean("Apply to Self", () -> VoidCyanClient.damageColorApplyToSelf, val -> VoidCyanClient.damageColorApplyToSelf = val);
      this.addBoolean("Apply to Players", () -> VoidCyanClient.damageColorApplyToPlayers, val -> VoidCyanClient.damageColorApplyToPlayers = val);
      this.addBoolean("Apply to Entities", () -> VoidCyanClient.damageColorApplyToEntities, val -> VoidCyanClient.damageColorApplyToEntities = val);
      this.addBoolean("Apply to Armor", () -> VoidCyanClient.damageColorApplyToArmor, val -> VoidCyanClient.damageColorApplyToArmor = val);
      this.addBoolean("Apply to Armor Stands", () -> VoidCyanClient.damageColorApplyToArmorStands, val -> VoidCyanClient.damageColorApplyToArmorStands = val);
      this.addIntSlider("Transparency", 0, 255, () -> VoidCyanClient.damageColorAlpha, val -> VoidCyanClient.damageColorAlpha = val);
      this.addIntSlider("Duration", 1, 40, () -> VoidCyanClient.damageColorDuration, val -> VoidCyanClient.damageColorDuration = val);
      this.addColorPicker("Color", () -> VoidCyanClient.damageColorRed << 16 | VoidCyanClient.damageColorGreen << 8 | VoidCyanClient.damageColorBlue, rgb -> {
         VoidCyanClient.damageColorRed = rgb >> 16 & 255;
         VoidCyanClient.damageColorGreen = rgb >> 8 & 255;
         VoidCyanClient.damageColorBlue = rgb & 255;
      });
   }
}
