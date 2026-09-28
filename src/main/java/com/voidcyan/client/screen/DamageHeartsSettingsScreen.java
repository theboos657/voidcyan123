package com.voidcyan.client.screen;

import com.voidcyan.client.module.damagehearts.DamageHeartsModule;
import net.minecraft.client.gui.screen.Screen;

public class DamageHeartsSettingsScreen extends BaseSettingsScreen {
   public DamageHeartsSettingsScreen(Screen parent) {
      super(parent, "Damage Hearts Settings");
   }

   @Override
   protected void buildSettings() {
      this.addBoolean("Golden Hearts (Absorption = Gold)", () -> DamageHeartsModule.goldenHeartsMode, val -> DamageHeartsModule.goldenHeartsMode = val);
      this.addBoolean("Show Players", () -> DamageHeartsModule.showPlayers, val -> DamageHeartsModule.showPlayers = val);
      this.addBoolean("Show Hostiles", () -> DamageHeartsModule.showHostile, val -> DamageHeartsModule.showHostile = val);
      this.addBoolean("Show Animals", () -> DamageHeartsModule.showAnimals, val -> DamageHeartsModule.showAnimals = val);
      this.addBoolean("Show Bosses", () -> DamageHeartsModule.showBosses, val -> DamageHeartsModule.showBosses = val);
      this.addBoolean("Rainbow Mode", () -> DamageHeartsModule.rainbowMode, val -> DamageHeartsModule.rainbowMode = val);
      this.addBoolean("Show Number", () -> DamageHeartsModule.showNumeric, val -> DamageHeartsModule.showNumeric = val);
      this.addIntSlider(
         "Lifetime (x0.1s)", 5, 80, () -> (int)(DamageHeartsModule.lifetime * 10.0F), val -> DamageHeartsModule.lifetime = val.intValue() / 10.0F
      );
      this.addIntSlider(
         "Fade Time (x0.1s)", 5, 40, () -> (int)(DamageHeartsModule.fadeTime * 10.0F), val -> DamageHeartsModule.fadeTime = val.intValue() / 10.0F
      );
      this.addIntSlider("Combine Delay (ms)", 50, 500, () -> DamageHeartsModule.combineDelayMs, val -> DamageHeartsModule.combineDelayMs = val);
      this.addIntSlider("Render Distance", 10, 200, () -> (int)DamageHeartsModule.renderDistance, val -> DamageHeartsModule.renderDistance = val.intValue());
   }
}
