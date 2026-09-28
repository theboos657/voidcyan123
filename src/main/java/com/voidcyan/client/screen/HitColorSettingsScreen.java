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
      this.addIntSlider("Color Red", 0, 255, () -> VoidCyanClient.hitColorRed, val -> {
         VoidCyanClient.hitColorRed = val;
         VoidCyanClient.onHitColorChanged();
      });
      this.addIntSlider("Color Green", 0, 255, () -> VoidCyanClient.hitColorGreen, val -> {
         VoidCyanClient.hitColorGreen = val;
         VoidCyanClient.onHitColorChanged();
      });
      this.addIntSlider("Color Blue", 0, 255, () -> VoidCyanClient.hitColorBlue, val -> {
         VoidCyanClient.hitColorBlue = val;
         VoidCyanClient.onHitColorChanged();
      });
   }
}
