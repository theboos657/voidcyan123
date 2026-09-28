package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class CustomHitboxSettingsScreen extends BaseSettingsScreen {
   public CustomHitboxSettingsScreen(Screen parent) {
      super(parent, "Custom Hitbox Settings");
   }

   @Override
   protected void buildSettings() {
      this.addIntSlider("Render Range", 10, 100, () -> VoidCyanClient.customHitboxRange, val -> VoidCyanClient.customHitboxRange = val);
      this.addIntSlider("Blue", 0, 255, () -> VoidCyanClient.hitboxBlue, val -> VoidCyanClient.hitboxBlue = val);
      this.addIntSlider("Green", 0, 255, () -> VoidCyanClient.hitboxGreen, val -> VoidCyanClient.hitboxGreen = val);
      this.addIntSlider("Red", 0, 255, () -> VoidCyanClient.hitboxRed, val -> VoidCyanClient.hitboxRed = val);
      this.addSlider("Hitbox Thickness", 0.5F, 5.0F, () -> VoidCyanClient.hitboxThickness, val -> VoidCyanClient.hitboxThickness = val);
      this.addBoolean("Hitbox Dynamic", () -> VoidCyanClient.isCustomHitboxDynamic, val -> VoidCyanClient.isCustomHitboxDynamic = val);
      this.addBoolean("Show Crystal Hitbox", () -> VoidCyanClient.hitboxShowCrystal, val -> VoidCyanClient.hitboxShowCrystal = val);
      this.addIntSlider("Crystal Blue", 0, 255, () -> VoidCyanClient.crystalHitboxBlue, val -> VoidCyanClient.crystalHitboxBlue = val);
      this.addIntSlider("Crystal Green", 0, 255, () -> VoidCyanClient.crystalHitboxGreen, val -> VoidCyanClient.crystalHitboxGreen = val);
      this.addIntSlider("Crystal Red", 0, 255, () -> VoidCyanClient.crystalHitboxRed, val -> VoidCyanClient.crystalHitboxRed = val);
   }
}
