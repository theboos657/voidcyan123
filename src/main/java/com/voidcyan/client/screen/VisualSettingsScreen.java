package com.voidcyan.client.screen;

import com.voidcyan.client.FeatureModules;
import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class VisualSettingsScreen extends BaseSettingsScreen {
   public VisualSettingsScreen(Screen parent) {
      super(parent, "Visual Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Custom FOV Enabled", () -> VoidCyanClient.isCustomFovEnabled, val -> VoidCyanClient.isCustomFovEnabled = val);
      this.addIntSlider("Custom FOV", 10, 300, () -> VoidCyanClient.customFov, val -> VoidCyanClient.customFov = val);
      this.addBoolean("Motion Blur", () -> FeatureModules.on[FeatureModules.MBLUR], val -> {
         FeatureModules.on[FeatureModules.MBLUR] = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addSlider("Motion Blur Strength", 1.0F, 10.0F, () -> FeatureModules.blurStrength, val -> {
         FeatureModules.blurStrength = val;
         VoidCyanClient.markConfigDirty();
      });
      this.addBoolean("Menu Blur", () -> FeatureModules.on[FeatureModules.MENUBLUR], val -> {
         FeatureModules.on[FeatureModules.MENUBLUR] = val;
         VoidCyanClient.markConfigDirty();
      });
   }
}
