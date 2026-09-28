package com.voidcyan.client.screen;

import com.voidcyan.client.VoidCyanClient;
import net.minecraft.client.gui.screen.Screen;

public class AttackHudSettingsScreen extends BaseSettingsScreen {
   public AttackHudSettingsScreen(Screen parent) {
      super(parent, "Attack Indicator HUD Settings");
   }

   @Override
   protected void buildSettings() {
      this.addEnum("Mode", new String[]{"Item", "Custom", "Vanilla"}, () -> VoidCyanClient.attackHudMode, val -> {
         VoidCyanClient.attackHudMode = val;
         VoidCyanClient.saveConfig();
      });
      this.addIntSlider("Size", 16, 80, () -> VoidCyanClient.attackHudSize, val -> {
         VoidCyanClient.attackHudSize = val;
         VoidCyanClient.saveConfig();
      });
   }
}
